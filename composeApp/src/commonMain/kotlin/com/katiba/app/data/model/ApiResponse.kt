package com.katiba.app.data.model

import kotlinx.serialization.Serializable

/**
 * Generic API response wrapper
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorResponse? = null
)
