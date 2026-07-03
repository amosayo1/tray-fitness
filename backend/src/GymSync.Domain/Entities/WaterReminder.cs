namespace GymSync.Domain.Entities;

public class WaterReminder
{
    public Guid Id { get; set; }
    public Guid UserId { get; set; }
    public User User { get; set; } = null!;
    public Guid? WorkoutId { get; set; }
    public Workout? Workout { get; set; }
    public int IntervalMinutes { get; set; } = 15;
    public DateTime? LastRemindedAt { get; set; }
    public bool IsActive { get; set; } = true;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
}
