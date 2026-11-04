package br.pucpr.cookbook.ui.screens.login

import androidx.lifecycle.ViewModel
import br.pucpr.cookbook.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Expõe a sessão atual e a ação de sair para telas já autenticadas (ex.: Home). */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val currentUserEmail: String?
        get() = authRepository.currentUserEmail

    fun logout() = authRepository.logout()
}
