package br.pucpr.cookbook.ui.screens.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.cookbook.data.local.entity.CollectionEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.local.relation.CollectionWithRecipes
import br.pucpr.cookbook.data.repository.CollectionRepository
import br.pucpr.cookbook.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionForm(val id: Long = 0, val nome: String = "", val descricao: String = "", val recipeIds: Set<Long> = emptySet(), val error: String? = null)
data class CollectionsUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val collections: List<CollectionWithRecipes> = emptyList(),
    val recipes: List<RecipeEntity> = emptyList(),
    val form: CollectionForm? = null,
    val pendingDelete: CollectionEntity? = null,
    val message: String? = null,
)

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val repository: CollectionRepository,
    recipes: RecipeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CollectionsUiState())
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()

    init {
        combine(repository.observeAll(), recipes.observeAllRecipes()) { collections, all -> collections to all }
            .onEach { (collections, all) -> _uiState.update { it.copy(loading = false, collections = collections, recipes = all.sortedBy { r -> r.nome }) } }
            .catch { _uiState.update { it.copy(loading = false, message = "Não foi possível carregar as coleções.") } }
            .launchIn(viewModelScope)
    }

    fun openNew() { if (!_uiState.value.saving) _uiState.update { it.copy(form = CollectionForm()) } }
    fun openEdit(item: CollectionWithRecipes) {
        if (!_uiState.value.saving) _uiState.update { it.copy(form = CollectionForm(item.collection.id, item.collection.nome, item.collection.descricao, item.recipes.map { r -> r.id }.toSet())) }
    }
    fun change(form: CollectionForm) { if (!_uiState.value.saving) _uiState.update { it.copy(form = form.copy(error = null)) } }
    fun dismiss() { if (!_uiState.value.saving) _uiState.update { it.copy(form = null) } }
    fun requestDelete(item: CollectionEntity) { if (!_uiState.value.saving) _uiState.update { it.copy(pendingDelete = item) } }
    fun dismissDelete() { if (!_uiState.value.saving) _uiState.update { it.copy(pendingDelete = null) } }
    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun save() {
        val s = _uiState.value
        val form = s.form ?: return
        if (s.saving) return
        val error = when {
            form.nome.isBlank() -> "Informe o nome da coleção."
            form.recipeIds.any { id -> s.recipes.none { it.id == id } } -> "Uma receita selecionada não está mais disponível."
            else -> null
        }
        if (error != null) { _uiState.update { it.copy(form = form.copy(error = error)) }; return }
        _uiState.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                repository.save(CollectionEntity(form.id, form.nome.trim(), form.descricao.trim()), form.recipeIds)
                _uiState.update { it.copy(form = null, message = "Coleção salva.") }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(form = form.copy(error = "Não foi possível salvar a coleção.")) }
            } finally { _uiState.update { it.copy(saving = false) } }
        }
    }

    fun delete() {
        val item = _uiState.value.pendingDelete ?: return
        if (_uiState.value.saving) return
        _uiState.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                repository.delete(item)
                _uiState.update { it.copy(pendingDelete = null, message = "Coleção excluída.") }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(message = "Não foi possível excluir a coleção.") }
            } finally { _uiState.update { it.copy(saving = false) } }
        }
    }
}
