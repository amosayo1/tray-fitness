using GymSync.Application.Common.Interfaces;
using GymSync.Domain.Enums;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.SignalR;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace GymSync.Infrastructure.SignalR;

[Authorize]
public class WorkoutHub : Hub
{
    private readonly IApplicationDbContext _context;
    private readonly ILogger<WorkoutHub> _logger;

    public WorkoutHub(
        IApplicationDbContext context,
        ILogger<WorkoutHub> logger)
    {
        _context = context;
        _logger = logger;
    }

    public override async Task OnConnectedAsync()
    {
        var userId = GetUserId();
        if (userId.HasValue)
        {
            await Groups.AddToGroupAsync(Context.ConnectionId, $"user_{userId.Value}");
            await UpdateUserStatus(userId.Value, UserStatus.Online);
        }
        await base.OnConnectedAsync();
    }

    public override async Task OnDisconnectedAsync(Exception? exception)
    {
        var userId = GetUserId();
        if (userId.HasValue)
        {
            await UpdateUserStatus(userId.Value, UserStatus.Offline);
            await Groups.RemoveFromGroupAsync(Context.ConnectionId, $"user_{userId.Value}");
        }
        await base.OnDisconnectedAsync(exception);
    }

    public async Task JoinWorkoutRoom(Guid workoutId)
    {
        await Groups.AddToGroupAsync(Context.ConnectionId, $"workout_{workoutId}");
        _logger.LogInformation("User {UserId} joined workout room {WorkoutId}", GetUserId(), workoutId);
    }

    public async Task LeaveWorkoutRoom(Guid workoutId)
    {
        await Groups.RemoveFromGroupAsync(Context.ConnectionId, $"workout_{workoutId}");
    }

    public async Task SetStatus(string status)
    {
        var userId = GetUserId();
        if (!userId.HasValue) return;

        if (Enum.TryParse<UserStatus>(status, true, out var userStatus))
        {
            await UpdateUserStatus(userId.Value, userStatus);
        }
    }

    public async Task SetTyping(bool isTyping)
    {
        var userId = GetUserId();
        if (!userId.HasValue) return;

        await UpdateUserStatus(userId.Value, isTyping ? UserStatus.Typing : UserStatus.Online);

        var user = await _context.Users.FindAsync(userId.Value);
        if (user?.PartnerId.HasValue == true)
        {
            await Clients.Group($"user_{user.PartnerId.Value}")
                .SendAsync("PartnerTyping", userId.Value, isTyping);
        }
    }

    public async Task SetInChat(bool inChat)
    {
        var userId = GetUserId();
        if (!userId.HasValue) return;

        await UpdateUserStatus(userId.Value, inChat ? UserStatus.InChat : UserStatus.Online);
    }

    public async Task SendMessage(Guid receiverId, Guid messageId, string preview)
    {
        await Clients.Group($"user_{receiverId}")
            .SendAsync("NewMessage", GetUserId(), messageId, preview);
    }

    public async Task MarkDelivered(Guid messageId)
    {
        var message = await _context.ChatMessages.FindAsync(messageId);
        if (message is null) return;

        message.IsDelivered = true;
        message.DeliveredAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        await Clients.Group($"user_{message.SenderId}")
            .SendAsync("MessageDelivered", messageId);
    }

    public async Task MarkRead(Guid messageId)
    {
        var message = await _context.ChatMessages.FindAsync(messageId);
        if (message is null) return;

        message.IsRead = true;
        message.ReadAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        await Clients.Group($"user_{message.SenderId}")
            .SendAsync("MessageRead", messageId);
    }

    public async Task NotifySetCompleted(Guid workoutId, string exerciseName, int setNumber, bool isPr)
    {
        await Clients.Group($"workout_{workoutId}")
            .SendAsync("SetCompleted", GetUserId(), exerciseName, setNumber, isPr);
    }

    public async Task NotifyRestTimerUpdate(Guid workoutId, int remainingSeconds, bool isRunning)
    {
        await Clients.Group($"workout_{workoutId}")
            .SendAsync("RestTimerUpdate", GetUserId(), remainingSeconds, isRunning);
    }

    public async Task NotifyWorkoutStarted(Guid workoutId, string workoutName)
    {
        var userId = GetUserId();
        if (!userId.HasValue) return;

        var user = await _context.Users.FindAsync(userId.Value);
        if (user?.PartnerId.HasValue == true)
        {
            await Clients.Group($"user_{user.PartnerId.Value}")
                .SendAsync("PartnerWorkoutStarted", userId.Value, workoutId, workoutName);
        }
    }

    public async Task NotifyWorkoutFinished(Guid workoutId)
    {
        var userId = GetUserId();
        if (!userId.HasValue) return;

        var user = await _context.Users.FindAsync(userId.Value);
        if (user?.PartnerId.HasValue == true)
        {
            await Clients.Group($"user_{user.PartnerId.Value}")
                .SendAsync("PartnerWorkoutFinished", userId.Value, workoutId);
        }
    }

    public async Task SendMotivation(Guid receiverId, string type, string? message)
    {
        await Clients.Group($"user_{receiverId}")
            .SendAsync("MotivationReceived", GetUserId(), type, message);
    }

    private async Task UpdateUserStatus(Guid userId, UserStatus status)
    {
        var user = await _context.Users.FindAsync(userId);
        if (user is null) return;

        user.Status = status;
        user.LastActiveAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        if (user.PartnerId.HasValue)
        {
            await Clients.Group($"user_{user.PartnerId.Value}")
                .SendAsync("PartnerStatusChanged", userId, status.ToString());
        }
    }

    public async Task PetWaterReminder(Guid partnerId, string petName, string petType)
    {
        await Clients.Group($"user_{partnerId}")
            .SendAsync("PetBringsWater", GetUserId(), petName, petType);
    }

    public async Task PetRestAnimation(Guid workoutId, string petName, string petType)
    {
        await Clients.Group($"workout_{workoutId}")
            .SendAsync("PetResting", GetUserId(), petName, petType);
    }

    public async Task PetWaterTaken(Guid partnerId, string petName, string petType)
    {
        await Clients.Group($"user_{partnerId}")
            .SendAsync("PetWaterDrank", GetUserId(), petName, petType);
    }

    private Guid? GetUserId()
    {
        var claim = Context.User?.FindFirst(
            System.Security.Claims.ClaimTypes.NameIdentifier);
        return claim is not null && Guid.TryParse(claim.Value, out var id) ? id : null;
    }
}
