package br.pucpr.cookbook.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.repository.RecipeRepository
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
class FavoritesViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        recipeRepository.observeFavorites()
            .onEach { favoritos -> _uiState.update { it.copy(isLoading = false, favoritos = favoritos) } }
            .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
            .launchIn(viewModelScope)
    }

    fun onRemoveFavorite(recipe: RecipeEntity) {
        viewModelScope.launch {
            recipeRepository.setFavorite(recipe.id, false)
            _uiState.update {
                it.copy(lastRemovedId = recipe.id, snackbarMessage = "\"${recipe.nome}\" removida dos favoritos")
            }
        }
    }

    fun onUndoRemove() {
        val id = _uiState.value.lastRemovedId ?: return
        viewModelScope.launch {
            recipeRepository.setFavorite(id, true)
            _uiState.update { it.copy(lastRemovedId = null) }
        }
    }

    fun onSnackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
