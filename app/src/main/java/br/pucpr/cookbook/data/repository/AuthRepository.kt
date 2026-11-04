package br.pucpr.cookbook.data.repository

/**
 * Abstrai o Firebase Authentication para as telas. A sessão é persistida
 * automaticamente pelo Firebase: [isLoggedIn] continua verdadeiro entre
 * aberturas do app até que [logout] seja chamado.
 */
interface AuthRepository {
    val isLoggedIn: Boolean
    val currentUserEmail: String?

    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    fun logout()
}
