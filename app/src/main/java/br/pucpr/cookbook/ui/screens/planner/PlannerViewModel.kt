package br.pucpr.cookbook.ui.screens.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.cookbook.data.local.entity.PlannedMealEntity
import br.pucpr.cookbook.data.repository.NewIngredient
import br.pucpr.cookbook.data.repository.PlannerRepository
import br.pucpr.cookbook.data.repository.RecipeRepository
import br.pucpr.cookbook.data.repository.ShoppingRepository
import br.pucpr.cookbook.ui.components.formatDate
import br.pucpr.cookbook.ui.components.startOfWeek
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val plannerRepository: PlannerRepository,
    private val recipeRepository: RecipeRepository,
    private val shoppingRepository: ShoppingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()
    private val semana = MutableStateFlow(_uiState.value.inicioSemana)

    init {
        val meals = semana.flatMapLatest { inicio ->
            plannerRepository.observeWeek(inicio, inicio + 6).catch {
                _uiState.update { it.copy(errorMessage = "Não foi possível carregar o planejamento.") }
                emit(emptyList())
            }
        }
        combine(meals, recipeRepository.observeAllRecipes()) { planejadas, receitas ->
            planejadas.sortedWith(compareBy({ it.meal.dia }, { it.meal.tipo.ordinal }, { it.meal.id })) to
                receitas.sortedBy { it.nome }
        }
            .onEach { (planejadas, receitas) ->
                _uiState.update { it.copy(isLoading = false, meals = planejadas, recipes = receitas) }
            }
            .catch { _uiState.update { it.copy(isLoading = false, errorMessage = "Não foi possível carregar as receitas.") } }
            .launchIn(viewModelScope)
    }

    fun onChangeWeek(deslocamento: Long) = selectWeek(_uiState.value.inicioSemana + deslocamento * 7)

    fun onCurrentWeek() = selectWeek(startOfWeek(LocalDate.now().toEpochDay()))

    private fun selectWeek(inicio: Long) {
        if (inicio == semana.value) return
        _uiState.update { it.copy(inicioSemana = inicio, isLoading = true, meals = emptyList()) }
        semana.value = inicio
    }

    fun onOpenForm() {
        if (_uiState.value.isSaving) return
        val inicio = _uiState.value.inicioSemana
        val hoje = LocalDate.now().toEpochDay()
        val dia = if (hoje in inicio..inicio + 6) hoje else inicio
        _uiState.update { it.copy(formState = MealFormState(data = formatDate(dia))) }
    }

    fun onEditMeal(meal: PlannedMealEntity) {
        if (_uiState.value.isSaving) return
        _uiState.update {
            it.copy(formState = MealFormState(meal.id, meal.recipeId, formatDate(meal.dia), meal.tipo, meal.porcoes.toString()))
        }
    }

    fun onFormChange(form: MealFormState) {
        if (!_uiState.value.isSaving) _uiState.update { it.copy(formState = form.copy(errorMessage = null)) }
    }

    fun onDismissForm() {
        if (!_uiState.value.isSaving) _uiState.update { it.copy(formState = null) }
    }

    fun onSaveForm() {
        val state = _uiState.value
        val form = state.formState ?: return
        if (state.isSaving) return
        val erro = form.validate() ?: if (state.recipes.none { it.id == form.recipeId }) "Escolha uma receita disponível." else null
        if (erro != null) {
            _uiState.update { it.copy(formState = form.copy(errorMessage = erro)) }
            return
        }
        val meal = form.toEntity()
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                if (meal.id == 0L) plannerRepository.createMeal(meal) else plannerRepository.updateMeal(meal)
                selectWeek(startOfWeek(meal.dia))
                _uiState.update { it.copy(formState = null, snackbarMessage = "Refeição salva.") }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(formState = form.copy(errorMessage = "Não foi possível salvar a refeição.")) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onRequestDelete(meal: PlannedMealEntity) {
        if (!_uiState.value.isSaving) _uiState.update { it.copy(pendingDelete = meal) }
    }

    fun onDismissDelete() {
        if (!_uiState.value.isSaving) _uiState.update { it.copy(pendingDelete = null) }
    }

    fun onConfirmDelete() {
        val meal = _uiState.value.pendingDelete ?: return
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                plannerRepository.deleteMeal(meal)
                _uiState.update { it.copy(pendingDelete = null, snackbarMessage = "Refeição removida do planejamento.") }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(errorMessage = "Não foi possível excluir a refeição.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onAddToShopping(meal: PlannedMealEntity) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val receita = checkNotNull(recipeRepository.observeRecipeWithIngredients(meal.recipeId).first())
                val fator = meal.porcoes.toDouble() / receita.recipe.porcoes
                val ingredientes = receita.ingredients.map { NewIngredient(it.nome, it.quantidade * fator, it.unidade) }
                shoppingRepository.addIngredientsFromRecipe(ingredientes, meal.recipeId)
                _uiState.update { it.copy(snackbarMessage = "Ingredientes adicionados à lista de compras.") }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(errorMessage = "Não foi possível adicionar os ingredientes às compras.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null, errorMessage = null) }
    }
}
