namespace GymSync.Domain.Entities;

public class Exercise
{
    public Guid Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public string MuscleGroup { get; set; } = string.Empty;
    public string Category { get; set; } = string.Empty;
    public bool IsBodyweight { get; set; }
    public bool RequiresEquipment { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public ICollection<WorkoutExercise> WorkoutExercises { get; set; } = new List<WorkoutExercise>();
}
