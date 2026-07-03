using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Application.Features.Steps.Commands;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Steps.Queries;

public record GetDailyStepsQuery : IRequest<Result<List<DailyStepLogDto>>>
{
    public int Days { get; init; } = 30;
}

public class GetDailyStepsQueryHandler
    : IRequestHandler<GetDailyStepsQuery, Result<List<DailyStepLogDto>>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public GetDailyStepsQueryHandler(IApplicationDbContext context, ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result<List<DailyStepLogDto>>> Handle(
        GetDailyStepsQuery request, CancellationToken ct)
    {
        var fromDate = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-request.Days));

        var logs = await _context.DailyStepLogs
            .Where(s => s.UserId == _currentUser.UserId && s.Date >= fromDate)
            .OrderByDescending(s => s.Date)
            .Select(s => new DailyStepLogDto
            {
                Id = s.Id,
                Date = s.Date,
                Steps = s.Steps,
                Target = s.Target
            })
            .ToListAsync(ct);

        return Result.Success(logs);
    }
}
