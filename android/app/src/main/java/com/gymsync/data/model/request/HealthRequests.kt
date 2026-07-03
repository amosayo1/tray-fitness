package com.gymsync.data.model.request

data class LogStepsRequest(
    val date: String,
    val steps: Int
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
