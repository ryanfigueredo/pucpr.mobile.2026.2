package br.pucpr.cookbook.ui.screens.planner

import br.pucpr.cookbook.data.local.entity.MealType
import br.pucpr.cookbook.data.local.entity.PlannedMealEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.local.relation.PlannedMealWithRecipe
import br.pucpr.cookbook.ui.components.formatDate
import br.pucpr.cookbook.ui.components.parseDate
import br.pucpr.cookbook.ui.components.startOfWeek
import java.time.LocalDate

data class MealFormState(
    val id: Long = 0L,
    val recipeId: Long? = null,
    val data: String = formatDate(LocalDate.now().toEpochDay()),
    val tipo: MealType = MealType.ALMOCO,
    val porcoes: String = "1",
    val errorMessage: String? = null,
) {
    fun validate(): String? = when {
        recipeId == null -> "Escolha uma receita."
        parseDate(data) == null -> "Informe uma data válida no formato dd/mm/aaaa."
        porcoes.toIntOrNull()?.takeIf { it > 0 } == null -> "Informe um número de porções maior que zero."
        else -> null
    }

    fun toEntity(): PlannedMealEntity = PlannedMealEntity(
        id = id,
        recipeId = checkNotNull(recipeId),
        dia = checkNotNull(parseDate(data)),
        tipo = tipo,
        porcoes = porcoes.toInt(),
    )
}

data class PlannerUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val inicioSemana: Long = startOfWeek(LocalDate.now().toEpochDay()),
    val meals: List<PlannedMealWithRecipe> = emptyList(),
    val recipes: List<RecipeEntity> = emptyList(),
    val formState: MealFormState? = null,
    val pendingDelete: PlannedMealEntity? = null,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null,
)
