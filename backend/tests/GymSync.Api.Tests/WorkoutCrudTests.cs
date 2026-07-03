using GymSync.Application.Common.Interfaces;
using GymSync.Application.Features.Workouts.Commands;
using GymSync.Application.Features.Workouts.Queries;
using GymSync.Domain.Entities;
using GymSync.Domain.Enums;
using GymSync.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;
using Moq;

namespace GymSync.Api.Tests;

public class WorkoutCrudTests : IDisposable
{
    private readonly ApplicationDbContext _context;
    private readonly Mock<ICurrentUserService> _currentUserMock;
    private readonly Mock<INotificationService> _notificationMock;
    private readonly User _testUser;

    public WorkoutCrudTests()
    {
        var options = new DbContextOptionsBuilder<ApplicationDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
            .Options;

        _context = new ApplicationDbContext(options);
        _currentUserMock = new Mock<ICurrentUserService>();
        _notificationMock = new Mock<INotificationService>();

        _testUser = new User
        {
            Id = Guid.NewGuid(),
            Username = "workoutuser",
            DisplayName = "Workout User",
            Email = "workout@gymsync.app",
            PasswordHash = "hash",
            Status = UserStatus.Online,
            IsAdmin = false,
            CurrentStreak = 0,
            LongestStreak = 0,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };
        _context.Users.Add(_testUser);
        _context.SaveChanges();

        _currentUserMock.Setup(x => x.UserId).Returns(_testUser.Id);
        _currentUserMock.Setup(x => x.Username).Returns(_testUser.Username);
        _currentUserMock.Setup(x => x.IsAdmin).Returns(false);
    }

    [Fact]
    public async Task StartWorkout_CreatesWorkoutAndReturnsResponse()
    {
        var handler = new StartWorkoutCommandHandler(_context, _currentUserMock.Object, _notificationMock.Object);
        var command = new StartWorkoutCommand { Name = "Morning Push" };

        var result = await handler.Handle(command, CancellationToken.None);

        Assert.True(result.Succeeded);
        Assert.NotNull(result.Data);
        Assert.Equal("Morning Push", result.Data!.Name);
        Assert.NotEqual(Guid.Empty, result.Data.Id);
    }

    [Fact]
    public async Task StartWorkout_UpdatesUserStatusToWorkingOut()
    {
        var handler = new StartWorkoutCommandHandler(_context, _currentUserMock.Object, _notificationMock.Object);
        var command = new StartWorkoutCommand();

        await handler.Handle(command, CancellationToken.None);

        var user = await _context.Users.FirstAsync(u => u.Id == _testUser.Id);
        Assert.Equal(UserStatus.WorkingOut, user.Status);
    }

    [Fact]
    public async Task StartWorkout_WithTemplate_CreatesPrePopulatedSets()
    {
        var exercise = new Exercise
        {
            Id = Guid.NewGuid(),
            Name = "Bench Press",
            MuscleGroup = "Chest",
            Category = "Strength",
            RequiresEquipment = true
        };
        _context.Exercises.Add(exercise);

        var template = new WorkoutTemplate
        {
            Id = Guid.NewGuid(),
            UserId = _testUser.Id,
            Name = "Push Day",
            IsAdminCreated = true,
            Exercises = new List<WorkoutTemplateExercise>
            {
                new()
                {
                    ExerciseId = exercise.Id,
                    Order = 1,
                    DefaultSets = 3,
                    DefaultReps = 10,
                    DefaultWeight = 60
                }
            }
        };
        _context.WorkoutTemplates.Add(template);
        _context.SaveChanges();

        var handler = new StartWorkoutCommandHandler(_context, _currentUserMock.Object, _notificationMock.Object);
        var command = new StartWorkoutCommand { TemplateId = template.Id };

        var result = await handler.Handle(command, CancellationToken.None);

        Assert.True(result.Succeeded);

        var workout = await _context.Workouts
            .Include(w => w.Exercises)
                .ThenInclude(e => e.Sets)
            .FirstAsync(w => w.Id == result.Data!.Id);

        var firstExercise = workout.Exercises.First();
        Assert.Single(workout.Exercises);
        Assert.Equal(3, firstExercise.Sets.Count);
        Assert.All(firstExercise.Sets, s => Assert.Equal(10, s.Reps));
    }

    [Fact]
    public async Task CompleteSet_MarksSetAsCompleted()
    {
        var exercise = new Exercise
        {
            Id = Guid.NewGuid(),
            Name = "Squat",
            MuscleGroup = "Legs",
            Category = "Strength",
            RequiresEquipment = true
        };
        _context.Exercises.Add(exercise);

        var workout = new Workout
        {
            Id = Guid.NewGuid(),
            UserId = _testUser.Id,
            Name = "Leg Day",
            Status = WorkoutStatus.InProgress,
            StartedAt = DateTime.UtcNow
        };
        _context.Workouts.Add(workout);

        var workoutExercise = new WorkoutExercise
        {
            Id = Guid.NewGuid(),
            WorkoutId = workout.Id,
            ExerciseId = exercise.Id,
            Order = 1
        };
        _context.WorkoutExercises.Add(workoutExercise);

        var exerciseSet = new ExerciseSet
        {
            Id = Guid.NewGuid(),
            WorkoutExerciseId = workoutExercise.Id,
            SetNumber = 1,
            Reps = 10,
            Weight = 100,
            IsCompleted = false
        };
        _context.ExerciseSets.Add(exerciseSet);
        _context.SaveChanges();

        var handler = new CompleteSetCommandHandler(_context);
        var command = new CompleteSetCommand
        {
            WorkoutExerciseId = workoutExercise.Id,
            SetNumber = 1,
            Reps = 10,
            Weight = 100,
            Rpe = 8
        };

        var result = await handler.Handle(command, CancellationToken.None);

        Assert.True(result.Succeeded);

        var updatedSet = await _context.ExerciseSets.FirstAsync(s => s.Id == exerciseSet.Id);
        Assert.True(updatedSet.IsCompleted);
        Assert.Equal(10, updatedSet.Reps);
        Assert.Equal(100, updatedSet.Weight);
        Assert.Equal(8, updatedSet.Rpe);
    }

    [Fact]
    public async Task CompleteSet_OnFirstSet_ReturnsPersonalRecord()
    {
        var exercise = new Exercise
        {
            Id = Guid.NewGuid(),
            Name = "Deadlift",
            MuscleGroup = "Back",
            Category = "Strength",
            RequiresEquipment = true
        };
        _context.Exercises.Add(exercise);

        var workout = new Workout
        {
            Id = Guid.NewGuid(),
            UserId = _testUser.Id,
            Name = "Back Day",
            Status = WorkoutStatus.InProgress,
            StartedAt = DateTime.UtcNow
        };
        _context.Workouts.Add(workout);

        var workoutExercise = new WorkoutExercise
        {
            Id = Guid.NewGuid(),
            WorkoutId = workout.Id,
            ExerciseId = exercise.Id,
            Order = 1
        };
        _context.WorkoutExercises.Add(workoutExercise);

        var exerciseSet = new ExerciseSet
        {
            Id = Guid.NewGuid(),
            WorkoutExerciseId = workoutExercise.Id,
            SetNumber = 1,
            IsCompleted = false
        };
        _context.ExerciseSets.Add(exerciseSet);
        _context.SaveChanges();

        var handler = new CompleteSetCommandHandler(_context);
        var command = new CompleteSetCommand
        {
            WorkoutExerciseId = workoutExercise.Id,
            SetNumber = 1,
            Reps = 5,
            Weight = 140
        };

        var result = await handler.Handle(command, CancellationToken.None);

        Assert.True(result.Succeeded);
        Assert.True(result.Data!.IsPersonalRecord);
        Assert.Null(result.Data.PreviousPrValue);
    }

    [Fact]
    public async Task CompleteSet_WithNonExistentSet_ReturnsFailure()
    {
        var handler = new CompleteSetCommandHandler(_context);
        var command = new CompleteSetCommand
        {
            WorkoutExerciseId = Guid.NewGuid(),
            SetNumber = 99,
            Reps = 10,
            Weight = 50
        };

        var result = await handler.Handle(command, CancellationToken.None);

        Assert.False(result.Succeeded);
    }

    [Fact]
    public async Task FinishWorkout_CompletesWorkoutAndReturnsSummary()
    {
        var exercise = new Exercise
        {
            Id = Guid.NewGuid(),
            Name = "Push Up",
            MuscleGroup = "Chest",
            Category = "Strength",
            IsBodyweight = true
        };
        _context.Exercises.Add(exercise);

        var workout = new Workout
        {
            Id = Guid.NewGuid(),
            UserId = _testUser.Id,
            Name = "Morning Workout",
            Status = WorkoutStatus.InProgress,
            StartedAt = DateTime.UtcNow.AddHours(-1)
        };
        _context.Workouts.Add(workout);

        var workoutExercise = new WorkoutExercise
        {
            Id = Guid.NewGuid(),
            WorkoutId = workout.Id,
            ExerciseId = exercise.Id,
            Order = 1
        };
        _context.WorkoutExercises.Add(workoutExercise);

        var completedSet = new ExerciseSet
        {
            Id = Guid.NewGuid(),
            WorkoutExerciseId = workoutExercise.Id,
            SetNumber = 1,
            Reps = 20,
            IsCompleted = true,
            IsPersonalRecord = true
        };
        _context.ExerciseSets.Add(completedSet);
        _context.SaveChanges();

        var handler = new FinishWorkoutCommandHandler(_context, _currentUserMock.Object, _notificationMock.Object);
        var command = new FinishWorkoutCommand { WorkoutId = workout.Id, CaloriesBurned = 150 };

        var result = await handler.Handle(command, CancellationToken.None);

        Assert.True(result.Succeeded);
        Assert.NotNull(result.Data);
        Assert.Equal(workout.Id, result.Data!.Id);
        Assert.Equal(1, result.Data.TotalExercises);
        Assert.Equal(1, result.Data.TotalSets);
        Assert.Equal(20, result.Data.TotalReps);
        Assert.Equal(150, result.Data.CaloriesBurned);
        Assert.Equal(1, result.Data.PersonalRecords);
        Assert.Contains("Chest", result.Data.MusclesTrained);
    }

    [Fact]
    public async Task FinishWorkout_UpdatesUserStreak()
    {
        var workout = new Workout
        {
            Id = Guid.NewGuid(),
            UserId = _testUser.Id,
            Name = "Streak Workout",
            Status = WorkoutStatus.InProgress,
            StartedAt = DateTime.UtcNow.AddMinutes(-30)
        };
        _context.Workouts.Add(workout);
        _context.SaveChanges();

        var handler = new FinishWorkoutCommandHandler(_context, _currentUserMock.Object, _notificationMock.Object);
        var command = new FinishWorkoutCommand { WorkoutId = workout.Id };

        await handler.Handle(command, CancellationToken.None);

        var user = await _context.Users.FirstAsync(u => u.Id == _testUser.Id);
        Assert.Equal(1, user.CurrentStreak);
        Assert.Equal(1, user.LongestStreak);
        Assert.Equal(UserStatus.FinishedWorkout, user.Status);
    }

    [Fact]
    public async Task FinishWorkout_WithNonExistentWorkout_ReturnsFailure()
    {
        var handler = new FinishWorkoutCommandHandler(_context, _currentUserMock.Object, _notificationMock.Object);
        var command = new FinishWorkoutCommand { WorkoutId = Guid.NewGuid() };

        var result = await handler.Handle(command, CancellationToken.None);

        Assert.False(result.Succeeded);
    }

    public void Dispose()
    {
        _context.Dispose();
    }
}
