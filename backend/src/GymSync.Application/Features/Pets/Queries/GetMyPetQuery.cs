using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Domain.Enums;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Pets.Queries;

public record GetMyPetQuery : IRequest<Result<PetDto>>;

public record PetDto
{
    public Guid Id { get; init; }
    public string Name { get; init; } = string.Empty;
    public PetType Type { get; init; }
    public string? Color { get; init; }
}

public class GetMyPetQueryHandler : IRequestHandler<GetMyPetQuery, Result<PetDto>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public GetMyPetQueryHandler(IApplicationDbContext context, ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result<PetDto>> Handle(GetMyPetQuery request, CancellationToken ct)
    {
        var pet = await _context.Pets
            .FirstOrDefaultAsync(p => p.UserId == _currentUser.UserId, ct);

        if (pet is null)
            return Result.Failure<PetDto>("No pet set yet. Activate your account to choose one.");

        return Result.Success(new PetDto
        {
            Id = pet.Id,
            Name = pet.Name,
            Type = pet.Type,
            Color = pet.Color
        });
    }
}
