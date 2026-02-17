package com.katiba.app.data.api

import com.katiba.app.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * API client for authentication endpoints
 */
class AuthApiClient {
    private val client by lazy {
        HttpClient {
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
    }
    
    /**
     * Register a new user
     * Returns userId for OTP verification
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<RegisterResponse> = try {
        val response = client.post("/api/auth/register") {
            setBody(RegisterRequest(name, email, password, confirmPassword))
        }
        
        if (response.status.isSuccess()) {
            Result.success(response.body<RegisterResponse>())
        } else {
            val error = response.body<ApiErrorResponse>()
            Result.failure(Exception(error.message ?: error.error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Verify email with OTP
     * Returns user profile and authentication tokens
     */
    suspend fun verifyEmail(
        userId: String,
        otp: String
    ): Result<VerifyEmailResponse> = try {
        val response = client.post("/api/auth/verify-email") {
            setBody(VerifyOtpRequest(userId, otp))
        }
        
        if (response.status.isSuccess()) {
            val verifyResponse = response.body<VerifyEmailResponse>()
            // Save tokens
            TokenManager.saveTokens(verifyResponse.accessToken, verifyResponse.refreshToken)
            Result.success(verifyResponse)
        } else {
            val error = response.body<ApiErrorResponse>()
            Result.failure(Exception(error.message ?: error.error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Resend OTP for email verification or password reset
     */
    suspend fun resendOtp(
        userId: String,
        purpose: String
    ): Result<MessageResponse> = try {
        val response = client.post("/api/auth/resend-otp") {
            setBody(ResendOtpRequest(userId, purpose))
        }
        
        if (response.status.isSuccess()) {
            Result.success(response.body<MessageResponse>())
        } else {
            val error = response.body<ApiErrorResponse>()
            Result.failure(Exception(error.message ?: error.error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Request password reset OTP
     * Returns userId for OTP verification
     */
    suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse> = try {
        val response = client.post("/api/auth/forgot-password") {
            setBody(ForgotPasswordRequest(email))
        }
        
        if (response.status.isSuccess()) {
            Result.success(response.body<ForgotPasswordResponse>())
        } else {
            val error = response.body<ApiErrorResponse>()
            Result.failure(Exception(error.message ?: error.error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Verify password reset OTP
     * Returns short-lived reset token
     */
    suspend fun verifyResetOtp(
        userId: String,
        otp: String
    ): Result<VerifyResetOtpResponse> = try {
        val response = client.post("/api/auth/verify-reset-otp") {
            setBody(VerifyOtpRequest(userId, otp))
        }
        
        if (response.status.isSuccess()) {
            Result.success(response.body<VerifyResetOtpResponse>())
        } else {
            val error = response.body<ApiErrorResponse>()
            Result.failure(Exception(error.message ?: error.error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Reset password with token from OTP verification
     */
    suspend fun resetPassword(
        token: String,
        password: String
    ): Result<MessageResponse> = try {
        val response = client.post("/api/auth/reset-password") {
            setBody(ResetPasswordRequest(token, password))
        }
        
        if (response.status.isSuccess()) {
            Result.success(response.body<MessageResponse>())
        } else {
            val error = response.body<ApiErrorResponse>()
            Result.failure(Exception(error.message ?: error.error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Login user
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<LoginResponse> = try {
        val response = client.post("/api/auth/login") {
            setBody(LoginRequest(email, password))
        }
        
        if (response.status.isSuccess()) {
            val loginResponse = response.body<LoginResponse>()
            // Save tokens
            TokenManager.saveTokens(loginResponse.accessToken, loginResponse.refreshToken)
            Result.success(loginResponse)
        } else {
            val error = response.body<ApiErrorResponse>()
            Result.failure(Exception(error.message ?: error.error))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    /**
     * Logout user
     */
    fun logout() {
        TokenManager.clearTokens()
    }
}
