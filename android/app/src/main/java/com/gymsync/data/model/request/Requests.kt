package com.gymsync.data.model.request

data class LoginRequest(
    val username: String,
    val password: String
)

data class ActivateRequest(
    val inviteToken: String,
    val username: String,
    val displayName: String,
    val password: String,
    val email: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

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

data class SendMessageRequest(
    val textContent: String? = null,
    val imageBase64: String? = null,
    val imageContentType: String? = null
)

data class MotivationRequest(
    val type: String,
    val customMessage: String? = null
)

data class ResetPasswordRequest(
    val userId: String,
    val newPassword: String
)

data class SetPetRequest(
    val name: String,
    val type: String,
    val color: String? = null
)

data class ActivateWithPetRequest(
    val inviteToken: String,
    val username: String,
    val displayName: String,
    val password: String,
    val email: String,
    val petName: String,
    val petType: String,
    val petColor: String? = null
)

data class SetWaterReminderRequest(
    val workoutId: String? = null,
    val intervalMinutes: Int = 15,
    val isActive: Boolean = true
)

data class LogWaterIntakeRequest(
    val workoutId: String? = null,
    val amountMl: Int = 250
)

data class LogStepsRequest(
    val date: String,
    val steps: Int
)

data class AddExerciseRequest(
    val exerciseId: String,
    val order: Int = 1,
    val defaultSets: Int = 3,
    val defaultReps: Int = 10
)

data class AiChatRequest(
    val message: String,
    val petType: Int,
    val petName: String,
    val userName: String? = null,
    val context: String? = null
)
