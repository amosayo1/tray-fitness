namespace GymSync.Domain.Entities;

public class ExerciseSet
{
    public Guid Id { get; set; }
    public Guid WorkoutExerciseId { get; set; }
    public WorkoutExercise WorkoutExercise { get; set; } = null!;
    public int SetNumber { get; set; }
    public int? Reps { get; set; }
    public decimal? Weight { get; set; }
    public int? Steps { get; set; }
    public int? DistanceMeters { get; set; }
    public int? DurationSeconds { get; set; }
    public int? Rpe { get; set; }
    public string? Notes { get; set; }
    public bool IsPersonalRecord { get; set; }
    public bool IsCompleted { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
