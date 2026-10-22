package br.pucpr.cookbook.ui.screens.explore

import br.pucpr.cookbook.data.local.entity.RecipeCategory
import br.pucpr.cookbook.data.local.entity.RecipeDifficulty
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.ui.components.RecipeFormState

data class ExploreUiState(
    val isLoading: Boolean = true,
    val termoBusca: String = "",
    val categoriaSelecionada: RecipeCategory? = null,
    val dificuldadeSelecionada: RecipeDifficulty? = null,
    val recipes: List<RecipeEntity> = emptyList(),
    val formState: RecipeFormState? = null,
    val errorMessage: String? = null,
)
