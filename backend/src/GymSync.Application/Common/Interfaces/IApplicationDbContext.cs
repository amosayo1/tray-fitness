using GymSync.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Common.Interfaces;

public interface IApplicationDbContext
{
    DbSet<User> Users { get; }
    DbSet<RefreshToken> RefreshTokens { get; }
    DbSet<InviteToken> InviteTokens { get; }
    DbSet<Workout> Workouts { get; }
    DbSet<WorkoutSession> WorkoutSessions { get; }
    DbSet<Exercise> Exercises { get; }
    DbSet<WorkoutExercise> WorkoutExercises { get; }
    DbSet<ExerciseSet> ExerciseSets { get; }
    DbSet<RestTimer> RestTimers { get; }
    DbSet<ChatMessage> ChatMessages { get; }
    DbSet<Motivation> Motivations { get; }
    DbSet<Challenge> Challenges { get; }
    DbSet<ChallengeProgress> ChallengeProgresses { get; }
    DbSet<WorkoutTemplate> WorkoutTemplates { get; }
    DbSet<WorkoutTemplateExercise> WorkoutTemplateExercises { get; }
    DbSet<Notification> Notifications { get; }
    DbSet<DailyStepLog> DailyStepLogs { get; }
    DbSet<Pet> Pets { get; }
    DbSet<WaterReminder> WaterReminders { get; }
    DbSet<WaterIntake> WaterIntakes { get; }

    Task<int> SaveChangesAsync(CancellationToken cancellationToken = default);
}
