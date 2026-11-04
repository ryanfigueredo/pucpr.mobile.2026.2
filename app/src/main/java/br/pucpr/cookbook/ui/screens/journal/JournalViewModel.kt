package br.pucpr.cookbook.ui.screens.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.cookbook.data.local.entity.JournalEntryEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.repository.JournalRepository
import br.pucpr.cookbook.data.repository.RecipeRepository
import br.pucpr.cookbook.ui.components.formatDate
import br.pucpr.cookbook.ui.components.parseDate
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
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

data class JournalForm(
    val id: Long = 0, val recipeId: Long? = null,
    val data: String = formatDate(LocalDate.now().toEpochDay()),
    val porcoes: String = "1", val nota: Int = 5, val observacoes: String = "", val error: String? = null,
)
data class JournalUiState(
    val loading: Boolean = true, val saving: Boolean = false,
    val entries: List<JournalEntryEntity> = emptyList(), val recipes: List<RecipeEntity> = emptyList(),
    val recipeFilter: Long? = null, val form: JournalForm? = null,
    val pendingDelete: JournalEntryEntity? = null, val message: String? = null,
) {
    val visibleEntries: List<JournalEntryEntity> get() = entries.filter { recipeFilter == null || it.recipeId == recipeFilter }
}

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: JournalRepository,
    recipes: RecipeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()
    init {
        combine(repository.observeAll(), recipes.observeAllRecipes()) { entries, all -> entries to all }
            .onEach { (entries, all) ->
                _uiState.update { state ->
                    val prefill = if (state.loading && state.form == null) {
                        val id = savedStateHandle.get<Long>("recipeId") ?: 0L
                        val recipe = all.firstOrNull { it.id == id }
                        recipe?.let { JournalForm(recipeId = it.id, porcoes = (savedStateHandle.get<Int>("porcoes") ?: 0).takeIf { count -> count > 0 }?.toString() ?: it.porcoes.toString()) }
                    } else null
                    state.copy(loading = false, entries = entries, recipes = all.sortedBy { it.nome }, form = prefill ?: state.form)
                }
            }
            .catch { _uiState.update { it.copy(loading = false, message = "Não foi possível carregar o diário.") } }
            .launchIn(viewModelScope)
    }
    fun filter(id: Long?) = _uiState.update { it.copy(recipeFilter = id) }
    fun openNew() { if (!_uiState.value.saving) _uiState.update { it.copy(form = JournalForm()) } }
    fun openEdit(item: JournalEntryEntity) {
        if (!_uiState.value.saving) _uiState.update {
            it.copy(form = JournalForm(item.id, item.recipeId, formatDate(item.dia), item.porcoes.toString(), item.nota, item.observacoes))
        }
    }
    fun change(form: JournalForm) { if (!_uiState.value.saving) _uiState.update { it.copy(form = form.copy(error = null)) } }
    fun dismiss() { if (!_uiState.value.saving) _uiState.update { it.copy(form = null) } }
    fun requestDelete(item: JournalEntryEntity) { if (!_uiState.value.saving) _uiState.update { it.copy(pendingDelete = item) } }
    fun dismissDelete() { if (!_uiState.value.saving) _uiState.update { it.copy(pendingDelete = null) } }
    fun clearMessage() = _uiState.update { it.copy(message = null) }
    fun save() {
        val s = _uiState.value
        val form = s.form ?: return
        if (s.saving) return
        val error = when {
            form.recipeId == null || s.recipes.none { it.id == form.recipeId } -> "Escolha uma receita disponível."
            parseDate(form.data) == null -> "Informe uma data válida (dd/mm/aaaa)."
            form.porcoes.toIntOrNull()?.takeIf { it > 0 } == null -> "Informe uma quantidade de porções maior que zero."
            form.nota !in 1..5 -> "Escolha uma nota de 1 a 5."
            else -> null
        }
        if (error != null) { _uiState.update { it.copy(form = form.copy(error = error)) }; return }
        val entry = JournalEntryEntity(form.id, form.recipeId, checkNotNull(parseDate(form.data)), form.porcoes.toInt(), form.nota, form.observacoes.trim())
        _uiState.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                repository.save(entry)
                _uiState.update { it.copy(form = null, recipeFilter = null, message = "Preparo registrado.") }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(form = form.copy(error = "Não foi possível salvar o registro.")) }
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
                _uiState.update { it.copy(pendingDelete = null, message = "Registro excluído.") }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(message = "Não foi possível excluir o registro.") }
            } finally { _uiState.update { it.copy(saving = false) } }
        }
    }
}
