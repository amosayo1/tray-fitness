namespace GymSync.Domain.Entities;

public class DailyStepLog
{
    public Guid Id { get; set; }
    public Guid UserId { get; set; }
    public User User { get; set; } = null!;
    public DateOnly Date { get; set; }
    public int Steps { get; set; }
    public int Target { get; set; } = 10000;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
}
