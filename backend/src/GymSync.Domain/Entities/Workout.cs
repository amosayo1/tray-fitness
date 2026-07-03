using GymSync.Domain.Enums;

namespace GymSync.Domain.Entities;

public class Workout
{
    public Guid Id { get; set; }
    public Guid UserId { get; set; }
    public User User { get; set; } = null!;
    public string Name { get; set; } = string.Empty;
    public string? Notes { get; set; }
    public WorkoutStatus Status { get; set; } = WorkoutStatus.NotStarted;
    public DateTime StartedAt { get; set; }
    public DateTime? CompletedAt { get; set; }
    public TimeSpan? Duration { get; set; }
    public int? CaloriesBurned { get; set; }
    public int TotalVolume { get; set; }
    public bool IsShared { get; set; }
    public Guid? SharedWithUserId { get; set; }
    public Guid? OriginalWorkoutId { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    public ICollection<WorkoutExercise> Exercises { get; set; } = new List<WorkoutExercise>();
}
