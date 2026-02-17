package com.katiba.app.data.service

interface GoogleSignInService {
    suspend fun signIn(): Result<String> // Returns ID Token
}
