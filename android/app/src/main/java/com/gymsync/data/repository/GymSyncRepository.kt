package com.gymsync.data.repository

import com.gymsync.data.api.GymSyncApi
import com.gymsync.data.local.TokenManager
import com.gymsync.data.model.request.*
import com.gymsync.data.model.response.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GymSyncRepository @Inject constructor(
    private val tokenManager: TokenManager,
    private val api: GymSyncApi
) {
    suspend fun login(username: String, password: String): Result<TokenResponse> = apiCall {
        api.login(LoginRequest(username, password))
    }

    suspend fun activate(inviteToken: String, username: String, displayName: String, password: String, email: String): Result<TokenResponse> = apiCall {
        api.activate(ActivateRequest(inviteToken, username, displayName, password, email))
    }

    suspend fun activateWithPet(inviteToken: String, username: String, displayName: String, password: String, email: String, petName: String, petType: String, petColor: String?): Result<TokenResponse> = apiCall {
        api.activateWithPet(ActivateWithPetRequest(inviteToken, username, displayName, password, email, petName, petType, petColor))
    }

    suspend fun refreshToken(): Result<TokenResponse> = apiCall {
        api.refresh(RefreshRequest(tokenManager.refreshToken ?: ""))
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = apiCall {
        api.changePassword(ChangePasswordRequest(currentPassword, newPassword))
    }

    suspend fun generateInvite(): Result<String> = apiCall {
        api.generateInvite()
    }

    suspend fun startWorkout(name: String? = null, templateId: String? = null, invitePartner: Boolean = false): Result<WorkoutResponse> = apiCall {
        api.startWorkout(StartWorkoutRequest(name, templateId, invitePartner))
    }

    suspend fun joinWorkout(workoutId: String): Result<Unit> = apiCall {
        api.joinWorkout(workoutId)
    }

    suspend fun getWorkout(workoutId: String): Result<WorkoutDetailDto> = apiCall {
        api.getWorkout(workoutId)
    }

    suspend fun completeSet(workoutId: String, workoutExerciseId: String, setNumber: Int, reps: Int?, weight: Double?, rpe: Int?): Result<SetCompletedResponse> = apiCall {
        api.completeSet(workoutId, CompleteSetRequest(workoutExerciseId, setNumber, reps, weight, rpe))
    }

    suspend fun addExerciseToWorkout(workoutId: String, exerciseId: String, order: Int, defaultSets: Int = 3, defaultReps: Int = 10): Result<WorkoutExerciseDto> = apiCall {
        api.addExerciseToWorkout(workoutId, AddExerciseRequest(exerciseId, order, defaultSets, defaultReps))
    }

    suspend fun finishWorkout(workoutId: String, caloriesBurned: Int?): Result<WorkoutSummaryResponse> = apiCall {
        api.finishWorkout(workoutId, FinishWorkoutRequest(workoutId, caloriesBurned))
    }

    suspend fun startRestTimer(workoutId: String, durationSeconds: Int): Result<Unit> = apiCall {
        api.startRestTimer(workoutId, StartRestTimerRequest(workoutId, durationSeconds))
    }

    suspend fun sendMessage(text: String? = null, imageBase64: String? = null, imageContentType: String? = null): Result<MessageResponse> = apiCall {
        api.sendMessage(SendMessageRequest(text, imageBase64, imageContentType))
    }

    suspend fun getMessages(page: Int = 1, pageSize: Int = 50): Result<List<MessageResponse>> = apiCall {
        api.getMessages(page, pageSize)
    }

    suspend fun sendMotivation(type: String, customMessage: String? = null): Result<Unit> = apiCall {
        api.sendMotivation(MotivationRequest(type, customMessage))
    }

    suspend fun getHomeData(): Result<HomeDataResponse> = apiCall {
        api.getHomeData()
    }

    suspend fun getProgress(months: Int = 3): Result<ProgressResponse> = apiCall {
        api.getProgress(months)
    }

    suspend fun adminGenerateInvite(): Result<String> = apiCall {
        api.adminGenerateInvite()
    }

    suspend fun deactivateAccount(userId: String): Result<Unit> = apiCall {
        api.deactivateAccount(userId)
    }

    suspend fun resetPassword(userId: String, newPassword: String): Result<String> = apiCall {
        api.resetPassword(ResetPasswordRequest(userId, newPassword))
    }

    suspend fun setPet(name: String, type: String, color: String?): Result<PetDto> = apiCall {
        api.setPet(SetPetRequest(name, type, color))
    }

    suspend fun getMyPet(): Result<PetDto> = apiCall {
        api.getMyPet()
    }

    suspend fun getExercises(): Result<List<ExerciseDto>> = apiCall {
        api.getExercises()
    }

    suspend fun getTemplates(): Result<List<WorkoutTemplateDto>> = apiCall {
        api.getTemplates()
    }

    suspend fun logSteps(date: String, steps: Int): Result<DailyStepLogDto> = apiCall {
        api.logSteps(LogStepsRequest(date, steps))
    }

    suspend fun getStepHistory(days: Int = 30): Result<List<DailyStepLogDto>> = apiCall {
        api.getStepHistory(days)
    }

    suspend fun setWaterReminder(workoutId: String? = null, intervalMinutes: Int = 15, isActive: Boolean = true): Result<WaterReminderDto> = apiCall {
        api.setWaterReminder(SetWaterReminderRequest(workoutId, intervalMinutes, isActive))
    }

    suspend fun logWater(workoutId: String? = null, amountMl: Int = 250): Result<WaterIntakeDto> = apiCall {
        api.logWater(LogWaterIntakeRequest(workoutId, amountMl))
    }

    suspend fun getWaterIntakes(workoutId: String? = null, lastHours: Int = 24): Result<List<WaterIntakeDto>> = apiCall {
        api.getWaterIntakes(workoutId, lastHours)
    }

    suspend fun aiChat(message: String, petType: Int, petName: String, userName: String?, context: String?): Result<AiChatResponse> = apiCall {
        api.aiChat(AiChatRequest(message, petType, petName, userName, context))
    }

    private suspend fun <T> apiCall(call: suspend () -> retrofit2.Response<ApiResult<T>>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.succeeded && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.errors?.joinToString() ?: "Request failed"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}