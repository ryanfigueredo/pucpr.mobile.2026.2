package br.pucpr.cookbook.ui.screens.login

enum class AuthMode { LOGIN, REGISTER }

data class LoginUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false,
) {
    val emailValido: Boolean
        get() = email.trim().matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))

    val senhaValida: Boolean
        get() = password.length >= 6

    val podeEnviar: Boolean
        get() = emailValido && senhaValida && !isSubmitting

    /** Rótulos que mudam conforme o modo (entrar x criar conta). */
    val tituloAcao: String
        get() = if (mode == AuthMode.LOGIN) "Entrar" else "Criar conta"

    val alternarTexto: String
        get() = if (mode == AuthMode.LOGIN) {
            "Não tem conta? Cadastre-se"
        } else {
            "Já tem conta? Entrar"
        }

    fun validate(): String? = when {
        !emailValido -> "Informe um e-mail válido."
        !senhaValida -> "A senha precisa ter ao menos 6 caracteres."
        else -> null
    }
}
