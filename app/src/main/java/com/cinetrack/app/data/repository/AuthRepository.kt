package com.cinetrack.app.data.repository

import com.cinetrack.app.data.model.AppUser

sealed interface AuthResult {
    data class Success(val user: AppUser) : AuthResult
    data class Error(val message: String) : AuthResult
}

sealed interface AuthActionResult {
    data object Success : AuthActionResult
    data class Error(val message: String) : AuthActionResult
}

interface AuthRepository {
    val isFirebaseConfigured: Boolean
    val currentUser: AppUser?
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signUp(name: String, email: String, password: String): AuthResult
    suspend fun sendPasswordReset(email: String): AuthActionResult
    suspend fun reauthenticateCurrentUser(password: String): AuthActionResult
    suspend fun deleteCurrentAccount(): AuthActionResult
    fun signOut()
}
