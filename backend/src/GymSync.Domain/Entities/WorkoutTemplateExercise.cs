namespace GymSync.Domain.Entities;

public class WorkoutTemplateExercise
{
    public Guid Id { get; set; }
    public Guid WorkoutTemplateId { get; set; }
    public WorkoutTemplate WorkoutTemplate { get; set; } = null!;
    public Guid ExerciseId { get; set; }
    public Exercise Exercise { get; set; } = null!;
    public int Order { get; set; }
    public int DefaultSets { get; set; }
    public int DefaultReps { get; set; }
    public decimal? DefaultWeight { get; set; }
    public int? DefaultRestSeconds { get; set; }
}
