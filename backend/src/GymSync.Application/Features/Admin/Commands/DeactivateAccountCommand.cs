using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Admin.Commands;

public record DeactivateAccountCommand : IRequest<Result>
{
    public Guid UserId { get; init; }
}

public class DeactivateAccountCommandHandler : IRequestHandler<DeactivateAccountCommand, Result>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public DeactivateAccountCommandHandler(
        IApplicationDbContext context,
        ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result> Handle(DeactivateAccountCommand request, CancellationToken ct)
    {
        if (!_currentUser.IsAdmin)
            return Result.Failure("Only admin can deactivate accounts");

        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Id == request.UserId, ct);

        if (user is null)
            return Result.Failure("User not found");

        user.Status = Domain.Enums.UserStatus.Offline;
        user.PartnerId = null;

        var partner = await _context.Users
            .FirstOrDefaultAsync(u => u.PartnerId == user.Id, ct);

        if (partner is not null)
        {
            partner.PartnerId = null;
            partner.IsPartnerLinked = false;
        }

        await _context.SaveChangesAsync(ct);
        return Result.Success();
    }
}
