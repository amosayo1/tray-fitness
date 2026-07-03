using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Workouts.Commands;

public record StartRestTimerCommand : IRequest<Result>
{
    public Guid WorkoutId { get; init; }
    public int DurationSeconds { get; init; }
}

public class StartRestTimerCommandHandler : IRequestHandler<StartRestTimerCommand, Result>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public StartRestTimerCommandHandler(
        IApplicationDbContext context,
        ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result> Handle(StartRestTimerCommand request, CancellationToken ct)
    {
        var existing = await _context.RestTimers
            .FirstOrDefaultAsync(r => r.WorkoutId == request.WorkoutId && r.IsRunning, ct);

        if (existing is not null)
        {
            existing.IsRunning = false;
        }

        var timer = new Domain.Entities.RestTimer
        {
            Id = Guid.NewGuid(),
            WorkoutId = request.WorkoutId,
            UserId = _currentUser.UserId,
            DurationSeconds = request.DurationSeconds,
            RemainingSeconds = request.DurationSeconds,
            IsRunning = true,
            StartedAt = DateTime.UtcNow
        };

        _context.RestTimers.Add(timer);
        await _context.SaveChangesAsync(ct);

        return Result.Success();
    }
}
