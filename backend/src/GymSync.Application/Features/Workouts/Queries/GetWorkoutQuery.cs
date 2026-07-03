using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Workouts.Queries;

public record GetWorkoutQuery(Guid WorkoutId) : IRequest<Result<WorkoutDetailDto>>;

public record WorkoutExerciseDto
{
    public Guid Id { get; init; }
    public Guid ExerciseId { get; init; }
    public string ExerciseName { get; init; } = string.Empty;
    public string MuscleGroup { get; init; } = string.Empty;
    public int Order { get; init; }
    public List<ExerciseSetDto> Sets { get; init; } = new();
}

public record ExerciseSetDto
{
    public Guid Id { get; init; }
    public int SetNumber { get; init; }
    public int? Reps { get; init; }
    public decimal? Weight { get; init; }
    public int? Steps { get; init; }
    public int? Rpe { get; init; }
    public bool IsCompleted { get; init; }
    public bool IsPersonalRecord { get; init; }
}

public record WorkoutDetailDto
{
    public Guid Id { get; init; }
    public string Name { get; init; } = string.Empty;
    public DateTime StartedAt { get; init; }
    public int TotalVolume { get; init; }
    public int? CaloriesBurned { get; init; }
    public List<WorkoutExerciseDto> Exercises { get; init; } = new();
}

public class GetWorkoutQueryHandler : IRequestHandler<GetWorkoutQuery, Result<WorkoutDetailDto>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public GetWorkoutQueryHandler(IApplicationDbContext context, ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result<WorkoutDetailDto>> Handle(GetWorkoutQuery request, CancellationToken ct)
    {
        var workout = await _context.Workouts
            .Include(w => w.Exercises)
                .ThenInclude(we => we.Exercise)
            .Include(w => w.Exercises)
                .ThenInclude(we => we.Sets.OrderBy(s => s.SetNumber))
            .FirstOrDefaultAsync(w => w.Id == request.WorkoutId && w.UserId == _currentUser.UserId, ct);

        if (workout is null)
            return Result.Failure<WorkoutDetailDto>("Workout not found");

        return Result.Success(new WorkoutDetailDto
        {
            Id = workout.Id,
            Name = workout.Name,
            StartedAt = workout.StartedAt,
            TotalVolume = workout.TotalVolume,
            CaloriesBurned = workout.CaloriesBurned,
            Exercises = workout.Exercises.OrderBy(e => e.Order).Select(we => new WorkoutExerciseDto
            {
                Id = we.Id,
                ExerciseId = we.ExerciseId,
                ExerciseName = we.Exercise.Name,
                MuscleGroup = we.Exercise.MuscleGroup,
                Order = we.Order,
                Sets = we.Sets.Select(s => new ExerciseSetDto
                {
                    Id = s.Id,
                    SetNumber = s.SetNumber,
                    Reps = s.Reps,
                    Weight = s.Weight,
                    Steps = s.Steps,
                    Rpe = s.Rpe,
                    IsCompleted = s.IsCompleted,
                    IsPersonalRecord = s.IsPersonalRecord
                }).ToList()
            }).ToList()
        });
    }
}
