package br.pucpr.cookbook.ui.screens.details

import br.pucpr.cookbook.data.local.entity.IngredientEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.ui.components.RecipeFormState

data class DetailsUiState(
    val isLoading: Boolean = true,
    val recipe: RecipeEntity? = null,
    val ingredients: List<IngredientEntity> = emptyList(),
    val porcoesAjustadas: Int = 0,
    val formState: RecipeFormState? = null,
    val showDeleteDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null,
    val deleted: Boolean = false,
)
