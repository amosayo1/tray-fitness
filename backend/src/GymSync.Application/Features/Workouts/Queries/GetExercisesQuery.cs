using GymSync.Application.Common.Interfaces;
using GymSync.Application.Common.Models;
using MediatR;
using Microsoft.EntityFrameworkCore;

namespace GymSync.Application.Features.Workouts.Queries;

public record GetExercisesQuery : IRequest<Result<List<ExerciseDto>>>;

public record ExerciseDto
{
    public Guid Id { get; init; }
    public string Name { get; init; } = string.Empty;
    public string? Description { get; init; }
    public string MuscleGroup { get; init; } = string.Empty;
    public string Category { get; init; } = string.Empty;
    public bool IsBodyweight { get; init; }
    public bool RequiresEquipment { get; init; }
}

public class GetExercisesQueryHandler : IRequestHandler<GetExercisesQuery, Result<List<ExerciseDto>>>
{
    private readonly IApplicationDbContext _context;

    public GetExercisesQueryHandler(IApplicationDbContext context)
    {
        _context = context;
    }

    public async Task<Result<List<ExerciseDto>>> Handle(GetExercisesQuery request, CancellationToken ct)
    {
        var exercises = await _context.Exercises
            .OrderBy(e => e.Category)
            .ThenBy(e => e.Name)
            .Select(e => new ExerciseDto
            {
                Id = e.Id,
                Name = e.Name,
                Description = e.Description,
                MuscleGroup = e.MuscleGroup,
                Category = e.Category,
                IsBodyweight = e.IsBodyweight,
                RequiresEquipment = e.RequiresEquipment
            })
            .ToListAsync(ct);

        return Result.Success(exercises);
    }
}
