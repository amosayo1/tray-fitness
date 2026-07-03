using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Domain.Entities;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Steps.Commands;

public record LogStepsCommand : IRequest<Result<DailyStepLogDto>>
{
    public DateOnly Date { get; init; }
    public int Steps { get; init; }
}

public record DailyStepLogDto
{
    public Guid Id { get; init; }
    public DateOnly Date { get; init; }
    public int Steps { get; init; }
    public int Target { get; init; }
    public int Remaining => Math.Max(0, Target - Steps);
    public double PercentComplete => Target > 0 ? Math.Round((double)Steps / Target * 100, 1) : 0;
}

public class LogStepsCommandHandler : IRequestHandler<LogStepsCommand, Result<DailyStepLogDto>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public LogStepsCommandHandler(IApplicationDbContext context, ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result<DailyStepLogDto>> Handle(LogStepsCommand request, CancellationToken ct)
    {
        var existing = await _context.DailyStepLogs
            .FirstOrDefaultAsync(s => s.UserId == _currentUser.UserId && s.Date == request.Date, ct);

        if (existing is not null)
        {
            existing.Steps = request.Steps;
            existing.UpdatedAt = DateTime.UtcNow;
        }
        else
        {
            existing = new DailyStepLog
            {
                Id = Guid.NewGuid(),
                UserId = _currentUser.UserId,
                Date = request.Date,
                Steps = request.Steps,
                Target = 10000,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };
            _context.DailyStepLogs.Add(existing);
        }

        await _context.SaveChangesAsync(ct);

        return Result.Success(new DailyStepLogDto
        {
            Id = existing.Id,
            Date = existing.Date,
            Steps = existing.Steps,
            Target = existing.Target
        });
    }
}
