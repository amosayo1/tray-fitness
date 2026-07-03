package com.gymsync.data.model.response

data class ProgressResponse(
    val monthlyStats: List<MonthlyStats>,
    val exerciseProgress: List<ExerciseProgress>,
    val calendar: List<CalendarEntry>,
    val streak: StreakInfo
)

data class MonthlyStats(
    val month: String,
    val workoutCount: Int,
    val totalVolume: Int,
    val totalCalories: Int,
    val totalDuration: String
)

data class ExerciseProgress(
    val exerciseName: String,
    val bestWeight: Double,
    val bestReps: Int,
    val estimatedOneRm: Double,
    val achievedAt: String
)

data class CalendarEntry(
    val date: String,
    val hasWorkout: Boolean,
    val durationMinutes: Int?
)

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastWorkoutDate: String?,
    val streakAtRisk: Boolean
)
