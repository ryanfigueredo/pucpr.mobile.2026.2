package br.pucpr.cookbook.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.cookbook.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val SEED_DESTAQUE = 42

// @HiltViewModel + construtor @Inject: o Hilt injeta o RecipeRepository quando a
// tela chama hiltViewModel() — o Composable nunca acessa o repositório diretamente.
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        recipeRepository.observeAllRecipes()
            .onEach { recipes ->
                // StateFlow emitido aqui é coletado pela tela via collectAsStateWithLifecycle.
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    // Seed fixa: a seleção de destaque é estável entre reaberturas da tela.
                    destaque = recipes.shuffled(Random(SEED_DESTAQUE)).take(5),
                    recentes = recipes.take(5),
                )
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
            .launchIn(viewModelScope)
    }

    fun onErrorShown() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
