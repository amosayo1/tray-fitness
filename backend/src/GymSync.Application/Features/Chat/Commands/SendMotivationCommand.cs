using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Domain.Entities;
using GymSync.Domain.Enums;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Chat.Commands;

public record SendMotivationCommand : IRequest<Result>
{
    public MotivationType Type { get; init; }
    public string? CustomMessage { get; init; }
}

public class SendMotivationCommandHandler : IRequestHandler<SendMotivationCommand, Result>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;
    private readonly INotificationService _notificationService;

    public SendMotivationCommandHandler(
        IApplicationDbContext context,
        ICurrentUserService currentUser,
        INotificationService notificationService)
    {
        _context = context;
        _currentUser = currentUser;
        _notificationService = notificationService;
    }

    public async Task<Result> Handle(SendMotivationCommand request, CancellationToken ct)
    {
        var user = await _context.Users
            .FirstAsync(u => u.Id == _currentUser.UserId, ct);

        if (!user.PartnerId.HasValue)
            return Result.Failure("No partner linked");

        var motivation = new Motivation
        {
            Id = Guid.NewGuid(),
            SenderId = _currentUser.UserId,
            ReceiverId = user.PartnerId.Value,
            Type = request.Type,
            CustomMessage = request.CustomMessage,
            SentAt = DateTime.UtcNow
        };

        _context.Motivations.Add(motivation);
        await _context.SaveChangesAsync(ct);

        var label = request.Type switch
        {
            MotivationType.Fire => "sent you Fire! 🔥",
            MotivationType.Respect => "shows Respect! 🤝",
            MotivationType.KeepGoing => "Keep Going! 💪",
            MotivationType.Legend => "called you a Legend! ⭐",
            MotivationType.NiceLift => "Nice Lift! 🏋️",
            MotivationType.LetsGo => "Let's Go! 🚀",
            MotivationType.Custom => request.CustomMessage ?? "sent motivation!",
            _ => "sent motivation!"
        };

        await _notificationService.SendPushNotificationToPartnerAsync(
            _currentUser.UserId,
            $"Motivation from {user.DisplayName}",
            label,
            new { type = "motivation", motivationType = request.Type.ToString() });

        return Result.Success();
    }
}
