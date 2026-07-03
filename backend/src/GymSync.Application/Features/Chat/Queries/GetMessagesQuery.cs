using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Chat.Queries;

public record GetMessagesQuery : IRequest<Result<List<MessageDto>>>
{
    public int Page { get; init; } = 1;
    public int PageSize { get; init; } = 50;
}

public record MessageDto
{
    public Guid Id { get; init; }
    public Guid SenderId { get; init; }
    public string SenderName { get; init; } = string.Empty;
    public string? TextContent { get; init; }
    public string? ImageUrl { get; init; }
    public string? VoiceNoteUrl { get; init; }
    public int? VoiceNoteDurationSeconds { get; init; }
    public bool IsRead { get; init; }
    public bool IsDelivered { get; init; }
    public DateTime SentAt { get; init; }
}

public class GetMessagesQueryHandler : IRequestHandler<GetMessagesQuery, Result<List<MessageDto>>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public GetMessagesQueryHandler(
        IApplicationDbContext context,
        ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result<List<MessageDto>>> Handle(GetMessagesQuery request, CancellationToken ct)
    {
        var user = await _context.Users
            .FirstAsync(u => u.Id == _currentUser.UserId, ct);

        var messages = await _context.ChatMessages
            .Include(m => m.Sender)
            .Where(m => (m.SenderId == _currentUser.UserId && m.ReceiverId == user.PartnerId) ||
                        (m.SenderId == user.PartnerId && m.ReceiverId == _currentUser.UserId))
            .OrderByDescending(m => m.SentAt)
            .Skip((request.Page - 1) * request.PageSize)
            .Take(request.PageSize)
            .Select(m => new MessageDto
            {
                Id = m.Id,
                SenderId = m.SenderId,
                SenderName = m.Sender.DisplayName,
                TextContent = m.TextContent,
                ImageUrl = m.ImageUrl,
                VoiceNoteUrl = m.VoiceNoteUrl,
                VoiceNoteDurationSeconds = m.VoiceNoteDurationSeconds,
                IsRead = m.IsRead,
                IsDelivered = m.IsDelivered,
                SentAt = m.SentAt
            })
            .ToListAsync(ct);

        return Result.Success(messages);
    }
}
