package com.katiba.app.data.api

import com.katiba.app.data.model.ApiErrorResponse
import com.katiba.app.data.model.UpdateProfileRequest
import com.katiba.app.data.model.UserProfile
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * API client for user profile endpoints
 */
class UserApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
        }
        
        defaultRequest {
            url(ApiConfig.BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }
    
    /**
     * Get current user profile
     * Requires authentication
     */
    suspend fun getProfile(): Result<UserProfile> = try {
        val token = TokenManager.getAccessToken()
            ?: return@try Result.failure(Exception("Not authenticated"))
        
        val response = client.get("/api/users/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        
        if (response.status.isSuccess()) {
            Result.success(response.body<UserProfile>())
        } else if (response.status == HttpStatusCode.Unauthorized) {
            TokenManager.clearTokens()
            Result.failure(Exception("Session expired. Please login again."))
        } else {
            val error = response.body<ApiErrorResponse>()
            Result.failure(Exception(error.message ?: error.error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Update user profile
     * Requires authentication
     */
    suspend fun updateProfile(
        name: String? = null,
        county: String? = null,
        constituency: String? = null,
        ward: String? = null,
        nationalId: String? = null,
        isRegisteredVoter: Boolean? = null
    ): Result<UserProfile> = try {
        val token = TokenManager.getAccessToken()
            ?: return@try Result.failure(Exception("Not authenticated"))
        
        val response = client.put("/api/users/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(UpdateProfileRequest(
                name = name,
                county = county,
                constituency = constituency,
                ward = ward,
                nationalId = nationalId,
                isRegisteredVoter = isRegisteredVoter
            ))
        }
        
        if (response.status.isSuccess()) {
            Result.success(response.body<UserProfile>())
        } else if (response.status == HttpStatusCode.Unauthorized) {
            TokenManager.clearTokens()
            Result.failure(Exception("Session expired. Please login again."))
        } else {
            val error = response.body<ApiErrorResponse>()
            Result.failure(Exception(error.message ?: error.error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
