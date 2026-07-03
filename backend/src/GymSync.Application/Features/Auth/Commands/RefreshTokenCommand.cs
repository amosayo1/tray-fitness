using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Auth.Commands;

public record RefreshTokenCommand : IRequest<Result<TokenResult>>
{
    public string RefreshToken { get; init; } = string.Empty;
}

public class RefreshTokenCommandHandler : IRequestHandler<RefreshTokenCommand, Result<TokenResult>>
{
    private readonly IApplicationDbContext _context;
    private readonly ITokenService _tokenService;

    public RefreshTokenCommandHandler(
        IApplicationDbContext context,
        ITokenService tokenService)
    {
        _context = context;
        _tokenService = tokenService;
    }

    public async Task<Result<TokenResult>> Handle(RefreshTokenCommand request, CancellationToken ct)
    {
        var storedToken = await _context.RefreshTokens
            .Include(t => t.User)
            .FirstOrDefaultAsync(t => t.Token == request.RefreshToken && t.IsActive, ct);

        if (storedToken is null)
            return Result.Failure<TokenResult>("Invalid or expired refresh token");

        storedToken.IsRevoked = true;
        storedToken.RevokedAt = DateTime.UtcNow;

        var accessToken = _tokenService.GenerateAccessToken(
            storedToken.User.Id, storedToken.User.Username, storedToken.User.IsAdmin);
        var newRefreshToken = _tokenService.GenerateRefreshToken();

        _context.RefreshTokens.Add(new Domain.Entities.RefreshToken
        {
            Id = Guid.NewGuid(),
            UserId = storedToken.UserId,
            Token = newRefreshToken,
            ExpiresAt = DateTime.UtcNow.AddDays(30),
            CreatedAt = DateTime.UtcNow
        });

        await _context.SaveChangesAsync(ct);

        return Result.Success(new TokenResult
        {
            AccessToken = accessToken,
            RefreshToken = newRefreshToken,
            ExpiresAt = DateTime.UtcNow.AddMinutes(60)
        });
    }
}
