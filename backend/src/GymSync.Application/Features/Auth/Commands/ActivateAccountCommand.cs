using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Auth.Commands;

public record ActivateAccountCommand : IRequest<Result<TokenResult>>
{
    public string InviteToken { get; init; } = string.Empty;
    public string Username { get; init; } = string.Empty;
    public string DisplayName { get; init; } = string.Empty;
    public string Password { get; init; } = string.Empty;
    public string Email { get; init; } = string.Empty;
    public string PetName { get; init; } = string.Empty;
    public Domain.Enums.PetType PetType { get; init; }
    public string? PetColor { get; init; }
}

public class ActivateAccountCommandHandler : IRequestHandler<ActivateAccountCommand, Result<TokenResult>>
{
    private readonly IApplicationDbContext _context;
    private readonly IPasswordService _passwordService;
    private readonly ITokenService _tokenService;
    private readonly ICurrentUserService _currentUser;

    public ActivateAccountCommandHandler(
        IApplicationDbContext context,
        IPasswordService passwordService,
        ITokenService tokenService,
        ICurrentUserService currentUser)
    {
        _context = context;
        _passwordService = passwordService;
        _tokenService = tokenService;
        _currentUser = currentUser;
    }

    public async Task<Result<TokenResult>> Handle(ActivateAccountCommand request, CancellationToken ct)
    {
        var inviteToken = await _context.InviteTokens
            .FirstOrDefaultAsync(t => t.Token == request.InviteToken && t.IsValid, ct);

        if (inviteToken is null)
            return Result.Failure<TokenResult>("Invalid or expired invite token");

        var adminUser = await _context.Users
            .FirstAsync(u => u.Id == inviteToken.CreatedByUserId, ct);

        var user = new Domain.Entities.User
        {
            Id = Guid.NewGuid(),
            Username = request.Username,
            DisplayName = request.DisplayName,
            Email = request.Email,
            PasswordHash = _passwordService.HashPassword(request.Password),
            Status = Domain.Enums.UserStatus.Online,
            IsAdmin = false,
            PartnerId = adminUser.Id
        };

        adminUser.PartnerId = user.Id;
        adminUser.IsPartnerLinked = true;

        inviteToken.IsUsed = true;
        inviteToken.UsedAt = DateTime.UtcNow;

        _context.Users.Add(user);

        if (!string.IsNullOrWhiteSpace(request.PetName))
        {
            _context.Pets.Add(new Domain.Entities.Pet
            {
                Id = Guid.NewGuid(),
                UserId = user.Id,
                Name = request.PetName,
                Type = request.PetType,
                Color = request.PetColor
            });
        }

        await _context.SaveChangesAsync(ct);

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
