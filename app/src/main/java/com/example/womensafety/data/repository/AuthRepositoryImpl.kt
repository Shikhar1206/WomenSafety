package com.example.womensafety.data.repository

import com.example.womensafety.core.util.Resource
import com.example.womensafety.domain.model.User
import com.example.womensafety.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toDomain())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signUp(email: String, password: String, name: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            result.user?.updateProfile(profileUpdate)?.await()
            result.user?.sendEmailVerification()?.await()
            Resource.Success(result.user!!.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Sign up failed")
            Resource.Error(e.message ?: "Sign up failed", e)
        }
    }

    override suspend fun signIn(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result.user!!.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Sign in failed")
            Resource.Error(e.message ?: "Sign in failed", e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send reset email", e)
        }
    }

    override suspend fun sendEmailVerification(): Resource<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send verification", e)
        }
    }

    override fun isLoggedIn(): Boolean = auth.currentUser != null

    private fun FirebaseUser.toDomain() = User(
        uid = uid,
        email = email ?: "",
        displayName = displayName,
        phoneNumber = phoneNumber,
        photoUrl = photoUrl?.toString(),
        isEmailVerified = isEmailVerified
    )
}
