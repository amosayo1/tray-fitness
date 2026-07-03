using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Auth.Commands;

public record LoginCommand : IRequest<Result<TokenResult>>
{
    public string Username { get; init; } = string.Empty;
    public string Password { get; init; } = string.Empty;
}

public class LoginCommandHandler : IRequestHandler<LoginCommand, Result<TokenResult>>
{
    private readonly IApplicationDbContext _context;
    private readonly IPasswordService _passwordService;
    private readonly ITokenService _tokenService;

    public LoginCommandHandler(
        IApplicationDbContext context,
        IPasswordService passwordService,
        ITokenService tokenService)
    {
        _context = context;
        _passwordService = passwordService;
        _tokenService = tokenService;
    }

    public async Task<Result<TokenResult>> Handle(LoginCommand request, CancellationToken ct)
    {
        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Username == request.Username, ct);

        if (user is null || !_passwordService.VerifyPassword(request.Password, user.PasswordHash))
            return Result.Failure<TokenResult>("Invalid username or password");

        user.Status = Domain.Enums.UserStatus.Online;
        user.LastActiveAt = DateTime.UtcNow;

        var accessToken = _tokenService.GenerateAccessToken(user.Id, user.Username, user.IsAdmin);
        var refreshToken = _tokenService.GenerateRefreshToken();

        _context.RefreshTokens.Add(new Domain.Entities.RefreshToken
        {
            Id = Guid.NewGuid(),
            UserId = user.Id,
            Token = refreshToken,
            ExpiresAt = DateTime.UtcNow.AddDays(30),
            CreatedAt = DateTime.UtcNow
        });

        await _context.SaveChangesAsync(ct);

        return Result.Success(new TokenResult
        {
            AccessToken = accessToken,
            RefreshToken = refreshToken,
            ExpiresAt = DateTime.UtcNow.AddMinutes(60)
        });
    }
}
