namespace GymSync.Domain.Entities;

public class InviteToken
{
    public Guid Id { get; set; }
    public Guid CreatedByUserId { get; set; }
    public User CreatedByUser { get; set; } = null!;
    public string Token { get; set; } = string.Empty;
    public bool IsUsed { get; set; }
    public DateTime ExpiresAt { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? UsedAt { get; set; }

    public bool IsValid => !IsUsed && DateTime.UtcNow < ExpiresAt;
}
