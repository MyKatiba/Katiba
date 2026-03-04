package com.katiba.app.data.repository

import com.katiba.app.data.model.ApiResponse
import com.katiba.app.data.model.EmailNotVerifiedException
import com.katiba.app.data.model.ForgotPasswordRequest
import com.katiba.app.data.model.ForgotPasswordResponse
import com.katiba.app.data.model.LoginRequest
import com.katiba.app.data.model.LoginResponse
import com.katiba.app.data.model.LookupRequest
import com.katiba.app.data.model.LookupResponse
import com.katiba.app.data.model.MessageResponse
import com.katiba.app.data.model.RegisterRequest
import com.katiba.app.data.model.RegisterResponse
import com.katiba.app.data.model.ResendOtpRequest
import com.katiba.app.data.model.ResetPasswordRequest
import com.katiba.app.data.model.UserProfile
import com.katiba.app.data.model.VerifyEmailResponse
import com.katiba.app.data.model.VerifyOtpRequest
import com.katiba.app.data.model.VerifyResetOtpResponse
import com.katiba.app.data.AppPreferences
import com.katiba.app.data.api.ApiConfig
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

    private var accessToken: String? = null

    init {
        if (AppPreferences.isLoggedIn) {
            val savedId = AppPreferences.currentUserId!!
            _currentUser.value = UserProfile(
                id = savedId,
                name = AppPreferences.userName ?: "User",
                email = AppPreferences.userEmail ?: "",
                avatarUrl = AppPreferences.userAvatarUrl ?: "",
                emailVerified = AppPreferences.userEmailVerified,
                joinedDate = AppPreferences.userJoinedDate ?: ""
            )
            val savedAccess = AppPreferences.accessToken
            val savedRefresh = AppPreferences.refreshToken
            if (savedAccess != null && savedRefresh != null) {
                TokenManager.saveTokens(savedAccess, savedRefresh)
                accessToken = savedAccess
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            firebaseAuth.authStateChanged.collect { user ->
                if (user != null) {
                    val profile = UserProfile(
                        id = user.uid,
                        name = user.displayName ?: "User",
                        email = user.email ?: "",
                        avatarUrl = user.photoURL ?: "",
                        emailVerified = user.isEmailVerified,
                        joinedDate = "",
                    )
                    _currentUser.value = profile
                    AppPreferences.saveUserSession(
                        userId = profile.id,
                        name = profile.name,
                        email = profile.email,
                        avatarUrl = profile.avatarUrl,
                        emailVerified = profile.emailVerified,
                        joinedDate = profile.joinedDate
                    )
                } else {
                    if (accessToken == null && !AppPreferences.isLoggedIn) {
                        _currentUser.value = null
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Login
    // ─────────────────────────────────────────────────────────────────────────

    override suspend fun loginWithEmail(email: String, password: String): Result<UserProfile> {
        return try {
            val response: ApiResponse<LoginResponse> = client.post("auth/login") {
                setBody(LoginRequest(email, password))
            }.body()

            if (response.success && response.data != null) {
                val loginData = response.data
                TokenManager.saveTokens(loginData.accessToken, loginData.refreshToken)
                accessToken = loginData.accessToken
                _currentUser.value = loginData.user
                AppPreferences.saveUserSession(
                    userId = loginData.user.id,
                    name = loginData.user.name,
                    email = loginData.user.email,
                    avatarUrl = loginData.user.avatarUrl,
                    emailVerified = loginData.user.emailVerified,
                    joinedDate = loginData.user.joinedDate,
                    access = loginData.accessToken,
                    refresh = loginData.refreshToken
                )
                Result.success(loginData.user)
            } else {
                // Detect unverified-email error by message text or structured code
                if (isEmailNotVerifiedError(response)) {
                    val inlineUserId = response.errorUserId()?.takeIf { it.isNotBlank() }
                    if (inlineUserId != null) {
                        // userId came straight from the error body
                        Result.failure(EmailNotVerifiedException(inlineUserId, email))
                    } else {
                        // Fall back to /auth/lookup to get the userId
                        val lookedUpId = lookupUserByEmailInternal(email)
                        if (lookedUpId != null) {
                            Result.failure(EmailNotVerifiedException(lookedUpId, email))
                        } else {
                            Result.failure(EmailNotVerifiedException("", email, response.errorMessage()))
                        }
                    }
                } else {
                    Result.failure(Exception(response.errorMessage()))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Register
    // ─────────────────────────────────────────────────────────────────────────

    override suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<String> {
        return try {
            val response: ApiResponse<RegisterResponse> = client.post("auth/register") {
                setBody(RegisterRequest(name, email, password, confirmPassword))
            }.body()

            if (response.success && response.data != null) {
                Result.success(response.data.userId)
            } else {
                // EMAIL_EXISTS: the account exists but may not be verified.
                // Call /auth/lookup to get the userId, then the OTP screen will resend.
                if (isEmailExistsError(response)) {
                    val inlineUserId = response.errorUserId()?.takeIf { it.isNotBlank() }
                    if (inlineUserId != null) {
                        Result.failure(EmailNotVerifiedException(inlineUserId, email))
                    } else {
                        val lookedUpId = lookupUserByEmailInternal(email)
                        if (lookedUpId != null) {
                            Result.failure(EmailNotVerifiedException(lookedUpId, email))
                        } else {
                            // lookup failed – surface the original error
                            Result.failure(Exception(response.errorMessage()))
                        }
                    }
                } else {
                    Result.failure(Exception(humaniseRegistrationError(response.errorMessage())))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lookup  (public – satisfies the interface)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calls POST /api/auth/lookup with the user's email.
     * Returns the userId if the account exists (verified or not).
     * Returns a failure if the email is not found or the call fails.
     */
    override suspend fun lookupUserByEmail(email: String): Result<String> {
        return try {
            val response: ApiResponse<LookupResponse> = client.post("auth/lookup") {
                setBody(LookupRequest(email))
            }.body()

            if (response.success && response.data != null) {
                Result.success(response.data.userId)
            } else {
                Result.failure(Exception(response.errorMessage()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Internal convenience wrapper – returns userId string or null (never throws).
     * Used by loginWithEmail / registerWithEmail as a fallback.
     */
    private suspend fun lookupUserByEmailInternal(email: String): String? =
        lookupUserByEmail(email).getOrNull()

    // ─────────────────────────────────────────────────────────────────────────
    // Error-shape helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * True when the backend signals that this email already exists.
     * Matches the structured error code "EMAIL_EXISTS" and common message text.
     */
    private fun isEmailExistsError(response: ApiResponse<*>): Boolean {
        if (response.errorCode() == "EMAIL_EXISTS") return true
        val msg = response.errorMessage().lowercase()
        return msg.contains("already registered") ||
               msg.contains("email already") ||
               msg.contains("already exists")
    }

    /**
     * True when the backend signals the account exists but the email is not yet verified.
     * Matches the structured error code "EMAIL_NOT_VERIFIED" and common message text.
     */
    private fun isEmailNotVerifiedError(response: ApiResponse<*>): Boolean {
        if (response.errorCode() == "EMAIL_NOT_VERIFIED") return true
        val msg = response.errorMessage().lowercase()
        return msg.contains("verify your email") ||
               msg.contains("email not verified") ||
               msg.contains("not verified") ||
               msg.contains("verify your account") ||
               msg.contains("email verification")
    }

    /**
     * Turns a generic backend validation message into a user-friendly hint.
     * The backend returns "Invalid input" for password-strength failures without
     * explaining which rule was violated.
     */
    private fun humaniseRegistrationError(raw: String): String {
        val lower = raw.lowercase()
        if (lower.contains("invalid input") || lower.contains("validation")) {
            return "Registration failed. Please ensure your password is at least 8 characters " +
                   "and contains an uppercase letter, a number, and a special character (e.g. !@#\$)."
        }
        return raw
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Verify email
    // ─────────────────────────────────────────────────────────────────────────

    override suspend fun verifyEmail(userId: String, otp: String): Result<UserProfile> {
        return try {
            val response: ApiResponse<VerifyEmailResponse> = client.post("auth/verify-email") {
                setBody(VerifyOtpRequest(userId, otp))
            }.body()

            if (response.success && response.data != null) {
                val verifyData = response.data
                TokenManager.saveTokens(verifyData.accessToken, verifyData.refreshToken)
                accessToken = verifyData.accessToken
                _currentUser.value = verifyData.user
                AppPreferences.saveUserSession(
                    userId = verifyData.user.id,
                    name = verifyData.user.name,
                    email = verifyData.user.email,
                    avatarUrl = verifyData.user.avatarUrl,
                    emailVerified = verifyData.user.emailVerified,
                    joinedDate = verifyData.user.joinedDate,
                    access = verifyData.accessToken,
                    refresh = verifyData.refreshToken
                )
                Result.success(verifyData.user)
            } else {
                Result.failure(Exception(response.errorMessage()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Resend OTP
    // ─────────────────────────────────────────────────────────────────────────

    override suspend fun resendOtp(userId: String, purpose: String): Result<String> {
        return try {
            val response: ApiResponse<MessageResponse> = client.post("auth/resend-otp") {
                setBody(ResendOtpRequest(userId, purpose))
            }.body()

            if (response.success) {
                Result.success(response.data?.message ?: "OTP resent successfully")
            } else {
                Result.failure(Exception(response.errorMessage()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Password reset
    // ─────────────────────────────────────────────────────────────────────────

    override suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response: ApiResponse<ForgotPasswordResponse> = client.post("auth/forgot-password") {
                setBody(ForgotPasswordRequest(email))
            }.body()

            if (response.success && response.data != null) {
                Result.success(response.data.userId)
            } else {
                Result.failure(Exception(response.errorMessage()))
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
                Result.failure(Exception(response.errorMessage()))
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
                Result.failure(Exception(response.errorMessage()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Google Auth
    // ─────────────────────────────────────────────────────────────────────────

    override suspend fun loginWithGoogle(idToken: String): Result<UserProfile> {
        return try {
            val credential = dev.gitlive.firebase.auth.GoogleAuthProvider.credential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential)
            val user = result.user ?: return Result.failure(Exception("User is null"))

            val firebaseIdToken = user.getIdToken(false)
            if (firebaseIdToken != null) {
                TokenManager.saveTokens(firebaseIdToken, firebaseIdToken)
            }

            val profile = UserProfile(
                id = user.uid,
                name = user.displayName ?: "User",
                email = user.email ?: "",
                avatarUrl = user.photoURL ?: "",
                emailVerified = user.isEmailVerified,
                joinedDate = ""
            )
            _currentUser.value = profile
            AppPreferences.saveUserSession(
                userId = profile.id,
                name = profile.name,
                email = profile.email,
                avatarUrl = profile.avatarUrl,
                emailVerified = profile.emailVerified,
                joinedDate = profile.joinedDate,
                access = firebaseIdToken,
                refresh = firebaseIdToken
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sign out
    // ─────────────────────────────────────────────────────────────────────────

    override suspend fun signOut() {
        firebaseAuth.signOut()
        TokenManager.clearTokens()
        AppPreferences.clearSession()
        accessToken = null
        _currentUser.value = null
    }
}
