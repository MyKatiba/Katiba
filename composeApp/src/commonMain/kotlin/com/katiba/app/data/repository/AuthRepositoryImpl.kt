package com.katiba.app.data.repository

import com.katiba.app.data.model.ApiResponse
import com.katiba.app.data.model.UserProfile
import com.katiba.app.data.model.LoginRequest
import com.katiba.app.data.model.LoginResponse
import com.katiba.app.data.model.RegisterRequest
import com.katiba.app.data.model.RegisterResponse
import com.katiba.app.data.model.VerifyOtpRequest
import com.katiba.app.data.api.ApiConfig
import com.katiba.app.data.model.VerifyEmailResponse
import com.katiba.app.data.model.ResendOtpRequest
import com.katiba.app.data.model.MessageResponse
import com.katiba.app.data.model.ForgotPasswordRequest
import com.katiba.app.data.model.ForgotPasswordResponse
import com.katiba.app.data.model.VerifyResetOtpResponse
import com.katiba.app.data.model.ResetPasswordRequest
import com.katiba.app.data.api.TokenManager
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AuthRepositoryImpl : AuthRepository {
    private val firebaseAuth: FirebaseAuth = Firebase.auth
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    override val currentUser: Flow<UserProfile?> = _currentUser.asStateFlow()

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            url("${ApiConfig.BASE_URL}/api/")
        }
    }
    
    // In-memory token storage for now
    private var accessToken: String? = null

    init {
        // Listen to Firebase Auth state
        CoroutineScope(Dispatchers.IO).launch {
            firebaseAuth.authStateChanged.collect { user ->
                if (user != null) {
                    // Map Firebase User to UserProfile
                    _currentUser.value = UserProfile(
                        id = user.uid,
                        name = user.displayName ?: "User",
                        email = user.email ?: "",
                        avatarUrl = user.photoURL ?: "",
                        emailVerified = user.isEmailVerified,
                        joinedDate = "", // API doesn't provide generic date easily, use current or empty
                    )
                } else {
                    // Only clear if no access token (Backend session)
                    if (accessToken == null) {
                        _currentUser.value = null
                    }
                }
            }
        }
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<UserProfile> {
        return try {
            val response: ApiResponse<LoginResponse> = client.post("auth/login") {
                setBody(LoginRequest(email, password))
            }.body()

            if (response.success && response.data != null) {
                val loginData = response.data
                // Use TokenManager
                TokenManager.saveTokens(loginData.accessToken, loginData.refreshToken)
                accessToken = loginData.accessToken
                _currentUser.value = loginData.user
                Result.success(loginData.user)
            } else {
                Result.failure(Exception(response.error?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(name: String, email: String, password: String, confirmPassword: String): Result<String> {
        return try {
            val response: ApiResponse<RegisterResponse> = client.post("auth/register") {
                setBody(RegisterRequest(name, email, password, confirmPassword))
            }.body()
            
            if (response.success && response.data != null) {
                Result.success(response.data.userId)
            } else {
                 Result.failure(Exception(response.error?.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyEmail(userId: String, otp: String): Result<UserProfile> {
        return try {
             val response: ApiResponse<VerifyEmailResponse> = client.post("auth/verify-email") {
                setBody(VerifyOtpRequest(userId, otp))
            }.body()
            
            if (response.success && response.data != null) {
                val verifyData = response.data
                // Use TokenManager
                TokenManager.saveTokens(verifyData.accessToken, verifyData.refreshToken)
                accessToken = verifyData.accessToken
                _currentUser.value = verifyData.user
                Result.success(verifyData.user)
            } else {
                 Result.failure(Exception(response.error?.message ?: "Verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resendOtp(userId: String, purpose: String): Result<String> {
        return try {
            val response: ApiResponse<MessageResponse> = client.post("auth/resend-otp") {
                setBody(ResendOtpRequest(userId, purpose))
            }.body()

            if (response.success) {
                Result.success(response.data?.message ?: "OTP resent successfully")
            } else {
                Result.failure(Exception(response.error?.message ?: "Failed to resend OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response: ApiResponse<ForgotPasswordResponse> = client.post("auth/forgot-password") {
                setBody(ForgotPasswordRequest(email))
            }.body()

            if (response.success && response.data != null) {
                Result.success(response.data.userId)
            } else {
                Result.failure(Exception(response.error?.message ?: "Failed to initiate password reset"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyResetOtp(userId: String, otp: String): Result<String> {
        return try {
            val response: ApiResponse<VerifyResetOtpResponse> = client.post("auth/verify-reset-otp") {
                setBody(VerifyOtpRequest(userId, otp))
            }.body()

            if (response.success && response.data != null) {
                Result.success(response.data.resetToken)
            } else {
                Result.failure(Exception(response.error?.message ?: "Failed to verify OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(token: String, password: String): Result<String> {
        return try {
            val response: ApiResponse<MessageResponse> = client.post("auth/reset-password") {
                setBody(ResetPasswordRequest(token, password))
            }.body()

            if (response.success) {
                Result.success(response.data?.message ?: "Password reset successfully")
            } else {
                Result.failure(Exception(response.error?.message ?: "Failed to reset password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<UserProfile> {
        // This is handled via Firebase on the client side usually.
        // If we get an ID Token, we sign in to Firebase with it.
        try {
            val credential = dev.gitlive.firebase.auth.GoogleAuthProvider.credential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential)
            val user = result.user
            if (user != null) {
                 val profile = UserProfile(
                    id = user.uid,
                    name = user.displayName ?: "User",
                    email = user.email ?: "",
                    avatarUrl = user.photoURL ?: "",
                    emailVerified = user.isEmailVerified,
                    joinedDate = ""
                )
                 _currentUser.value = profile
                 return Result.success(profile)
            }
             return Result.failure(Exception("User is null"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        TokenManager.clearTokens()
        accessToken = null
        _currentUser.value = null
    }
}
