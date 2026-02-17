package com.katiba.app.ui.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Navigation 3 Style destinations using NavKey interface
 * All destinations must be @Serializable for state preservation
 */

// Onboarding & Auth destinations
@Serializable
data object OnboardingRoute : NavKey

@Serializable
data object LoginRoute : NavKey

@Serializable
data object SignUpRoute : NavKey

@Serializable
data object ForgotPasswordRoute : NavKey

@Serializable
data class OTPVerificationRoute(
    val userId: String,
    val email: String,
    val purpose: String // "email_verification" or "password_reset"
) : NavKey

@Serializable
data object CivicDataInputRoute : NavKey

@Serializable
data class ResetPasswordRoute(val resetToken: String) : NavKey

// Main bottom navigation destinations
@Serializable
data object HomeRoute : NavKey

@Serializable
data object ConstitutionRoute : NavKey

@Serializable
data object PlansRoute : NavKey

@Serializable
data object ProfileRoute : NavKey

// Home detail screens
@Serializable
data class ClauseDetailRoute(val clauseId: String) : NavKey

@Serializable
data class AIDescriptionRoute(val clauseId: String) : NavKey

@Serializable
data class TipsRoute(val clauseId: String) : NavKey

// Constitution reader screens
@Serializable
data object PreambleRoute : NavKey

@Serializable
data class ChapterListRoute(val searchQuery: String = "") : NavKey

@Serializable
data class ClauseGridRoute(val chapterNumber: Int) : NavKey

@Serializable
data class ReadingRoute(val chapterNumber: Int, val articleNumber: Int) : NavKey

@Serializable
data object SchedulesRoute : NavKey

@Serializable
data class ScheduleDetailRoute(val scheduleNumber: Int) : NavKey

// Plans screens
@Serializable
data class LessonRoute(val lessonId: String) : NavKey

// Profile screens
@Serializable
data object SettingsRoute : NavKey

@Serializable
data object NotificationsRoute : NavKey

@Serializable
data object MzalendoRoute : NavKey

// Settings edit screens
@Serializable
data object EditProfileRoute : NavKey

@Serializable
data object PasswordSecurityRoute : NavKey

@Serializable
data object UpdateResidenceRoute : NavKey

@Serializable
data object NationalIDRoute : NavKey

@Serializable
data object AppearanceRoute : NavKey

@Serializable
data object FontSizeRoute : NavKey

@Serializable
data object LanguageRoute : NavKey

@Serializable
data object AboutKatibaRoute : NavKey

@Serializable
data object SendFeedbackRoute : NavKey

/**
 * SerializersModule for KMP state preservation
 * Required for iOS/Native targets where reflection is not available
 */
val navKeySerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(OnboardingRoute::class, OnboardingRoute.serializer())
        subclass(LoginRoute::class, LoginRoute.serializer())
        subclass(SignUpRoute::class, SignUpRoute.serializer())
        subclass(ForgotPasswordRoute::class, ForgotPasswordRoute.serializer())
        subclass(OTPVerificationRoute::class, OTPVerificationRoute.serializer())
        subclass(CivicDataInputRoute::class, CivicDataInputRoute.serializer())
        subclass(ResetPasswordRoute::class, ResetPasswordRoute.serializer())
        subclass(HomeRoute::class, HomeRoute.serializer())
        subclass(ConstitutionRoute::class, ConstitutionRoute.serializer())
        subclass(PlansRoute::class, PlansRoute.serializer())
        subclass(ProfileRoute::class, ProfileRoute.serializer())
        subclass(ClauseDetailRoute::class, ClauseDetailRoute.serializer())
        subclass(AIDescriptionRoute::class, AIDescriptionRoute.serializer())
        subclass(TipsRoute::class, TipsRoute.serializer())
        subclass(PreambleRoute::class, PreambleRoute.serializer())
        subclass(ChapterListRoute::class, ChapterListRoute.serializer())
        subclass(ClauseGridRoute::class, ClauseGridRoute.serializer())
        subclass(ReadingRoute::class, ReadingRoute.serializer())
        subclass(SchedulesRoute::class, SchedulesRoute.serializer())
        subclass(ScheduleDetailRoute::class, ScheduleDetailRoute.serializer())
        subclass(LessonRoute::class, LessonRoute.serializer())
        subclass(SettingsRoute::class, SettingsRoute.serializer())
        subclass(NotificationsRoute::class, NotificationsRoute.serializer())
        subclass(MzalendoRoute::class, MzalendoRoute.serializer())
        subclass(EditProfileRoute::class, EditProfileRoute.serializer())
        subclass(PasswordSecurityRoute::class, PasswordSecurityRoute.serializer())
        subclass(UpdateResidenceRoute::class, UpdateResidenceRoute.serializer())
        subclass(NationalIDRoute::class, NationalIDRoute.serializer())
        subclass(AppearanceRoute::class, AppearanceRoute.serializer())
        subclass(FontSizeRoute::class, FontSizeRoute.serializer())
        subclass(LanguageRoute::class, LanguageRoute.serializer())
        subclass(AboutKatibaRoute::class, AboutKatibaRoute.serializer())
        subclass(SendFeedbackRoute::class, SendFeedbackRoute.serializer())
    }
}
