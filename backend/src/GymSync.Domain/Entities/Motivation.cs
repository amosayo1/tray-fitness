using GymSync.Domain.Enums;

namespace GymSync.Domain.Entities;

public class Motivation
{
    public Guid Id { get; set; }
    public Guid SenderId { get; set; }
    public User Sender { get; set; } = null!;
    public Guid ReceiverId { get; set; }
    public User Receiver { get; set; } = null!;
    public MotivationType Type { get; set; }
    public string? CustomMessage { get; set; }
    public DateTime SentAt { get; set; } = DateTime.UtcNow;
}
