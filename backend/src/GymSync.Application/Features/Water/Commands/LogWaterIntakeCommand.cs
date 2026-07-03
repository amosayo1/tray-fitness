using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Domain.Entities;
using MediatR;

namespace GymSync.Application.Features.Water.Commands;

public record LogWaterIntakeCommand : IRequest<Result<WaterIntakeDto>>
{
    public Guid? WorkoutId { get; init; }
    public int AmountMl { get; init; } = 250;
}

public record WaterIntakeDto
{
    public Guid Id { get; init; }
    public int AmountMl { get; init; }
    public DateTime TakenAt { get; init; }
}

public class LogWaterIntakeCommandHandler
    : IRequestHandler<LogWaterIntakeCommand, Result<WaterIntakeDto>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public LogWaterIntakeCommandHandler(IApplicationDbContext context, ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result<WaterIntakeDto>> Handle(LogWaterIntakeCommand request, CancellationToken ct)
    {
        var intake = new WaterIntake
        {
            Id = Guid.NewGuid(),
            UserId = _currentUser.UserId,
            WorkoutId = request.WorkoutId,
            AmountMl = request.AmountMl,
            TakenAt = DateTime.UtcNow
        };

        _context.WaterIntakes.Add(intake);
        await _context.SaveChangesAsync(ct);

        return Result.Success(new WaterIntakeDto
        {
            Id = intake.Id,
            AmountMl = intake.AmountMl,
            TakenAt = intake.TakenAt
        });
    }
}
