using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Workouts.Commands;

public record CompleteSetCommand : IRequest<Result<SetCompletedResponse>>
{
    public Guid WorkoutExerciseId { get; init; }
    public int SetNumber { get; init; }
    public int? Reps { get; init; }
    public decimal? Weight { get; init; }
    public int? Rpe { get; init; }
    public string? Notes { get; init; }
}

public record SetCompletedResponse
{
    public bool IsPersonalRecord { get; init; }
    public string? PreviousPrValue { get; init; }
}

public class CompleteSetCommandHandler : IRequestHandler<CompleteSetCommand, Result<SetCompletedResponse>>
{
    private readonly IApplicationDbContext _context;

    public CompleteSetCommandHandler(IApplicationDbContext context)
    {
        _context = context;
    }

    public async Task<Result<SetCompletedResponse>> Handle(CompleteSetCommand request, CancellationToken ct)
    {
        var exerciseSet = await _context.ExerciseSets
            .Include(s => s.WorkoutExercise)
                .ThenInclude(we => we.Exercise)
            .Include(s => s.WorkoutExercise)
                .ThenInclude(we => we.Workout)
            .FirstOrDefaultAsync(s =>
                s.WorkoutExerciseId == request.WorkoutExerciseId &&
                s.SetNumber == request.SetNumber, ct);

        if (exerciseSet is null)
            return Result.Failure<SetCompletedResponse>("Set not found");

        exerciseSet.Reps = request.Reps ?? exerciseSet.Reps;
        exerciseSet.Weight = request.Weight ?? exerciseSet.Weight;
        exerciseSet.Rpe = request.Rpe;
        exerciseSet.Notes = request.Notes;
        exerciseSet.IsCompleted = true;

        var isPr = false;
        string? previousPr = null;

        if (request.Weight.HasValue && request.Reps.HasValue)
        {
            var estimatedOneRm = request.Weight.Value * (decimal)(1 + request.Reps.Value / 30.0);
            var previousSets = await _context.ExerciseSets
                .Where(s => s.WorkoutExercise.ExerciseId == exerciseSet.WorkoutExercise.ExerciseId &&
                            s.IsCompleted && s.Id != exerciseSet.Id)
                .Select(s => s.Weight * (decimal)(1 + (s.Reps ?? 0) / 30.0))
                .ToListAsync(ct);

            if (previousSets.Count > 0)
            {
                var maxPrevious = previousSets.Max();
                if (estimatedOneRm > (maxPrevious ?? 0))
                {
                    isPr = true;
                    previousPr = maxPrevious?.ToString("F1") ?? "0";
                }
            }
            else
            {
                isPr = true;
            }
        }

        exerciseSet.IsPersonalRecord = isPr;

        if (isPr && exerciseSet.Weight.HasValue)
        {
            var volume = exerciseSet.Reps.GetValueOrDefault(0) * (int)Math.Floor(exerciseSet.Weight.Value);
            var we = exerciseSet.WorkoutExercise;
            we.Workout.TotalVolume += volume;
        }

        await _context.SaveChangesAsync(ct);

        return Result.Success(new SetCompletedResponse
        {
            IsPersonalRecord = isPr,
            PreviousPrValue = previousPr
        });
    }
}
