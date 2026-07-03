package com.gymsync.data.model.response

data class DailyStepLogDto(
    val id: String,
    val date: String,
    val steps: Int,
    val target: Int,
    val remaining: Int,
    val percentComplete: Double
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
