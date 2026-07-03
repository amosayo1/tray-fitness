package com.gymsync.data.api

import com.gymsync.data.model.request.*
import com.gymsync.data.model.response.*
import retrofit2.Response
import retrofit2.http.*

interface GymSyncApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResult<TokenResponse>>

    @POST("api/auth/activate")
    suspend fun activate(@Body request: ActivateRequest): Response<ApiResult<TokenResponse>>

    @POST("api/auth/activate")
    suspend fun activateWithPet(@Body request: ActivateWithPetRequest): Response<ApiResult<TokenResponse>>

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<ApiResult<TokenResponse>>

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResult<Unit>>

    @POST("api/auth/generate-invite")
    suspend fun generateInvite(): Response<ApiResult<String>>

    @GET("api/workout/{workoutId}")
    suspend fun getWorkout(@Path("workoutId") workoutId: String): Response<ApiResult<WorkoutDetailDto>>

    @POST("api/workout/start")
    suspend fun startWorkout(@Body request: StartWorkoutRequest): Response<ApiResult<WorkoutResponse>>

    @POST("api/workout/{workoutId}/join")
    suspend fun joinWorkout(@Path("workoutId") workoutId: String): Response<ApiResult<Unit>>

    @POST("api/workout/{workoutId}/complete-set")
    suspend fun completeSet(
        @Path("workoutId") workoutId: String,
        @Body request: CompleteSetRequest
    ): Response<ApiResult<SetCompletedResponse>>

    @POST("api/workout/{workoutId}/add-exercise")
    suspend fun addExerciseToWorkout(
        @Path("workoutId") workoutId: String,
        @Body request: AddExerciseRequest
    ): Response<ApiResult<WorkoutExerciseDto>>

    @POST("api/workout/{workoutId}/finish")
    suspend fun finishWorkout(
        @Path("workoutId") workoutId: String,
        @Body request: FinishWorkoutRequest
    ): Response<ApiResult<WorkoutSummaryResponse>>

    @POST("api/workout/{workoutId}/rest-timer")
    suspend fun startRestTimer(
        @Path("workoutId") workoutId: String,
        @Body request: StartRestTimerRequest
    ): Response<ApiResult<Unit>>

    @POST("api/chat/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<ApiResult<MessageResponse>>

    @POST("api/chat/{messageId}/read")
    suspend fun markMessageRead(@Path("messageId") messageId: String): Response<ApiResult<Unit>>

    @POST("api/chat/motivation")
    suspend fun sendMotivation(@Body request: MotivationRequest): Response<ApiResult<Unit>>

    @GET("api/chat/messages")
    suspend fun getMessages(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<ApiResult<List<MessageResponse>>>

    @GET("api/progress/home")
    suspend fun getHomeData(): Response<ApiResult<HomeDataResponse>>

    @GET("api/progress/history")
    suspend fun getProgress(@Query("months") months: Int = 3): Response<ApiResult<ProgressResponse>>

    @POST("api/admin/generate-invite")
    suspend fun adminGenerateInvite(): Response<ApiResult<String>>

    @POST("api/admin/deactivate/{userId}")
    suspend fun deactivateAccount(@Path("userId") userId: String): Response<ApiResult<Unit>>

    @POST("api/admin/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResult<String>>

    @POST("api/pet")
    suspend fun setPet(@Body request: SetPetRequest): Response<ApiResult<PetDto>>

    @GET("api/pet/my")
    suspend fun getMyPet(): Response<ApiResult<PetDto>>

    @GET("api/workout/exercises")
    suspend fun getExercises(): Response<ApiResult<List<ExerciseDto>>>

    @GET("api/workout/templates")
    suspend fun getTemplates(): Response<ApiResult<List<WorkoutTemplateDto>>>

    @POST("api/steps/log")
    suspend fun logSteps(@Body request: LogStepsRequest): Response<ApiResult<DailyStepLogDto>>

    @GET("api/steps/history")
    suspend fun getStepHistory(@Query("days") days: Int = 30): Response<ApiResult<List<DailyStepLogDto>>>

    @POST("api/water/reminder")
    suspend fun setWaterReminder(@Body request: SetWaterReminderRequest): Response<ApiResult<WaterReminderDto>>

    @POST("api/water/log")
    suspend fun logWater(@Body request: LogWaterIntakeRequest): Response<ApiResult<WaterIntakeDto>>

    @POST("api/ai/chat")
    suspend fun aiChat(@Body request: AiChatRequest): Response<ApiResult<AiChatResponse>>

    @GET("api/water/intakes")
    suspend fun getWaterIntakes(
        @Query("workoutId") workoutId: String? = null,
        @Query("lastHours") lastHours: Int = 24
    ): Response<ApiResult<List<WaterIntakeDto>>>
}
