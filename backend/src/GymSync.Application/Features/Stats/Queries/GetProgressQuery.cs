using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Stats.Queries;

public record GetProgressQuery : IRequest<Result<ProgressResponse>>
{
    public int Months { get; init; } = 3;
}

public record ProgressResponse
{
    public List<MonthlyStatsDto> MonthlyStats { get; init; } = new();
    public List<ExerciseProgressDto> ExerciseProgress { get; init; } = new();
    public List<WorkoutCalendarEntry> Calendar { get; init; } = new();
    public StreakInfo Streak { get; init; } = null!;
}

public record MonthlyStatsDto
{
    public string Month { get; init; } = string.Empty;
    public int WorkoutCount { get; init; }
    public int TotalVolume { get; init; }
    public int TotalCalories { get; init; }
    public TimeSpan TotalDuration { get; init; }
}

public record ExerciseProgressDto
{
    public string ExerciseName { get; init; } = string.Empty;
    public decimal BestWeight { get; init; }
    public int BestReps { get; init; }
    public decimal EstimatedOneRm { get; init; }
    public DateTime AchievedAt { get; init; }
}

public record WorkoutCalendarEntry
{
    public DateTime Date { get; init; }
    public bool HasWorkout { get; init; }
    public int? DurationMinutes { get; init; }
}

public record StreakInfo
{
    public int CurrentStreak { get; init; }
    public int LongestStreak { get; init; }
    public DateTime? LastWorkoutDate { get; init; }
    public bool StreakAtRisk { get; init; }
}

public class GetProgressQueryHandler : IRequestHandler<GetProgressQuery, Result<ProgressResponse>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public GetProgressQueryHandler(
        IApplicationDbContext context,
        ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result<ProgressResponse>> Handle(GetProgressQuery request, CancellationToken ct)
    {
        var user = await _context.Users
            .FirstAsync(u => u.Id == _currentUser.UserId, ct);

        var sinceDate = DateTime.UtcNow.AddMonths(-request.Months);
        var today = DateTime.UtcNow.Date;

        var workouts = await _context.Workouts
            .Include(w => w.Exercises)
                .ThenInclude(e => e.Exercise)
            .Include(w => w.Exercises)
                .ThenInclude(e => e.Sets)
            .Where(w => w.UserId == _currentUser.UserId && w.StartedAt >= sinceDate)
            .ToListAsync(ct);

        var monthlyStats = workouts
            .GroupBy(w => new { w.StartedAt.Year, w.StartedAt.Month })
            .Select(g => new MonthlyStatsDto
            {
                Month = $"{g.Key.Year}-{g.Key.Month:D2}",
                WorkoutCount = g.Count(),
                TotalVolume = g.Sum(w => w.Exercises
                    .SelectMany(e => e.Sets)
                    .Where(s => s.IsCompleted)
                    .Sum(s => (s.Reps ?? 0) * (int)(s.Weight ?? 0))),
                TotalCalories = g.Sum(w => w.CaloriesBurned ?? 0),
                TotalDuration = TimeSpan.FromTicks(
                    g.Where(w => w.Duration.HasValue)
                        .Sum(w => w.Duration!.Value.Ticks))
            })
            .OrderBy(m => m.Month)
            .ToList();

        var exerciseProgress = await _context.ExerciseSets
            .Include(s => s.WorkoutExercise)
                .ThenInclude(we => we.Exercise)
            .Where(s => s.IsCompleted && s.Weight.HasValue && s.Reps.HasValue &&
                        s.WorkoutExercise.Workout.UserId == _currentUser.UserId)
            .GroupBy(s => s.WorkoutExercise.Exercise.Name)
            .Select(g => new ExerciseProgressDto
            {
                ExerciseName = g.Key,
                BestWeight = g.Max(s => s.Weight ?? 0),
                BestReps = g.OrderByDescending(s => s.Weight).Select(s => s.Reps ?? 0).FirstOrDefault(),
                EstimatedOneRm = g.Max(s => (s.Weight ?? 0) * (1 + (s.Reps ?? 0) / 30m)),
                AchievedAt = g.OrderByDescending(s => s.Weight).Select(s => s.CreatedAt).FirstOrDefault()
            })
            .OrderByDescending(e => e.EstimatedOneRm)
            .Take(10)
            .ToListAsync(ct);

        var calendar = Enumerable.Range(0, 90)
            .Select(offset =>
            {
                var date = today.AddDays(-offset);
                var workoutOnDate = workouts.FirstOrDefault(w => w.StartedAt.Date == date);
                return new WorkoutCalendarEntry
                {
                    Date = date,
                    HasWorkout = workoutOnDate is not null,
                    DurationMinutes = workoutOnDate?.Duration?.Minutes
                };
            })
            .OrderBy(e => e.Date)
            .ToList();

        var streakAtRisk = user.LastWorkoutAt.HasValue &&
            (DateTime.UtcNow - user.LastWorkoutAt.Value).TotalDays >= 2;

        return Result.Success(new ProgressResponse
        {
            MonthlyStats = monthlyStats,
            ExerciseProgress = exerciseProgress,
            Calendar = calendar,
            Streak = new StreakInfo
            {
                CurrentStreak = user.CurrentStreak,
                LongestStreak = user.LongestStreak,
                LastWorkoutDate = user.LastWorkoutAt,
                StreakAtRisk = streakAtRisk
            }
        });
    }
}
