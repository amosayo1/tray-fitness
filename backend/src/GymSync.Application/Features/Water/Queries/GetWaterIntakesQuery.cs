using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Application.Features.Water.Commands;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Water.Queries;

public record GetWaterIntakesQuery : IRequest<Result<List<WaterIntakeDto>>>
{
    public Guid? WorkoutId { get; init; }
    public int LastHours { get; init; } = 24;
}

public class GetWaterIntakesQueryHandler
    : IRequestHandler<GetWaterIntakesQuery, Result<List<WaterIntakeDto>>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public GetWaterIntakesQueryHandler(IApplicationDbContext context, ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result<List<WaterIntakeDto>>> Handle(
        GetWaterIntakesQuery request, CancellationToken ct)
    {
        var query = _context.WaterIntakes
            .Where(w => w.UserId == _currentUser.UserId);

        if (request.WorkoutId.HasValue)
            query = query.Where(w => w.WorkoutId == request.WorkoutId);

        var since = DateTime.UtcNow.AddHours(-request.LastHours);
        query = query.Where(w => w.TakenAt >= since);

        var intakes = await query
            .OrderByDescending(w => w.TakenAt)
            .Select(w => new WaterIntakeDto
            {
                Id = w.Id,
                AmountMl = w.AmountMl,
                TakenAt = w.TakenAt
            })
            .ToListAsync(ct);

        return Result.Success(intakes);
    }
}
