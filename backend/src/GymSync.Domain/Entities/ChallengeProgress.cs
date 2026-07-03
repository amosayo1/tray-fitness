namespace GymSync.Domain.Entities;

public class ChallengeProgress
{
    public Guid Id { get; set; }
    public Guid ChallengeId { get; set; }
    public Challenge Challenge { get; set; } = null!;
    public Guid UserId { get; set; }
    public User User { get; set; } = null!;
    public decimal Progress { get; set; }
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
}
