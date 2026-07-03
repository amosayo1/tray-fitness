using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Workouts.Queries;

public record GetWorkoutTemplatesQuery : IRequest<Result<List<WorkoutTemplateDto>>>;

public record WorkoutTemplateDto
{
    public Guid Id { get; init; }
    public string Name { get; init; } = string.Empty;
    public string? Description { get; init; }
    public bool IsAdminCreated { get; init; }
    public List<WorkoutTemplateExerciseDto> Exercises { get; init; } = new();
}

public record WorkoutTemplateExerciseDto
{
    public Guid ExerciseId { get; init; }
    public string ExerciseName { get; init; } = string.Empty;
    public string MuscleGroup { get; init; } = string.Empty;
    public int Order { get; init; }
    public int DefaultSets { get; init; }
    public int DefaultReps { get; init; }
    public decimal? DefaultWeight { get; init; }
    public int? DefaultRestSeconds { get; init; }
}

public class GetWorkoutTemplatesQueryHandler
    : IRequestHandler<GetWorkoutTemplatesQuery, Result<List<WorkoutTemplateDto>>>
{
    private readonly IApplicationDbContext _context;
    private readonly ICurrentUserService _currentUser;

    public GetWorkoutTemplatesQueryHandler(
        IApplicationDbContext context,
        ICurrentUserService currentUser)
    {
        _context = context;
        _currentUser = currentUser;
    }

    public async Task<Result<List<WorkoutTemplateDto>>> Handle(
        GetWorkoutTemplatesQuery request, CancellationToken ct)
    {
        var templates = await _context.WorkoutTemplates
            .Where(t => t.UserId == _currentUser.UserId || t.IsAdminCreated)
            .Include(t => t.Exercises)
                .ThenInclude(e => e.Exercise)
            .OrderBy(t => t.Name)
            .ToListAsync(ct);

        var dtos = templates.Select(t => new WorkoutTemplateDto
        {
            Id = t.Id,
            Name = t.Name,
            Description = t.Description,
            IsAdminCreated = t.IsAdminCreated,
            Exercises = t.Exercises
                .OrderBy(e => e.Order)
                .Select(e => new WorkoutTemplateExerciseDto
                {
                    ExerciseId = e.ExerciseId,
                    ExerciseName = e.Exercise.Name,
                    MuscleGroup = e.Exercise.MuscleGroup,
                    Order = e.Order,
                    DefaultSets = e.DefaultSets,
                    DefaultReps = e.DefaultReps,
                    DefaultWeight = e.DefaultWeight,
                    DefaultRestSeconds = e.DefaultRestSeconds
                })
                .ToList()
        }).ToList();

        return Result.Success(dtos);
    }
}
