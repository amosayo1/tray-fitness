using GymSync.Application.Common.Interfaces;
using GymSync.Domain.Entities;
using GymSync.Domain.Enums;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Hosting;

namespace GymSync.Infrastructure.Services;

public class DataSeederService : IHostedService
{
    private readonly IServiceProvider _serviceProvider;
    private readonly ILogger<DataSeederService> _logger;

    public DataSeederService(IServiceProvider serviceProvider, ILogger<DataSeederService> logger)
    {
        _serviceProvider = serviceProvider;
        _logger = logger;
    }

    public async Task StartAsync(CancellationToken ct)
    {
        using var scope = _serviceProvider.CreateScope();
        var context = scope.ServiceProvider.GetRequiredService<IApplicationDbContext>();

        if (context is DbContext dbContext)
        {
            await dbContext.Database.EnsureCreatedAsync(ct);
        }

        if (context.Users.Any())
            return;

        _logger.LogInformation("Seeding initial data...");

        var adminUser = new User
        {
            Id = Guid.NewGuid(),
            Username = "admin",
            DisplayName = "Terry",
            Email = "admin@gymsync.app",
            PasswordHash = "$2b$12$f7TX4aUQXazGkNVZVacb3e7M6TdETIgKFiUEflR5TWnADShfi/YkC",
            Status = UserStatus.Offline,
            IsAdmin = true,
            IsPartnerLinked = false,
            CurrentStreak = 0,
            LongestStreak = 0,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        context.Users.Add(adminUser);

        var adminPet = new Pet
        {
            Id = Guid.NewGuid(),
            UserId = adminUser.Id,
            Name = "Jeff",
            Type = PetType.Dog,
            Color = "BrownBlack"
        };
        context.Pets.Add(adminPet);

        var partnerUser = new User
        {
            Id = Guid.NewGuid(),
            Username = "ayo",
            DisplayName = "Ayo",
            Email = "ayo@gymsync.app",
            PasswordHash = "$2b$12$f7TX4aUQXazGkNVZVacb3e7M6TdETIgKFiUEflR5TWnADShfi/YkC",
            Status = UserStatus.Offline,
            IsAdmin = false,
            IsPartnerLinked = false,
            CurrentStreak = 0,
            LongestStreak = 0,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        context.Users.Add(partnerUser);

        var partnerPet = new Pet
        {
            Id = Guid.NewGuid(),
            UserId = partnerUser.Id,
            Name = "Kiara",
            Type = PetType.Cat,
            Color = "BlackBrownWhite"
        };
        context.Pets.Add(partnerPet);

        var exercises = new List<Exercise>
        {
            new() { Id = Guid.NewGuid(), Name = "Bench Press", MuscleGroup = "Chest", Category = "Strength", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Squat", MuscleGroup = "Legs", Category = "Strength", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Deadlift", MuscleGroup = "Back", Category = "Strength", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Overhead Press", MuscleGroup = "Shoulders", Category = "Strength", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Barbell Row", MuscleGroup = "Back", Category = "Strength", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Pull Up", MuscleGroup = "Back", Category = "Strength", IsBodyweight = true },
            new() { Id = Guid.NewGuid(), Name = "Push Up", MuscleGroup = "Chest", Category = "Strength", IsBodyweight = true },
            new() { Id = Guid.NewGuid(), Name = "Bicep Curl", MuscleGroup = "Arms", Category = "Strength", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Tricep Dip", MuscleGroup = "Arms", Category = "Strength", IsBodyweight = true },
            new() { Id = Guid.NewGuid(), Name = "Leg Press", MuscleGroup = "Legs", Category = "Strength", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Lat Pulldown", MuscleGroup = "Back", Category = "Strength", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Dumbbell Fly", MuscleGroup = "Chest", Category = "Strength", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Romanian Deadlift", MuscleGroup = "Legs", Category = "Strength", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Plank", MuscleGroup = "Core", Category = "Endurance", IsBodyweight = true },
            new() { Id = Guid.NewGuid(), Name = "Walking", MuscleGroup = "Cardio", Category = "Cardio", IsBodyweight = true, Description = "Track steps toward 10,000 daily goal" },
            new() { Id = Guid.NewGuid(), Name = "Running", MuscleGroup = "Cardio", Category = "Cardio", IsBodyweight = true },
            new() { Id = Guid.NewGuid(), Name = "Cycling", MuscleGroup = "Cardio", Category = "Cardio", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Sit Up", MuscleGroup = "Core", Category = "Endurance", IsBodyweight = true },
            new() { Id = Guid.NewGuid(), Name = "Lunge", MuscleGroup = "Legs", Category = "Strength", IsBodyweight = true },
            new() { Id = Guid.NewGuid(), Name = "Glute Bridge", MuscleGroup = "Legs", Category = "Strength", IsBodyweight = true },
            new() { Id = Guid.NewGuid(), Name = "Mountain Climber", MuscleGroup = "Core", Category = "Cardio", IsBodyweight = true },
            new() { Id = Guid.NewGuid(), Name = "Jumping Jack", MuscleGroup = "Cardio", Category = "Cardio", IsBodyweight = true },
            new() { Id = Guid.NewGuid(), Name = "Burpee", MuscleGroup = "Cardio", Category = "Cardio", IsBodyweight = true },
            new() { Id = Guid.NewGuid(), Name = "Dumbbell Shoulder Press", MuscleGroup = "Shoulders", Category = "Strength", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Dumbbell Row", MuscleGroup = "Back", Category = "Strength", RequiresEquipment = true },
            new() { Id = Guid.NewGuid(), Name = "Leg Raise", MuscleGroup = "Core", Category = "Endurance", IsBodyweight = true },
            new() { Id = Guid.NewGuid(), Name = "Bicycle Crunch", MuscleGroup = "Core", Category = "Endurance", IsBodyweight = true },
        };

        foreach (var exercise in exercises)
            context.Exercises.Add(exercise);

        var templates = new List<WorkoutTemplate>
        {
            new()
            {
                Id = Guid.NewGuid(),
                UserId = adminUser.Id,
                Name = "Push Day",
                Description = "Chest, Shoulders, Triceps",
                IsAdminCreated = true,
                Exercises = new List<WorkoutTemplateExercise>
                {
                    new() { ExerciseId = exercises[0].Id, Order = 1, DefaultSets = 4, DefaultReps = 8 },
                    new() { ExerciseId = exercises[3].Id, Order = 2, DefaultSets = 3, DefaultReps = 10 },
                    new() { ExerciseId = exercises[7].Id, Order = 3, DefaultSets = 3, DefaultReps = 12 },
                }
            },
            new()
            {
                Id = Guid.NewGuid(),
                UserId = adminUser.Id,
                Name = "Pull Day",
                Description = "Back, Biceps",
                IsAdminCreated = true,
                Exercises = new List<WorkoutTemplateExercise>
                {
                    new() { ExerciseId = exercises[4].Id, Order = 1, DefaultSets = 4, DefaultReps = 8 },
                    new() { ExerciseId = exercises[5].Id, Order = 2, DefaultSets = 3, DefaultReps = 10 },
                    new() { ExerciseId = exercises[7].Id, Order = 3, DefaultSets = 3, DefaultReps = 12 },
                }
            },
            new()
            {
                Id = Guid.NewGuid(),
                UserId = adminUser.Id,
                Name = "Leg Day",
                Description = "Quads, Hamstrings, Glutes",
                IsAdminCreated = true,
                Exercises = new List<WorkoutTemplateExercise>
                {
                    new() { ExerciseId = exercises[1].Id, Order = 1, DefaultSets = 4, DefaultReps = 8 },
                    new() { ExerciseId = exercises[9].Id, Order = 2, DefaultSets = 3, DefaultReps = 12 },
                    new() { ExerciseId = exercises[12].Id, Order = 3, DefaultSets = 3, DefaultReps = 10 },
                }
            },
            new()
            {
                Id = Guid.NewGuid(),
                UserId = adminUser.Id,
                Name = "Bodyweight Circuit",
                Description = "No equipment needed — just a mat",
                IsAdminCreated = true,
                Exercises = new List<WorkoutTemplateExercise>
                {
                    new() { ExerciseId = exercises[6].Id, Order = 1, DefaultSets = 3, DefaultReps = 20 },
                    new() { ExerciseId = exercises[17].Id, Order = 2, DefaultSets = 3, DefaultReps = 20 },
                    new() { ExerciseId = exercises[18].Id, Order = 3, DefaultSets = 3, DefaultReps = 12 },
                    new() { ExerciseId = exercises[13].Id, Order = 4, DefaultSets = 3, DefaultReps = 60, DefaultRestSeconds = 30 },
                    new() { ExerciseId = exercises[19].Id, Order = 5, DefaultSets = 3, DefaultReps = 15 },
                    new() { ExerciseId = exercises[25].Id, Order = 6, DefaultSets = 3, DefaultReps = 15 },
                }
            },
            new()
            {
                Id = Guid.NewGuid(),
                UserId = adminUser.Id,
                Name = "Cardio & Core",
                Description = "Get the heart rate up, no equipment needed",
                IsAdminCreated = true,
                Exercises = new List<WorkoutTemplateExercise>
                {
                    new() { ExerciseId = exercises[21].Id, Order = 1, DefaultSets = 3, DefaultReps = 30, DefaultRestSeconds = 15 },
                    new() { ExerciseId = exercises[20].Id, Order = 2, DefaultSets = 3, DefaultReps = 15, DefaultRestSeconds = 30 },
                    new() { ExerciseId = exercises[22].Id, Order = 3, DefaultSets = 3, DefaultReps = 10, DefaultRestSeconds = 30 },
                    new() { ExerciseId = exercises[26].Id, Order = 4, DefaultSets = 3, DefaultReps = 20, DefaultRestSeconds = 15 },
                }
            },
        };

        foreach (var template in templates)
            context.WorkoutTemplates.Add(template);

        await context.SaveChangesAsync(ct);
        _logger.LogInformation("Seed data created successfully");
    }

    public Task StopAsync(CancellationToken ct) => Task.CompletedTask;
}