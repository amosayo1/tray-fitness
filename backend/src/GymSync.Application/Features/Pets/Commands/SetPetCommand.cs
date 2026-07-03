using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Application.Features.Pets.Queries;
using GymSync.Domain.Entities;
using GymSync.Domain.Enums;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Pets.Commands;

public record SetPetCommand : IRequest<Result<PetDto>>
{
    public string Name { get; init; } = string.Empty;
    public PetType Type { get; init; }
    public string? Color { get; init; }
}

public class SetPetCommandHandler : IRequestHandler<SetPetCommand, Result<PetDto>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public SetPetCommandHandler(IApplicationDbContext context, ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result<PetDto>> Handle(SetPetCommand request, CancellationToken ct)
    {
        var existing = await _context.Pets
            .FirstOrDefaultAsync(p => p.UserId == _currentUser.UserId, ct);

        if (existing is not null)
        {
            existing.Name = request.Name;
            existing.Type = request.Type;
            existing.Color = request.Color;
            existing.UpdatedAt = DateTime.UtcNow;
        }
        else
        {
            existing = new Pet
            {
                Id = Guid.NewGuid(),
                UserId = _currentUser.UserId,
                Name = request.Name,
                Type = request.Type,
                Color = request.Color
            };
            _context.Pets.Add(existing);
        }

        await _context.SaveChangesAsync(ct);

        return Result.Success(new PetDto
        {
            Id = existing.Id,
            Name = existing.Name,
            Type = existing.Type,
            Color = existing.Color
        });
    }
}
