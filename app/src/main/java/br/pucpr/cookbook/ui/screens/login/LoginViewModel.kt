package br.pucpr.cookbook.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.cookbook.data.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onToggleMode() {
        if (_uiState.value.isSubmitting) return
        _uiState.update {
            val novoModo = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
            it.copy(mode = novoModo, errorMessage = null)
        }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val erro = state.validate()
        if (erro != null) {
            _uiState.update { it.copy(errorMessage = erro) }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val resultado = if (state.mode == AuthMode.LOGIN) {
                    authRepository.login(state.email, state.password)
                } else {
                    authRepository.register(state.email, state.password)
                }
                resultado
                    .onSuccess { _uiState.update { it.copy(isSubmitting = false, loginSuccess = true) } }
                    .onFailure { e -> _uiState.update { it.copy(isSubmitting = false, errorMessage = mapError(e)) } }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = mapError(e)) }
            }
        }
    }

    // Traduz as exceptions do Firebase para mensagens em português.
    private fun mapError(e: Throwable): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "A senha precisa ter ao menos 6 caracteres."
        is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado. Faça login."
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha inválidos."
        is FirebaseNetworkException -> "Sem conexão com a internet. Tente novamente."
        else -> "Não foi possível concluir. Tente novamente."
    }
}
