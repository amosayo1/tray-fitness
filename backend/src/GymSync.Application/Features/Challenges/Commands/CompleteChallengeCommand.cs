using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Challenges.Commands;

public record UpdateChallengeProgressCommand : IRequest<Result>
{
    public Guid ChallengeId { get; init; }
    public decimal Progress { get; init; }
}

public class UpdateChallengeProgressCommandHandler : IRequestHandler<UpdateChallengeProgressCommand, Result>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public UpdateChallengeProgressCommandHandler(
        IApplicationDbContext context,
        ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result> Handle(UpdateChallengeProgressCommand request, CancellationToken ct)
    {
        var challenge = await _context.Challenges
            .FirstOrDefaultAsync(c => c.Id == request.ChallengeId && c.IsActive, ct);

        if (challenge is null)
            return Result.Failure("Challenge not found or not active");

        var progress = await _context.ChallengeProgresses
            .FirstOrDefaultAsync(p => p.ChallengeId == request.ChallengeId && p.UserId == _currentUser.UserId, ct);

        if (progress is null)
        {
            progress = new Domain.Entities.ChallengeProgress
            {
                Id = Guid.NewGuid(),
                ChallengeId = request.ChallengeId,
                UserId = _currentUser.UserId,
                Progress = request.Progress,
                UpdatedAt = DateTime.UtcNow
            };
            _context.ChallengeProgresses.Add(progress);
        }
        else
        {
            progress.Progress = request.Progress;
            progress.UpdatedAt = DateTime.UtcNow;
        }

        await _context.SaveChangesAsync(ct);
        return Result.Success();
    }
}
