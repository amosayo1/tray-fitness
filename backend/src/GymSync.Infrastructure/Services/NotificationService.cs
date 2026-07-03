using GymSync.Application.Common.Interfaces;
using GymSync.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace GymSync.Infrastructure.Services;

public class NotificationService : INotificationService
{
    private readonly IApplicationDbContext _context;
    private readonly ILogger<NotificationService> _logger;

    public NotificationService(
        IApplicationDbContext context,
        ILogger<NotificationService> logger)
    {
        _context = context;
        _logger = logger;
    }

    public async Task SendPushNotificationAsync(Guid userId, string title, string body, object? data = null)
    {
        try
        {
            var notification = new Notification
            {
                Id = Guid.NewGuid(),
                UserId = userId,
                Title = title,
                Body = body,
                Data = System.Text.Json.JsonSerializer.Serialize(data),
                CreatedAt = DateTime.UtcNow
            };

            _context.Notifications.Add(notification);
            await _context.SaveChangesAsync();

            // FCM sending would be integrated here
            _logger.LogInformation("Notification sent to user {UserId}: {Title} - {Body}", userId, title, body);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to send notification to user {UserId}", userId);
        }
    }

    public async Task SendPushNotificationToPartnerAsync(Guid userId, string title, string body, object? data = null)
    {
        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Id == userId);

        if (user?.PartnerId is null)
            return;

        await SendPushNotificationAsync(user.PartnerId.Value, title, body, data);
    }
}
