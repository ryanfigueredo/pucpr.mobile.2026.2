package br.pucpr.cookbook.ui.screens.home

import br.pucpr.cookbook.data.local.entity.RecipeCategory
import br.pucpr.cookbook.data.local.entity.RecipeEntity

data class HomeUiState(
    val isLoading: Boolean = true,
    val destaque: List<RecipeEntity> = emptyList(),
    val recentes: List<RecipeEntity> = emptyList(),
    val categorias: List<RecipeCategory> = RecipeCategory.entries,
    val errorMessage: String? = null,
)
