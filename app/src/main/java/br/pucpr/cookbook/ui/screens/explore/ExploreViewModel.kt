package br.pucpr.cookbook.ui.screens.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.cookbook.data.local.entity.RecipeCategory
import br.pucpr.cookbook.data.local.entity.RecipeDifficulty
import br.pucpr.cookbook.data.repository.RecipeRepository
import br.pucpr.cookbook.ui.components.RecipeFormState
import br.pucpr.cookbook.ui.components.toNewIngredients
import br.pucpr.cookbook.ui.components.toRecipeEntity
import br.pucpr.cookbook.ui.components.validate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class Filters(
    val termo: String = "",
    val categoria: RecipeCategory? = null,
    val dificuldade: RecipeDifficulty? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Argumentos de navegação opcionais lidos aqui: "termo" chega da busca da tela
    // Início e "categoria" chega do LazyRow de categorias da mesma tela.
    private val termoInicial = savedStateHandle.get<String>("termo").orEmpty()
    private val categoriaInicial = savedStateHandle.get<String>("categoria")
        ?.let { raw -> RecipeCategory.entries.firstOrNull { it.name == raw } }

    private val filters = MutableStateFlow(Filters(termo = termoInicial, categoria = categoriaInicial))

    private val _uiState = MutableStateFlow(
        ExploreUiState(termoBusca = termoInicial, categoriaSelecionada = categoriaInicial),
    )
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        filters.flatMapLatest { f -> recipeRepository.observeRecipes(f.termo, f.categoria, f.dificuldade) }
            .onEach { recipes -> _uiState.update { it.copy(isLoading = false, recipes = recipes) } }
            .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
            .launchIn(viewModelScope)
    }

    fun onSearchChange(termo: String) {
        _uiState.update { it.copy(termoBusca = termo) }
        filters.update { it.copy(termo = termo) }
    }

    fun onCategoryToggled(categoria: RecipeCategory) {
        val novo = if (_uiState.value.categoriaSelecionada == categoria) null else categoria
        _uiState.update { it.copy(categoriaSelecionada = novo) }
        filters.update { it.copy(categoria = novo) }
    }

    fun onDifficultyToggled(dificuldade: RecipeDifficulty) {
        val novo = if (_uiState.value.dificuldadeSelecionada == dificuldade) null else dificuldade
        _uiState.update { it.copy(dificuldadeSelecionada = novo) }
        filters.update { it.copy(dificuldade = novo) }
    }

    fun onOpenForm() {
        _uiState.update { it.copy(formState = RecipeFormState()) }
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
        viewModelScope.launch {
            recipeRepository.createRecipe(
                recipe = form.toRecipeEntity(criadaEm = System.currentTimeMillis()),
                ingredients = form.toNewIngredients(),
            )
            _uiState.update { it.copy(formState = null) }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
