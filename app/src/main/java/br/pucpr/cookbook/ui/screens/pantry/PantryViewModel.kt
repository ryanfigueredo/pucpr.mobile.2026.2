package br.pucpr.cookbook.ui.screens.pantry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.cookbook.data.local.entity.PantryItemEntity
import br.pucpr.cookbook.data.repository.PantryRepository
import br.pucpr.cookbook.data.repository.ShoppingRepository
import br.pucpr.cookbook.ui.components.formatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val pantryRepository: PantryRepository,
    private val shoppingRepository: ShoppingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PantryUiState())
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

    init {
        pantryRepository.observeAll()
            .onEach { items -> _uiState.update { it.copy(isLoading = false, items = items) } }
            .catch { _uiState.update { it.copy(isLoading = false, errorMessage = "Não foi possível carregar a despensa.") } }
            .launchIn(viewModelScope)

        // A lista de compras passa apenas o id; o formulário continua editável antes de salvar.
        val shoppingItemId = savedStateHandle.get<Long>("shoppingItemId") ?: 0L
        if (shoppingItemId > 0L) {
            viewModelScope.launch {
                try {
                    val item = shoppingRepository.observeAll().first().firstOrNull { it.id == shoppingItemId && it.comprado }
                    if (item != null) {
                        _uiState.update {
                            it.copy(formState = PantryFormState(nome = item.nome, quantidade = item.quantidade.toString(), unidade = item.unidade))
                        }
                    } else {
                        _uiState.update { it.copy(errorMessage = "O item comprado não está mais disponível.") }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    _uiState.update { it.copy(errorMessage = "Não foi possível carregar o item comprado.") }
                }
            }
        }
    }

    fun onSearchChange(termo: String) {
        _uiState.update { it.copy(termoBusca = termo) }
    }

    fun onFilterChange(somenteProximos: Boolean) {
        _uiState.update { it.copy(somenteValidadeProxima = somenteProximos) }
    }

    fun onOpenForm() {
        if (!_uiState.value.isSaving) _uiState.update { it.copy(formState = PantryFormState()) }
    }

    fun onEditItem(item: PantryItemEntity) {
        if (_uiState.value.isSaving) return
        _uiState.update {
            it.copy(formState = PantryFormState(item.id, item.nome, item.quantidade.toString(), item.unidade, item.validade?.let(::formatDate).orEmpty()))
        }
    }

    fun onFormChange(form: PantryFormState) {
        if (!_uiState.value.isSaving) _uiState.update { it.copy(formState = form.copy(errorMessage = null)) }
    }

    fun onDismissForm() {
        if (!_uiState.value.isSaving) _uiState.update { it.copy(formState = null) }
    }

    fun onSaveForm() {
        val form = _uiState.value.formState ?: return
        if (_uiState.value.isSaving) return
        val erro = form.validate()
        if (erro != null) {
            _uiState.update { it.copy(formState = form.copy(errorMessage = erro)) }
            return
        }
        val item = form.toEntity()
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                if (item.id == 0L) pantryRepository.createItem(item) else pantryRepository.updateItem(item)
                _uiState.update {
                    it.copy(formState = null, termoBusca = "", somenteValidadeProxima = false, snackbarMessage = "Alimento salvo na despensa.")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(formState = form.copy(errorMessage = "Não foi possível salvar o alimento.")) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onRequestDelete(item: PantryItemEntity) {
        if (!_uiState.value.isSaving) _uiState.update { it.copy(pendingDelete = item) }
    }

    fun onDismissDelete() {
        if (!_uiState.value.isSaving) _uiState.update { it.copy(pendingDelete = null) }
    }

    fun onConfirmDelete() {
        val item = _uiState.value.pendingDelete ?: return
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                pantryRepository.deleteItem(item)
                _uiState.update { it.copy(pendingDelete = null, snackbarMessage = "Alimento removido da despensa.") }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(errorMessage = "Não foi possível excluir o alimento.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null, errorMessage = null) }
    }
}
