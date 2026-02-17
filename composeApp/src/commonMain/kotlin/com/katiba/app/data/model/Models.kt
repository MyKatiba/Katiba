package com.katiba.app.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a chapter in the Kenyan Constitution
 */
@Serializable
data class Chapter(
    val number: Int,
    val title: String,
    val articles: List<Article>
)

/**
 * Represents an article within a chapter
 */
@Serializable
data class Article(
    val number: Int,
    val title: String,
    val clauses: List<Clause>
)

/**
 * Represents a clause within an article
 */
@Serializable
data class Clause(
    val number: String,
    val text: String,
    val subClauses: List<SubClause> = emptyList()
)

/**
 * Represents a sub-clause within a clause (e.g., (a), (b), (c))
 */
@Serializable
data class SubClause(
    val label: String,
    val text: String,
    val miniClauses: List<MiniClause> = emptyList(),
    // Keep for backward compatibility
    val subSubClauses: List<SubSubClause> = emptyList()
)

/**
 * Represents a mini-clause within a sub-clause (e.g., (i), (ii), (iii))
 */
@Serializable
data class MiniClause(
    val label: String,
    val text: String
)

/**
 * Represents a sub-sub-clause within a sub-clause (e.g., (i), (ii), (iii))
 * @deprecated Use MiniClause instead
 */
@Serializable
data class SubSubClause(
    val label: String,
    val text: String
)

/**
 * Represents a schedule in the Constitution
 */
@Serializable
data class Schedule(
    val number: Int,
    val title: String,
    val reference: String = "",
    val content: kotlinx.serialization.json.JsonElement? = null
)

/**
 * Full Constitution structure for JSON loading
 */
@Serializable
data class Constitution(
    val preamble: String = "",
    val chapters: List<Chapter>,
    val schedules: List<Schedule> = emptyList()
)

/**
 * Daily content served to users
 */
@Serializable
data class DailyContent(
    val id: String,
    val date: String, // ISO date format
    val clause: Clause,
    val chapterTitle: String,
    val articleTitle: String,
    val articleNumber: Int,
    val aiDescription: String,
    val videoUrl: String = "",
    val videoThumbnailUrl: String = "",
    val educatorName: String = "",
    val nextSteps: List<String>,
    val tips: List<String>
)

/**
 * User profile data
 */
@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String = "",
    val emailVerified: Boolean = false,
    val nationalId: String = "",
    val county: String = "",
    val constituency: String = "",
    val ward: String = "",
    val isRegisteredVoter: Boolean = false,
    val joinedDate: String,
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val totalLessonsCompleted: Int = 0,
    val xp: Int = 0,
    val badges: List<Badge> = emptyList()
)

/**
 * Achievement badge
 */
@Serializable
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val earnedDate: String? = null,
    val isEarned: Boolean = false
)

/**
 * Learning progress for the Plans feature
 */
@Serializable
data class LearningProgress(
    val userId: String,
    val completedLessons: List<String>,
    val currentLessonId: String,
    val totalXp: Int = 0
)

/**
 * A lesson in the learning path
 */
@Serializable
data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val chapterNumber: Int,
    val order: Int,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val isLocked: Boolean = true,
    val isCurrent: Boolean = false
)

/**
 * Activity record for profile
 */
@Serializable
data class ActivityRecord(
    val id: String,
    val type: ActivityType,
    val title: String,
    val date: String,
    val xpEarned: Int = 0
)

@Serializable
enum class ActivityType {
    LESSON_COMPLETED,
    BADGE_EARNED,
    STREAK_MILESTONE,
    CHAPTER_COMPLETED,
    DAILY_CLAUSE_READ
}

// API Request/Response models

/**
 * Register request
 */
@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirm_password: String
)

/**
 * Register response
 */
@Serializable
data class RegisterResponse(
    val userId: String,
    val message: String
)

/**
 * Verify OTP request (for email verification and password reset)
 */
@Serializable
data class VerifyOtpRequest(
    val userId: String,
    val otp: String
)

/**
 * Email verification response
 */
@Serializable
data class VerifyEmailResponse(
    val user: UserProfile,
    val accessToken: String,
    val refreshToken: String
)

/**
 * Resend OTP request
 */
@Serializable
data class ResendOtpRequest(
    val userId: String,
    val purpose: String // "email_verification" or "password_reset"
)

/**
 * Forgot password request
 */
@Serializable
data class ForgotPasswordRequest(
    val email: String
)

/**
 * Forgot password response
 */
@Serializable
data class ForgotPasswordResponse(
    val userId: String,
    val message: String
)

/**
 * Verify reset OTP response
 */
@Serializable
data class VerifyResetOtpResponse(
    val resetToken: String
)

/**
 * Reset password request
 */
@Serializable
data class ResetPasswordRequest(
    val token: String,
    val password: String
)

/**
 * Login request
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Login response
 */
@Serializable
data class LoginResponse(
    val user: UserProfile,
    val accessToken: String,
    val refreshToken: String
)

/**
 * Generic message response
 */
@Serializable
data class MessageResponse(
    val message: String
)

/**
 * Update profile request
 */
@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val county: String? = null,
    val constituency: String? = null,
    val ward: String? = null,
    val nationalId: String? = null,
    val isRegisteredVoter: Boolean? = null
)

/**
 * API error response - handles various backend error formats
 */
@Serializable
data class ApiErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    /**
     * Get the error message from whichever field is available
     */
    fun getErrorMessage(): String = message ?: error ?: "An unknown error occurred"
}
