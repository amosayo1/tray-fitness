using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Domain.Entities;
using GymSync.Domain.Enums;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Workouts.Commands;

public record JoinWorkoutCommand : IRequest<Result>
{
    public Guid WorkoutId { get; init; }
}

public class JoinWorkoutCommandHandler : IRequestHandler<JoinWorkoutCommand, Result>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public JoinWorkoutCommandHandler(
        IApplicationDbContext context,
        ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result> Handle(JoinWorkoutCommand request, CancellationToken ct)
    {
        var workout = await _context.Workouts
            .FirstOrDefaultAsync(w => w.Id == request.WorkoutId, ct);

        if (workout is null)
            return Result.Failure("Workout not found");

        if (workout.UserId == _currentUser.UserId)
            return Result.Failure("Cannot join your own workout");

        var session = new WorkoutSession
        {
            Id = Guid.NewGuid(),
            WorkoutId = workout.Id,
            UserId = _currentUser.UserId,
            PartnerId = workout.UserId,
            JoinedAt = DateTime.UtcNow,
            IsActive = true
        };

        _context.WorkoutSessions.Add(session);

        var user = await _context.Users
            .FirstAsync(u => u.Id == _currentUser.UserId, ct);
        user.Status = UserStatus.WorkingOut;
        user.LastActiveAt = DateTime.UtcNow;

        await _context.SaveChangesAsync(ct);
        return Result.Success();
    }
}
