package com.katiba.app.data.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages authentication tokens
 * Platform-specific implementations should handle secure storage
 */
object TokenManager {
    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()
    
    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    fun saveTokens(accessToken: String, refreshToken: String) {
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
        _isAuthenticated.value = true
        // TODO: Persist to secure storage (platform-specific)
    }
    
    fun getAccessToken(): String? = _accessToken.value
    
    fun getRefreshToken(): String? = _refreshToken.value
    
    fun clearTokens() {
        _accessToken.value = null
        _refreshToken.value = null
        _isAuthenticated.value = false
        // TODO: Clear from secure storage (platform-specific)
    }
    
    fun isLoggedIn(): Boolean = _isAuthenticated.value
}
