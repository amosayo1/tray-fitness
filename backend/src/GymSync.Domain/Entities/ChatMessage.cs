namespace GymSync.Domain.Entities;

public class ChatMessage
{
    public Guid Id { get; set; }
    public Guid SenderId { get; set; }
    public User Sender { get; set; } = null!;
    public Guid ReceiverId { get; set; }
    public User Receiver { get; set; } = null!;
    public string? TextContent { get; set; }
    public string? ImageUrl { get; set; }
    public string? VoiceNoteUrl { get; set; }
    public int? VoiceNoteDurationSeconds { get; set; }
    public bool IsRead { get; set; }
    public bool IsDelivered { get; set; }
    public DateTime SentAt { get; set; } = DateTime.UtcNow;
    public DateTime? DeliveredAt { get; set; }
    public DateTime? ReadAt { get; set; }
}
