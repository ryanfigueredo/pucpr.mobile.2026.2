package br.pucpr.cookbook.ui.screens.shopping

import br.pucpr.cookbook.data.local.entity.ShoppingItemEntity
import br.pucpr.cookbook.data.model.UnidadeMedida

data class ShoppingGroup(
    val titulo: String,
    val itens: List<ShoppingItemEntity>,
)

data class ShoppingUiState(
    val isLoading: Boolean = true,
    val grupos: List<ShoppingGroup> = emptyList(),
    val showClearBoughtDialog: Boolean = false,
    val showClearAllDialog: Boolean = false,
    val novoItemNome: String = "",
    val novoItemQuantidade: String = "",
    val novoItemUnidade: String = UnidadeMedida.default.label,
    // Só exibe os erros inline (isError/supportingText) depois da primeira tentativa de adicionar.
    val tentouAdicionar: Boolean = false,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = grupos.isEmpty()

    /** Aceita vírgula ou ponto como separador decimal; null se inválida ou <= 0. */
    val novoItemQuantidadeValida: Double?
        get() = novoItemQuantidade.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }

    val podeAdicionarItem: Boolean
        get() = novoItemNome.isNotBlank() && novoItemQuantidadeValida != null
}
