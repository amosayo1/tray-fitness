package com.gymsync.data.model.request

data class StartWorkoutRequest(
    val name: String? = null,
    val templateId: String? = null,
    val invitePartner: Boolean = false
)

data class CompleteSetRequest(
    val workoutExerciseId: String,
    val setNumber: Int,
    val reps: Int? = null,
    val weight: Double? = null,
    val rpe: Int? = null,
    val notes: String? = null
)

data class FinishWorkoutRequest(
    val workoutId: String? = null,
    val caloriesBurned: Int? = null
)

data class StartRestTimerRequest(
    val workoutId: String? = null,
    val durationSeconds: Int
)

data class AddExerciseRequest(
    val exerciseId: String,
    val order: Int = 1,
    val defaultSets: Int = 3,
    val defaultReps: Int = 10
)
