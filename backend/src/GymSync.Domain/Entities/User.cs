using GymSync.Domain.Enums;

namespace GymSync.Domain.Entities;

public class User
{
    public Guid Id { get; set; }
    public string Username { get; set; } = string.Empty;
    public string DisplayName { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string PasswordHash { get; set; } = string.Empty;
    public string? ProfilePhotoUrl { get; set; }
    public UnitSystem UnitSystem { get; set; } = UnitSystem.Metric;
    public UserStatus Status { get; set; } = UserStatus.Offline;
    public bool IsAdmin { get; set; }
    public bool IsPartnerLinked { get; set; }
    public Guid? PartnerId { get; set; }
    public User? Partner { get; set; }
    public int CurrentStreak { get; set; }
    public int LongestStreak { get; set; }
    public DateTime? LastWorkoutAt { get; set; }
    public DateTime? LastActiveAt { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    public ICollection<RefreshToken> RefreshTokens { get; set; } = new List<RefreshToken>();
    public ICollection<Workout> Workouts { get; set; } = new List<Workout>();
    public ICollection<WorkoutSession> WorkoutSessions { get; set; } = new List<WorkoutSession>();
}
