using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Domain.Entities;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Auth.Commands;

public record GenerateInviteTokenCommand : IRequest<Result<string>>;

public class GenerateInviteTokenCommandHandler : IRequestHandler<GenerateInviteTokenCommand, Result<string>>
{
    private readonly IApplicationDbContext _context;
    private readonly ITokenService _tokenService;
    private readonly ICurrentUserService _currentUser;

    public GenerateInviteTokenCommandHandler(
        IApplicationDbContext context,
        ITokenService tokenService,
        ICurrentUserService currentUser)
    {
        _context = context;
        _tokenService = tokenService;
        _currentUser = currentUser;
    }

    public async Task<Result<string>> Handle(GenerateInviteTokenCommand request, CancellationToken ct)
    {
        if (!_currentUser.IsAdmin)
            return Result.Failure<string>("Only admin can generate invite tokens");

        var existingValid = await _context.InviteTokens
            .AnyAsync(t => !t.IsUsed && t.ExpiresAt > DateTime.UtcNow, ct);

        if (existingValid)
            return Result.Failure<string>("A valid invite token already exists. Use it before generating a new one.");

        var token = _tokenService.GenerateInviteToken();
        var inviteToken = new InviteToken
        {
            Id = Guid.NewGuid(),
            CreatedByUserId = _currentUser.UserId,
            Token = token,
            ExpiresAt = DateTime.UtcNow.AddDays(7),
            CreatedAt = DateTime.UtcNow
        };

        _context.InviteTokens.Add(inviteToken);
        await _context.SaveChangesAsync(ct);

        return Result.Success(token);
    }
}
