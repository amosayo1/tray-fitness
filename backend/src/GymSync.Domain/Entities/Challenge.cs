using GymSync.Domain.Enums;

namespace GymSync.Domain.Entities;

public class Challenge
{
    public Guid Id { get; set; }
    public ChallengeType Type { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public string Goal { get; set; } = string.Empty;
    public DateTime StartsAt { get; set; }
    public DateTime EndsAt { get; set; }
    public bool IsActive { get; set; } = true;
    public Guid? WinnerId { get; set; }
    public User? Winner { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public ICollection<ChallengeProgress> ProgressEntries { get; set; } = new List<ChallengeProgress>();
}
