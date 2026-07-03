using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Auth.Commands;

public record ChangePasswordCommand : IRequest<Result>
{
    public string CurrentPassword { get; init; } = string.Empty;
    public string NewPassword { get; init; } = string.Empty;
}

public class ChangePasswordCommandHandler : IRequestHandler<ChangePasswordCommand, Result>
{
    private readonly IApplicationDbContext _context;
    private readonly IPasswordService _passwordService;
    private readonly ICurrentUserService _currentUser;

    public ChangePasswordCommandHandler(
        IApplicationDbContext context,
        IPasswordService passwordService,
        ICurrentUserService currentUser)
    {
        _context = context;
        _passwordService = passwordService;
        _currentUser = currentUser;
    }

    public async Task<Result> Handle(ChangePasswordCommand request, CancellationToken ct)
    {
        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Id == _currentUser.UserId, ct);

        if (user is null || !_passwordService.VerifyPassword(request.CurrentPassword, user.PasswordHash))
            return Result.Failure("Current password is incorrect");

        user.PasswordHash = _passwordService.HashPassword(request.NewPassword);
        user.UpdatedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync(ct);
        return Result.Success();
    }
}
