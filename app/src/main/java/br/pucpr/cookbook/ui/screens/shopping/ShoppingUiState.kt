package br.pucpr.cookbook.ui.screens.shopping

import br.pucpr.cookbook.data.local.entity.ShoppingItemEntity

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
    val novoItemUnidade: String = "",
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = grupos.isEmpty()
}
