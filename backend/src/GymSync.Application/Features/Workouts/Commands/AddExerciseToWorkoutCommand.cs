using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Application.Features.Workouts.Queries;
using GymSync.Domain.Entities;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Workouts.Commands;

public record AddExerciseToWorkoutCommand : IRequest<Result<WorkoutExerciseDto>>
{
    public Guid WorkoutId { get; init; }
    public Guid ExerciseId { get; init; }
    public int Order { get; init; }
    public int DefaultSets { get; init; } = 3;
    public int DefaultReps { get; init; } = 10;
}

public class AddExerciseToWorkoutCommandHandler : IRequestHandler<AddExerciseToWorkoutCommand, Result<WorkoutExerciseDto>>
{
    private readonly IApplicationDbContext _context;

    public AddExerciseToWorkoutCommandHandler(IApplicationDbContext context)
    {
        _context = context;
    }

    public async Task<Result<WorkoutExerciseDto>> Handle(AddExerciseToWorkoutCommand request, CancellationToken ct)
    {
        var workout = await _context.Workouts
            .FirstOrDefaultAsync(w => w.Id == request.WorkoutId, ct);

        if (workout is null)
            return Result.Failure<WorkoutExerciseDto>("Workout not found");

        var exercise = await _context.Exercises
            .FirstOrDefaultAsync(e => e.Id == request.ExerciseId, ct);

        if (exercise is null)
            return Result.Failure<WorkoutExerciseDto>("Exercise not found");

        var we = new WorkoutExercise
        {
            Id = Guid.NewGuid(),
            WorkoutId = request.WorkoutId,
            ExerciseId = request.ExerciseId,
            Order = request.Order
        };

        for (int s = 1; s <= request.DefaultSets; s++)
        {
            we.Sets.Add(new ExerciseSet
            {
                Id = Guid.NewGuid(),
                WorkoutExerciseId = we.Id,
                SetNumber = s,
                Reps = request.DefaultReps
            });
        }

        _context.WorkoutExercises.Add(we);
        await _context.SaveChangesAsync(ct);

        return Result.Success(new WorkoutExerciseDto
        {
            Id = we.Id,
            ExerciseId = we.ExerciseId,
            ExerciseName = exercise.Name,
            MuscleGroup = exercise.MuscleGroup,
            Order = we.Order,
            Sets = we.Sets.OrderBy(s => s.SetNumber).Select(s => new ExerciseSetDto
            {
                Id = s.Id,
                SetNumber = s.SetNumber,
                Reps = s.Reps,
                IsCompleted = s.IsCompleted,
                IsPersonalRecord = s.IsPersonalRecord
            }).ToList()
        });
    }
}
