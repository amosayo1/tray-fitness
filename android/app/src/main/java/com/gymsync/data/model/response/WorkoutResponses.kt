package com.gymsync.data.model.response

data class WorkoutResponse(
    val id: String,
    val name: String,
    val startedAt: String,
    val partnerJoined: Boolean
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

data class ExerciseDto(
    val id: String,
    val name: String,
    val description: String?,
    val muscleGroup: String,
    val category: String,
    val isBodyweight: Boolean,
    val requiresEquipment: Boolean
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
