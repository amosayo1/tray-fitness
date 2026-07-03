using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using GymSync.Domain.Entities;
using GymSync.Domain.Enums;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Workouts.Commands;

public record StartWorkoutCommand : IRequest<Result<WorkoutResponse>>
{
    public string? Name { get; init; }
    public Guid? TemplateId { get; init; }
    public bool InvitePartner { get; init; }
}

public record WorkoutResponse
{
    public Guid Id { get; init; }
    public string Name { get; init; } = string.Empty;
    public DateTime StartedAt { get; init; }
    public bool PartnerJoined { get; init; }
}

public class StartWorkoutCommandHandler : IRequestHandler<StartWorkoutCommand, Result<WorkoutResponse>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;
    private readonly INotificationService _notificationService;

    public StartWorkoutCommandHandler(
        IApplicationDbContext context,
        ICurrentUserService currentUser,
        INotificationService notificationService)
    {
        _context = context;
        _currentUser = currentUser;
        _notificationService = notificationService;
    }

    public async Task<Result<WorkoutResponse>> Handle(StartWorkoutCommand request, CancellationToken ct)
    {
        var user = await _context.Users
            .FirstAsync(u => u.Id == _currentUser.UserId, ct);

        var workout = new Workout
        {
            Id = Guid.NewGuid(),
            UserId = user.Id,
            Name = request.Name ?? $"{user.DisplayName}'s Workout",
            Status = WorkoutStatus.InProgress,
            StartedAt = DateTime.UtcNow,
            IsShared = request.InvitePartner && user.PartnerId.HasValue,
            SharedWithUserId = request.InvitePartner ? user.PartnerId : null
        };

        if (request.TemplateId.HasValue)
        {
            var template = await _context.WorkoutTemplates
                .Include(t => t.Exercises)
                .FirstOrDefaultAsync(t => t.Id == request.TemplateId.Value, ct);

            if (template is not null)
            {
                foreach (var te in template.Exercises.OrderBy(e => e.Order))
                {
                    var we = new WorkoutExercise
                    {
                        Id = Guid.NewGuid(),
                        WorkoutId = workout.Id,
                        ExerciseId = te.ExerciseId,
                        Order = te.Order
                    };

                    for (int s = 1; s <= te.DefaultSets; s++)
                    {
                        we.Sets.Add(new ExerciseSet
                        {
                            Id = Guid.NewGuid(),
                            WorkoutExerciseId = we.Id,
                            SetNumber = s,
                            Reps = te.DefaultReps,
                            Weight = te.DefaultWeight
                        });
                    }

                    workout.Exercises.Add(we);
                }
            }
        }

        _context.Workouts.Add(workout);
        user.Status = UserStatus.WorkingOut;
        user.LastActiveAt = DateTime.UtcNow;

        await _context.SaveChangesAsync(ct);

        if (request.InvitePartner && user.PartnerId.HasValue)
        {
            await _notificationService.SendPushNotificationToPartnerAsync(
                user.Id,
                "Workout Invitation",
                $"{user.DisplayName} wants to work out with you.",
                new { workoutId = workout.Id, type = "workout_invite" });
        }

        return Result.Success(new WorkoutResponse
        {
            Id = workout.Id,
            Name = workout.Name,
            StartedAt = workout.StartedAt,
            PartnerJoined = false
        });
    }
}
