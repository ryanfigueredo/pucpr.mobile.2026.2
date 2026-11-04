package br.pucpr.cookbook.ui.screens.cooking

import br.pucpr.cookbook.data.local.entity.IngredientEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity

const val MIN_TIMER_MINUTES = 1
const val MAX_TIMER_MINUTES = 180
const val DEFAULT_TIMER_SECONDS = 5 * 60

enum class TimerStatus { IDLE, RUNNING, PAUSED, FINISHED }

data class CookingUiState(
    val isLoading: Boolean = true,
    val recipe: RecipeEntity? = null,
    val ingredients: List<IngredientEntity> = emptyList(),
    // Etapas = modoPreparo da receita, na ordem original (apenas entradas em branco são ignoradas).
    val steps: List<String> = emptyList(),
    val currentStep: Int = 0,
    // Porções usadas no preparo (ajustadas nos Detalhes ou as da própria receita). Não altera o banco.
    val porcoes: Int = 0,
    val checkedIngredientIds: Set<Long> = emptySet(),
    val showIngredients: Boolean = false,
    val timerStatus: TimerStatus = TimerStatus.IDLE,
    val timerConfiguredSeconds: Int = DEFAULT_TIMER_SECONDS,
    val timerRemainingSeconds: Int = DEFAULT_TIMER_SECONDS,
    val finished: Boolean = false,
    val errorMessage: String? = null,
) {
    val totalSteps: Int get() = steps.size
    val isFirstStep: Boolean get() = currentStep <= 0
    val isLastStep: Boolean get() = steps.isNotEmpty() && currentStep >= steps.lastIndex
    val progress: Float get() = if (steps.isEmpty()) 0f else (currentStep + 1).toFloat() / steps.size

    /** Fator aplicado às quantidades dos ingredientes (porções do preparo ÷ porções da receita). */
    val fatorPorcoes: Double
        get() {
            val base = recipe?.porcoes ?: return 1.0
            return if (base > 0 && porcoes > 0) porcoes.toDouble() / base else 1.0
        }
}
