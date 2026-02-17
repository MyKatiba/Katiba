package com.katiba.app.data.repository

import com.katiba.app.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    
    // Email/Password Auth (Backend)
    suspend fun loginWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun registerWithEmail(name: String, email: String, password: String, confirmPassword: String): Result<String> // Returns userId
    suspend fun verifyEmail(userId: String, otp: String): Result<UserProfile>
    
    // OTP & Password Reset
    suspend fun resendOtp(userId: String, purpose: String): Result<String>
    suspend fun forgotPassword(email: String): Result<String> // Returns userId
    suspend fun verifyResetOtp(userId: String, otp: String): Result<String> // Returns resetToken
    suspend fun resetPassword(token: String, password: String): Result<String>

    // Google Auth (Firebase)
    suspend fun loginWithGoogle(idToken: String): Result<UserProfile>
    
    // Common
    suspend fun signOut()
}
