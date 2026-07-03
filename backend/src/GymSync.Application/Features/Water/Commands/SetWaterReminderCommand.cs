using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Domain.Entities;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Water.Commands;

public record SetWaterReminderCommand : IRequest<Result<WaterReminderDto>>
{
    public Guid? WorkoutId { get; init; }
    public int IntervalMinutes { get; init; } = 15;
    public bool IsActive { get; init; } = true;
}

public record WaterReminderDto
{
    public Guid Id { get; init; }
    public int IntervalMinutes { get; init; }
    public bool IsActive { get; init; }
}

public class SetWaterReminderCommandHandler
    : IRequestHandler<SetWaterReminderCommand, Result<WaterReminderDto>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public SetWaterReminderCommandHandler(IApplicationDbContext context, ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result<WaterReminderDto>> Handle(SetWaterReminderCommand request, CancellationToken ct)
    {
        var existing = await _context.WaterReminders
            .FirstOrDefaultAsync(r => r.UserId == _currentUser.UserId && r.WorkoutId == request.WorkoutId, ct);

        if (existing is not null)
        {
            existing.IntervalMinutes = request.IntervalMinutes;
            existing.IsActive = request.IsActive;
            existing.UpdatedAt = DateTime.UtcNow;
        }
        else
        {
            existing = new WaterReminder
            {
                Id = Guid.NewGuid(),
                UserId = _currentUser.UserId,
                WorkoutId = request.WorkoutId,
                IntervalMinutes = request.IntervalMinutes,
                IsActive = request.IsActive
            };
            _context.WaterReminders.Add(existing);
        }

        await _context.SaveChangesAsync(ct);

        return Result.Success(new WaterReminderDto
        {
            Id = existing.Id,
            IntervalMinutes = existing.IntervalMinutes,
            IsActive = existing.IsActive
        });
    }
}
