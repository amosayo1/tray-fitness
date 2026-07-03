using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Domain.Entities;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Chat.Commands;

public record SendMessageCommand : IRequest<Result<MessageResponse>>
{
    public string? TextContent { get; init; }
    public string? ImageBase64 { get; init; }
    public string? ImageContentType { get; init; }
    public byte[]? VoiceNoteData { get; init; }
    public int? VoiceNoteDurationSeconds { get; init; }
}

public record MessageResponse
{
    public Guid Id { get; init; }
    public Guid SenderId { get; init; }
    public string SenderName { get; init; } = string.Empty;
    public string? TextContent { get; init; }
    public string? ImageUrl { get; init; }
    public string? VoiceNoteUrl { get; init; }
    public DateTime SentAt { get; init; }
}

public class SendMessageCommandHandler : IRequestHandler<SendMessageCommand, Result<MessageResponse>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;
    private readonly IFileStorageService _fileStorage;
    private readonly INotificationService _notificationService;

    public SendMessageCommandHandler(
        IApplicationDbContext context,
        ICurrentUserService currentUser,
        IFileStorageService fileStorage,
        INotificationService notificationService)
    {
        _context = context;
        _currentUser = currentUser;
        _fileStorage = fileStorage;
        _notificationService = notificationService;
    }

    public async Task<Result<MessageResponse>> Handle(SendMessageCommand request, CancellationToken ct)
    {
        var user = await _context.Users
            .FirstAsync(u => u.Id == _currentUser.UserId, ct);

        if (!user.PartnerId.HasValue)
            return Result.Failure<MessageResponse>("No partner linked");

        string? imageUrl = null;
        string? voiceNoteUrl = null;

        if (!string.IsNullOrEmpty(request.ImageBase64))
        {
            var bytes = Convert.FromBase64String(request.ImageBase64);
            using var ms = new MemoryStream(bytes);
            imageUrl = await _fileStorage.UploadFileAsync(
                ms, $"chat_{Guid.NewGuid()}.{request.ImageContentType?.Split('/').LastOrDefault() ?? "jpg"}",
                request.ImageContentType ?? "image/jpeg", ct);
        }

        if (request.VoiceNoteData is not null)
        {
            using var ms = new MemoryStream(request.VoiceNoteData);
            voiceNoteUrl = await _fileStorage.UploadFileAsync(
                ms, $"voice_{Guid.NewGuid()}.ogg", "audio/ogg", ct);
        }

        var message = new ChatMessage
        {
            Id = Guid.NewGuid(),
            SenderId = _currentUser.UserId,
            ReceiverId = user.PartnerId.Value,
            TextContent = request.TextContent,
            ImageUrl = imageUrl,
            VoiceNoteUrl = voiceNoteUrl,
            VoiceNoteDurationSeconds = request.VoiceNoteDurationSeconds,
            SentAt = DateTime.UtcNow,
            IsDelivered = false
        };

        _context.ChatMessages.Add(message);
        await _context.SaveChangesAsync(ct);

        await _notificationService.SendPushNotificationToPartnerAsync(
            _currentUser.UserId,
            $"New message from {user.DisplayName}",
            request.TextContent ?? (imageUrl is not null ? "Sent an image" : "Sent a voice note"),
            new { messageId = message.Id, type = "chat_message" });

        return Result.Success(new MessageResponse
        {
            Id = message.Id,
            SenderId = message.SenderId,
            SenderName = user.DisplayName,
            TextContent = message.TextContent,
            ImageUrl = message.ImageUrl,
            VoiceNoteUrl = message.VoiceNoteUrl,
            SentAt = message.SentAt
        });
    }
}
