namespace GymSync.Domain.Entities;

public class RestTimer
{
    public Guid Id { get; set; }
    public Guid WorkoutId { get; set; }
    public Workout Workout { get; set; } = null!;
    public Guid UserId { get; set; }
    public int DurationSeconds { get; set; }
    public int RemainingSeconds { get; set; }
    public bool IsRunning { get; set; }
    public DateTime? StartedAt { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
