package com.katiba.app.data.model

import kotlinx.serialization.Serializable

/**
 * Generic API response wrapper.
 * Captures both structured { error: { message, userId } } and flat
 * { success: false, message: "...", userId: "..." } error shapes.
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorResponse? = null,
    // Top-level flat error fields some backends return
    val message: String? = null,
    val userId: String? = null
) {
    /** Resolves the error message from whichever location the backend put it */
    fun errorMessage(): String =
        error?.message ?: error?.error ?: message ?: "Unknown error"

    /** Resolves the userId from the error payload (nested or flat) */
    fun errorUserId(): String? = error?.userId ?: userId

    /** Resolves the structured error code, e.g. "EMAIL_EXISTS", "EMAIL_NOT_VERIFIED" */
    fun errorCode(): String? = error?.code
}
