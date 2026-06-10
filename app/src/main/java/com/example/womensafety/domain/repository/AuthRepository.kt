package com.example.womensafety.domain.repository

import com.example.womensafety.core.util.Resource
import com.example.womensafety.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signUp(email: String, password: String, name: String): Resource<User>
    suspend fun signIn(email: String, password: String): Resource<User>
    suspend fun signOut()
    suspend fun sendPasswordReset(email: String): Resource<Unit>
    suspend fun sendEmailVerification(): Resource<Unit>
    fun isLoggedIn(): Boolean
}
