package br.pucpr.cookbook.ui.screens.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.cookbook.data.repository.NewIngredient
import br.pucpr.cookbook.data.repository.RecipeRepository
import br.pucpr.cookbook.data.repository.ShoppingRepository
import br.pucpr.cookbook.ui.components.IngredientDraft
import br.pucpr.cookbook.ui.components.RecipeFormState
import br.pucpr.cookbook.ui.components.toFormState
import br.pucpr.cookbook.ui.components.toNewIngredients
import br.pucpr.cookbook.ui.components.toRecipeEntity
import br.pucpr.cookbook.ui.components.validate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val shoppingRepository: ShoppingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Argumento obrigatório de navegação: o id da receita selecionada em Início,
    // Explorar ou Favoritos chega aqui pela rota "details/{recipeId}".
    private val recipeId: Long = checkNotNull(savedStateHandle.get<Long>("recipeId"))

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        recipeRepository.observeRecipeWithIngredients(recipeId)
            .onEach { recipeWithIngredients ->
                if (recipeWithIngredients == null) {
                    _uiState.update { it.copy(isLoading = false, deleted = true) }
                } else {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            recipe = recipeWithIngredients.recipe,
                            ingredients = recipeWithIngredients.ingredients,
                            // Só inicializa o ajuste de porções na primeira carga; edições
                            // seguintes do Room não devem sobrescrever a escolha do usuário.
                            porcoesAjustadas = if (current.porcoesAjustadas == 0) {
                                recipeWithIngredients.recipe.porcoes
                            } else {
                                current.porcoesAjustadas
                            },
                        )
                    }
                }
            }
            .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
            .launchIn(viewModelScope)
    }

    fun onAdjustPortions(novasPorcoes: Int) {
        if (novasPorcoes > 0) _uiState.update { it.copy(porcoesAjustadas = novasPorcoes) }
    }

    fun onToggleFavorite() {
        val recipe = _uiState.value.recipe ?: return
        viewModelScope.launch { recipeRepository.setFavorite(recipe.id, !recipe.favorita) }
    }

    fun onAddToShoppingList() {
        val state = _uiState.value
        val recipe = state.recipe ?: return
        val fator = state.porcoesAjustadas.toDouble() / recipe.porcoes.toDouble()
        viewModelScope.launch {
            val escalados = state.ingredients.map { NewIngredient(it.nome, it.quantidade * fator, it.unidade) }
            shoppingRepository.addIngredientsFromRecipe(escalados, recipe.id)
            _uiState.update { it.copy(snackbarMessage = "Ingredientes adicionados à lista de compras.") }
        }
    }

    fun onOpenEditForm() {
        val state = _uiState.value
        val recipe = state.recipe ?: return
        val draftIngredientes = state.ingredients.map { IngredientDraft(it.nome, it.quantidade.toString(), it.unidade) }
        _uiState.update { it.copy(formState = recipe.toFormState(draftIngredientes)) }
    }

    fun onFormChange(novoFormState: RecipeFormState) {
        _uiState.update { it.copy(formState = novoFormState) }
    }

    fun onDismissForm() {
        _uiState.update { it.copy(formState = null) }
    }

    fun onSaveForm() {
        val form = _uiState.value.formState ?: return
        val erro = form.validate()
        if (erro != null) {
            _uiState.update { it.copy(formState = form.copy(errorMessage = erro)) }
            return
        }
        val original = _uiState.value.recipe ?: return
        viewModelScope.launch {
            recipeRepository.updateRecipe(
                recipe = form.toRecipeEntity(criadaEm = original.criadaEm),
                ingredients = form.toNewIngredients(),
            )
            _uiState.update { it.copy(formState = null) }
        }
    }

    fun onRequestDelete() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onConfirmDelete() {
        val recipe = _uiState.value.recipe ?: return
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipe)
            _uiState.update { it.copy(showDeleteDialog = false, deleted = true) }
        }
    }

    fun onSnackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
