package com.gymsync.data.model.response

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

data class PetDto(
    val id: String,
    val name: String,
    val type: Int,
    val color: String?
)
