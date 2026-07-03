using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Domain.Enums;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Workouts.Commands;

public record FinishWorkoutCommand : IRequest<Result<WorkoutSummaryResponse>>
{
    public Guid WorkoutId { get; init; }
    public int? CaloriesBurned { get; init; }
}

public record WorkoutSummaryResponse
{
    public Guid Id { get; init; }
    public string Name { get; init; } = string.Empty;
    public TimeSpan Duration { get; init; }
    public int TotalExercises { get; init; }
    public int TotalSets { get; init; }
    public int TotalReps { get; init; }
    public int TotalVolume { get; init; }
    public int? CaloriesBurned { get; init; }
    public int PersonalRecords { get; init; }
    public List<string> MusclesTrained { get; init; } = new();
}

public class FinishWorkoutCommandHandler : IRequestHandler<FinishWorkoutCommand, Result<WorkoutSummaryResponse>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;
    private readonly INotificationService _notificationService;

    public FinishWorkoutCommandHandler(
        IApplicationDbContext context,
        ICurrentUserService currentUser,
        INotificationService notificationService)
    {
        _context = context;
        _currentUser = currentUser;
        _notificationService = notificationService;
    }

    public async Task<Result<WorkoutSummaryResponse>> Handle(FinishWorkoutCommand request, CancellationToken ct)
    {
        var workout = await _context.Workouts
            .Include(w => w.Exercises)
                .ThenInclude(e => e.Exercise)
            .Include(w => w.Exercises)
                .ThenInclude(e => e.Sets)
            .FirstOrDefaultAsync(w => w.Id == request.WorkoutId && w.UserId == _currentUser.UserId, ct);

        if (workout is null)
            return Result.Failure<WorkoutSummaryResponse>("Workout not found");

        workout.Status = WorkoutStatus.Completed;
        workout.CompletedAt = DateTime.UtcNow;
        workout.Duration = workout.CompletedAt.Value - workout.StartedAt;
        workout.CaloriesBurned = request.CaloriesBurned;

        var user = await _context.Users.FirstAsync(u => u.Id == _currentUser.UserId, ct);
        user.Status = UserStatus.FinishedWorkout;
        user.LastWorkoutAt = DateTime.UtcNow;
        user.CurrentStreak++;
        if (user.CurrentStreak > user.LongestStreak)
            user.LongestStreak = user.CurrentStreak;

        var summary = new WorkoutSummaryResponse
        {
            Id = workout.Id,
            Name = workout.Name,
            Duration = workout.Duration ?? TimeSpan.Zero,
            TotalExercises = workout.Exercises.Count,
            TotalSets = workout.Exercises.Sum(e => e.Sets.Count(s => s.IsCompleted)),
            TotalReps = workout.Exercises.Sum(e => e.Sets.Where(s => s.IsCompleted).Sum(s => s.Reps ?? 0)),
            TotalVolume = workout.Exercises.Sum(e => e.Sets.Where(s => s.IsCompleted).Sum(s => (s.Reps ?? 0) * (int)(s.Weight ?? 0))),
            CaloriesBurned = request.CaloriesBurned,
            PersonalRecords = workout.Exercises.Sum(e => e.Sets.Count(s => s.IsPersonalRecord)),
            MusclesTrained = workout.Exercises
                .Select(e => e.Exercise.MuscleGroup)
                .Distinct()
                .ToList()
        };

        await _context.SaveChangesAsync(ct);

        if (workout.IsShared && workout.SharedWithUserId.HasValue)
        {
            await _notificationService.SendPushNotificationToPartnerAsync(
                _currentUser.UserId,
                "Workout Complete",
                $"{user.DisplayName} finished their workout!",
                new { workoutId = workout.Id, type = "workout_complete" });
        }

        return Result.Success(summary);
    }
}
