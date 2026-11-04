package br.pucpr.cookbook.ui.screens.cooking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.cookbook.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val KEY_STEP = "cooking_step"


@HiltViewModel
class CookingModeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recipeId: Long = savedStateHandle.get<Long>("recipeId") ?: -1L

    // 0 = usar as porções da própria receita.
    private val porcoesSolicitadas: Int = savedStateHandle.get<Int>("porcoes") ?: 0

    private val _uiState = MutableStateFlow(
        CookingUiState(currentStep = (savedStateHandle.get<Int>(KEY_STEP) ?: 0).coerceAtLeast(0)),
    )
    val uiState: StateFlow<CookingUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        if (recipeId <= 0L) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Receita inválida.") }
        } else {
            recipeRepository.observeRecipeWithIngredients(recipeId)
                .onEach { data ->
                    if (data == null) {
                        _uiState.update { it.copy(isLoading = false, recipe = null) }
                    } else {
                        val steps = data.recipe.modoPreparo.filter { it.isNotBlank() }
                        val porcoes = if (porcoesSolicitadas > 0) porcoesSolicitadas else data.recipe.porcoes
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                recipe = data.recipe,
                                ingredients = data.ingredients,
                                steps = steps,
                                porcoes = porcoes,
                                currentStep = current.currentStep.coerceIn(0, maxOf(steps.lastIndex, 0)),
                            )
                        }
                    }
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Não foi possível carregar a receita.") }
                }
                .launchIn(viewModelScope)
        }
    }

    // --- Etapas ---

    fun onNextStep() {
        _uiState.update { s -> if (s.steps.isNotEmpty() && !s.isLastStep) s.copy(currentStep = s.currentStep + 1) else s }
        savedStateHandle[KEY_STEP] = _uiState.value.currentStep
    }

    fun onPreviousStep() {
        _uiState.update { s -> if (!s.isFirstStep) s.copy(currentStep = s.currentStep - 1) else s }
        savedStateHandle[KEY_STEP] = _uiState.value.currentStep
    }

    fun onFinishRecipe() {
        if (!_uiState.value.isLastStep) return
        cancelTimer()
        _uiState.update {
            it.copy(finished = true, timerStatus = TimerStatus.IDLE, timerRemainingSeconds = it.timerConfiguredSeconds)
        }
    }

    // --- Ingredientes ---

    fun onShowIngredients() = _uiState.update { it.copy(showIngredients = true) }

    fun onHideIngredients() = _uiState.update { it.copy(showIngredients = false) }

    fun onToggleIngredient(id: Long) {
        _uiState.update { s ->
            val marcados = if (id in s.checkedIngredientIds) s.checkedIngredientIds - id else s.checkedIngredientIds + id
            s.copy(checkedIngredientIds = marcados)
        }
    }

    // --- Temporizador (manual: o modelo atual não guarda duração por etapa) ---

    fun onTimerMinutesChange(delta: Int) {
        onTimerSetMinutes(_uiState.value.timerConfiguredSeconds / 60 + delta)
    }

    fun onTimerSetMinutes(minutes: Int) {
        val segundos = minutes.coerceIn(MIN_TIMER_MINUTES, MAX_TIMER_MINUTES) * 60
        _uiState.update { s ->
            if (s.timerStatus == TimerStatus.IDLE) {
                s.copy(timerConfiguredSeconds = segundos, timerRemainingSeconds = segundos)
            } else {
                s
            }
        }
    }

    fun onTimerStart() {
        val s = _uiState.value
        if (s.timerStatus != TimerStatus.IDLE || s.timerConfiguredSeconds <= 0) return
        _uiState.update { it.copy(timerStatus = TimerStatus.RUNNING, timerRemainingSeconds = it.timerConfiguredSeconds) }
        runTimer()
    }

    fun onTimerPause() {
        if (_uiState.value.timerStatus != TimerStatus.RUNNING) return
        cancelTimer()
        _uiState.update { it.copy(timerStatus = TimerStatus.PAUSED) }
    }

    fun onTimerResume() {
        if (_uiState.value.timerStatus != TimerStatus.PAUSED) return
        _uiState.update { it.copy(timerStatus = TimerStatus.RUNNING) }
        runTimer()
    }

    fun onTimerReset() {
        cancelTimer()
        _uiState.update { it.copy(timerStatus = TimerStatus.IDLE, timerRemainingSeconds = it.timerConfiguredSeconds) }
    }

    private fun runTimer() {
        timerJob?.cancel() // garante uma única contagem ativa
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerRemainingSeconds > 0) {
                delay(1_000)
                _uiState.update { it.copy(timerRemainingSeconds = (it.timerRemainingSeconds - 1).coerceAtLeast(0)) }
            }
            _uiState.update { it.copy(timerStatus = TimerStatus.FINISHED) }
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}
