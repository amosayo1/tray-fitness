using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Admin.Commands;

public record ResetUserPasswordCommand : IRequest<Result<string>>
{
    public Guid UserId { get; init; }
    public string NewPassword { get; init; } = string.Empty;
}

public class ResetUserPasswordCommandHandler : IRequestHandler<ResetUserPasswordCommand, Result<string>>
{
    private readonly IApplicationDbContext _context;
    private readonly IPasswordService _passwordService;
    private readonly ICurrentUserService _currentUser;

    public ResetUserPasswordCommandHandler(
        IApplicationDbContext context,
        IPasswordService passwordService,
        ICurrentUserService currentUser)
    {
        _context = context;
        _passwordService = passwordService;
        _currentUser = currentUser;
    }

    public async Task<Result<string>> Handle(ResetUserPasswordCommand request, CancellationToken ct)
    {
        if (!_currentUser.IsAdmin)
            return Result.Failure<string>("Only admin can reset passwords");

        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Id == request.UserId, ct);

        if (user is null)
            return Result.Failure<string>("User not found");

        user.PasswordHash = _passwordService.HashPassword(request.NewPassword);
        user.UpdatedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync(ct);
        return Result.Success("Password reset successfully");
    }
}
