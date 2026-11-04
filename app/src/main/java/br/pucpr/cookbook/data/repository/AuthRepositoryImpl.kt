package br.pucpr.cookbook.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AuthRepositoryImpl(private val auth: FirebaseAuth) : AuthRepository {

    override val isLoggedIn: Boolean
        get() = auth.currentUser != null

    override val currentUserEmail: String?
        get() = auth.currentUser?.email

    override suspend fun login(email: String, password: String): Result<Unit> =
        auth.signInWithEmailAndPassword(email.trim(), password).awaitUnit()

    override suspend fun register(email: String, password: String): Result<Unit> =
        auth.createUserWithEmailAndPassword(email.trim(), password).awaitUnit()

    override fun logout() = auth.signOut()

    // Converte o Task do Firebase em uma suspend function. Em caso de falha,
    // a exception é repassada para o ViewModel traduzir em mensagem amigável.
    private suspend fun <T> Task<T>.awaitUnit(): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { if (cont.isActive) cont.resume(Result.success(Unit)) }
            addOnFailureListener { if (cont.isActive) cont.resume(Result.failure(it)) }
        }
}
