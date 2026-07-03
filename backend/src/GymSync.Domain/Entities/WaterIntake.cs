namespace GymSync.Domain.Entities;

public class WaterIntake
{
    public Guid Id { get; set; }
    public Guid UserId { get; set; }
    public User User { get; set; } = null!;
    public Guid? WorkoutId { get; set; }
    public Workout? Workout { get; set; }
    public int AmountMl { get; set; } = 250;
    public DateTime TakenAt { get; set; } = DateTime.UtcNow;
}
