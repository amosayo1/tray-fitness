package com.gymsync.data.model.response

data class ApiResult<T>(
    val succeeded: Boolean,
    val data: T?,
    val errors: List<String>? = null
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: String
)

data class WorkoutResponse(
    val id: String,
    val name: String,
    val startedAt: String,
    val partnerJoined: Boolean
)

data class SetCompletedResponse(
    val isPersonalRecord: Boolean,
    val previousPrValue: String?
)

data class WorkoutSummaryResponse(
    val id: String,
    val name: String,
    val duration: String,
    val totalExercises: Int,
    val totalSets: Int,
    val totalReps: Int,
    val totalVolume: Int,
    val caloriesBurned: Int?,
    val personalRecords: Int,
    val musclesTrained: List<String>
)

data class MessageResponse(
    val id: String,
    val senderId: String,
    val senderName: String,
    val textContent: String?,
    val imageUrl: String?,
    val voiceNoteUrl: String?,
    val voiceNoteDurationSeconds: Int?,
    val isRead: Boolean,
    val isDelivered: Boolean,
    val sentAt: String
)

data class HomeDataResponse(
    val myProfile: UserProfile,
    val partnerProfile: UserProfile?,
    val currentStreak: Int,
    val hasWorkoutToday: Boolean,
    val partnerWorkoutToday: Boolean,
    val caloriesToday: Int,
    val workoutDurationToday: String,
    val currentChallenge: ChallengeInfo?,
    val unreadMessages: Int
)

data class UserProfile(
    val id: String,
    val displayName: String,
    val profilePhotoUrl: String?,
    val status: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveAt: String?
)

data class ChallengeInfo(
    val id: String,
    val name: String,
    val goal: String,
    val myProgress: Double,
    val partnerProgress: Double,
    val endsAt: String
)

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

data class PetDto(
    val id: String,
    val name: String,
    val type: Int,
    val color: String?
)

data class WaterReminderDto(
    val id: String,
    val intervalMinutes: Int,
    val isActive: Boolean
)

data class WaterIntakeDto(
    val id: String,
    val amountMl: Int,
    val takenAt: String
)

data class ExerciseDto(
    val id: String,
    val name: String,
    val description: String?,
    val muscleGroup: String,
    val category: String,
    val isBodyweight: Boolean,
    val requiresEquipment: Boolean
)

data class WorkoutTemplateDto(
    val id: String,
    val name: String,
    val description: String?,
    val isAdminCreated: Boolean,
    val exercises: List<WorkoutTemplateExerciseDto>
)

data class WorkoutTemplateExerciseDto(
    val exerciseId: String,
    val exerciseName: String,
    val muscleGroup: String,
    val order: Int,
    val defaultSets: Int,
    val defaultReps: Int,
    val defaultWeight: Double?,
    val defaultRestSeconds: Int?
)

data class WorkoutDetailDto(
    val id: String,
    val name: String,
    val startedAt: String,
    val totalVolume: Int,
    val caloriesBurned: Int?,
    val exercises: List<WorkoutExerciseDto>
)

data class WorkoutExerciseDto(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val muscleGroup: String,
    val order: Int,
    val sets: List<ExerciseSetDto>
)

data class ExerciseSetDto(
    val id: String,
    val setNumber: Int,
    val reps: Int?,
    val weight: Double?,
    val steps: Int?,
    val rpe: Int?,
    val isCompleted: Boolean,
    val isPersonalRecord: Boolean
)

data class DailyStepLogDto(
    val id: String,
    val date: String,
    val steps: Int,
    val target: Int,
    val remaining: Int,
    val percentComplete: Double
)

data class AiChatResponse(
    val message: String,
    val petAnimation: String
)
