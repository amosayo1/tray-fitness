using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Domain.Enums;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Stats.Queries;

public record GetHomeDataQuery : IRequest<Result<HomeDataResponse>>;

public record HomeDataResponse
{
    public UserProfileDto MyProfile { get; init; } = null!;
    public UserProfileDto? PartnerProfile { get; init; }
    public int CurrentStreak { get; init; }
    public bool HasWorkoutToday { get; init; }
    public bool PartnerWorkoutToday { get; init; }
    public int CaloriesToday { get; init; }
    public TimeSpan WorkoutDurationToday { get; init; }
    public ChallengeDto? CurrentChallenge { get; init; }
    public int UnreadMessages { get; init; }
}

public record UserProfileDto
{
    public Guid Id { get; init; }
    public string DisplayName { get; init; } = string.Empty;
    public string? ProfilePhotoUrl { get; init; }
    public UserStatus Status { get; init; }
    public int CurrentStreak { get; init; }
    public int LongestStreak { get; init; }
    public DateTime? LastActiveAt { get; init; }
}

public record ChallengeDto
{
    public Guid Id { get; init; }
    public string Name { get; init; } = string.Empty;
    public string Goal { get; init; } = string.Empty;
    public decimal MyProgress { get; init; }
    public decimal PartnerProgress { get; init; }
    public DateTime EndsAt { get; init; }
}

public class GetHomeDataQueryHandler : IRequestHandler<GetHomeDataQuery, Result<HomeDataResponse>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public GetHomeDataQueryHandler(
        IApplicationDbContext context,
        ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result<HomeDataResponse>> Handle(GetHomeDataQuery request, CancellationToken ct)
    {
        var user = await _context.Users
            .FirstAsync(u => u.Id == _currentUser.UserId, ct);

        UserProfileDto? partnerProfile = null;
        if (user.PartnerId.HasValue)
        {
            var partner = await _context.Users
                .FirstAsync(u => u.Id == user.PartnerId.Value, ct);

            partnerProfile = new UserProfileDto
            {
                Id = partner.Id,
                DisplayName = partner.DisplayName,
                ProfilePhotoUrl = partner.ProfilePhotoUrl,
                Status = partner.Status,
                CurrentStreak = partner.CurrentStreak,
                LongestStreak = partner.LongestStreak,
                LastActiveAt = partner.LastActiveAt
            };
        }

        var todayStart = DateTime.UtcNow.Date;
        var todayWorkout = await _context.Workouts
            .Where(w => w.UserId == _currentUser.UserId && w.StartedAt >= todayStart)
            .ToListAsync(ct);

        var partnerWorkoutToday = false;
        if (user.PartnerId.HasValue)
        {
            partnerWorkoutToday = await _context.Workouts
                .AnyAsync(w => w.UserId == user.PartnerId.Value && w.StartedAt >= todayStart, ct);
        }

        var activeChallenge = await _context.Challenges
            .Where(c => c.IsActive && c.EndsAt > DateTime.UtcNow)
            .Select(c => new
            {
                Challenge = c,
                MyProgress = _context.ChallengeProgresses
                    .Where(p => p.ChallengeId == c.Id && p.UserId == _currentUser.UserId)
                    .Select(p => (decimal?)p.Progress)
                    .FirstOrDefault() ?? 0,
                PartnerProgress = _context.ChallengeProgresses
                    .Where(p => p.ChallengeId == c.Id && p.UserId == user.PartnerId)
                    .Select(p => (decimal?)p.Progress)
                    .FirstOrDefault() ?? 0
            })
            .FirstOrDefaultAsync(ct);

        var unreadMessages = await _context.ChatMessages
            .CountAsync(m => m.ReceiverId == _currentUser.UserId && !m.IsRead, ct);

        var response = new HomeDataResponse
        {
            MyProfile = new UserProfileDto
            {
                Id = user.Id,
                DisplayName = user.DisplayName,
                ProfilePhotoUrl = user.ProfilePhotoUrl,
                Status = user.Status,
                CurrentStreak = user.CurrentStreak,
                LongestStreak = user.LongestStreak,
                LastActiveAt = user.LastActiveAt
            },
            PartnerProfile = partnerProfile,
            CurrentStreak = user.CurrentStreak,
            HasWorkoutToday = todayWorkout.Any(),
            PartnerWorkoutToday = partnerWorkoutToday,
            CaloriesToday = todayWorkout.Sum(w => w.CaloriesBurned ?? 0),
            WorkoutDurationToday = TimeSpan.FromTicks(
                todayWorkout.Where(w => w.Duration.HasValue)
                    .Sum(w => w.Duration!.Value.Ticks)),
            CurrentChallenge = activeChallenge is not null ? new ChallengeDto
            {
                Id = activeChallenge.Challenge.Id,
                Name = activeChallenge.Challenge.Name,
                Goal = activeChallenge.Challenge.Goal,
                MyProgress = activeChallenge.MyProgress,
                PartnerProgress = activeChallenge.PartnerProgress,
                EndsAt = activeChallenge.Challenge.EndsAt
            } : null,
            UnreadMessages = unreadMessages
        };

        return Result.Success(response);
    }
}
