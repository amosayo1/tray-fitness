namespace GymSync.Application.Common.Interfaces;

public interface INotificationService
{
    Task SendPushNotificationAsync(Guid userId, string title, string body, object? data = null);
    Task SendPushNotificationToPartnerAsync(Guid userId, string title, string body, object? data = null);
}
