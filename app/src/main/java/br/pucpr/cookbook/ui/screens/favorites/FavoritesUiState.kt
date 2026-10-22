package br.pucpr.cookbook.ui.screens.favorites

import br.pucpr.cookbook.data.local.entity.RecipeEntity

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val favoritos: List<RecipeEntity> = emptyList(),
    val lastRemovedId: Long? = null,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null,
)
