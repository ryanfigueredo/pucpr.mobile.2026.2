package br.pucpr.cookbook.ui.screens.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.cookbook.data.local.entity.ShoppingItemEntity
import br.pucpr.cookbook.data.repository.RecipeRepository
import br.pucpr.cookbook.data.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val shoppingRepository: ShoppingRepository,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingUiState())
    val uiState: StateFlow<ShoppingUiState> = _uiState.asStateFlow()

    init {
        // Combina os itens da lista com os nomes das receitas apenas para exibir o
        // agrupamento "por receita de origem" — a gravação em si fica no ShoppingRepository.
        combine(shoppingRepository.observeAll(), recipeRepository.observeAllRecipes()) { itens, receitas ->
            val nomesPorId = receitas.associate { it.id to it.nome }
            agrupar(itens, nomesPorId)
        }
            .onEach { grupos -> _uiState.update { it.copy(isLoading = false, grupos = grupos) } }
            .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
            .launchIn(viewModelScope)
    }

    private fun agrupar(itens: List<ShoppingItemEntity>, nomesPorId: Map<Long, String>): List<ShoppingGroup> {
        val pendentes = itens.filter { !it.comprado }
        val comprados = itens.filter { it.comprado }

        val gruposPendentes = pendentes
            .groupBy { item -> item.recipeId?.let { nomesPorId[it] } ?: "Adicionados manualmente" }
            .map { (titulo, lista) -> ShoppingGroup(titulo, lista.sortedBy { it.nome }) }
            .sortedBy { it.titulo }

        val grupoComprados = if (comprados.isNotEmpty()) {
            listOf(ShoppingGroup("Comprados", comprados.sortedBy { it.nome }))
        } else {
            emptyList()
        }

        return gruposPendentes + grupoComprados
    }

    fun onToggleComprado(item: ShoppingItemEntity) {
        viewModelScope.launch { shoppingRepository.setComprado(item, !item.comprado) }
    }

    fun onDeleteItem(item: ShoppingItemEntity) {
        viewModelScope.launch { shoppingRepository.deleteItem(item) }
    }

    fun onNovoItemNomeChange(valor: String) {
        _uiState.update { it.copy(novoItemNome = valor) }
    }

    fun onNovoItemQuantidadeChange(valor: String) {
        _uiState.update { it.copy(novoItemQuantidade = valor.filter { c -> c.isDigit() || c == ',' || c == '.' }) }
    }

    fun onNovoItemUnidadeChange(valor: String) {
        _uiState.update { it.copy(novoItemUnidade = valor) }
    }

    fun onAddManualItem() {
        // Marca a tentativa para a tela passar a exibir isError/supportingText nos campos inválidos.
        _uiState.update { it.copy(tentouAdicionar = true) }
        val state = _uiState.value
        val quantidade = state.novoItemQuantidadeValida
        if (!state.podeAdicionarItem || quantidade == null) return
        viewModelScope.launch {
            shoppingRepository.addItem(state.novoItemNome.trim(), quantidade, state.novoItemUnidade)
            // Unidade é mantida (última escolhida); nome e quantidade são limpos para o próximo item.
            _uiState.update {
                it.copy(
                    novoItemNome = "",
                    novoItemQuantidade = "",
                    tentouAdicionar = false,
                    snackbarMessage = "Item adicionado à lista.",
                )
            }
        }
    }

    fun onSnackbarShown() = _uiState.update { it.copy(snackbarMessage = null) }

    fun onRequestClearBought() = _uiState.update { it.copy(showClearBoughtDialog = true) }

    fun onDismissClearBoughtDialog() = _uiState.update { it.copy(showClearBoughtDialog = false) }

    fun onConfirmClearBought() {
        viewModelScope.launch {
            shoppingRepository.clearComprados()
            _uiState.update { it.copy(showClearBoughtDialog = false) }
        }
    }

    fun onRequestClearAll() = _uiState.update { it.copy(showClearAllDialog = true) }

    fun onDismissClearAllDialog() = _uiState.update { it.copy(showClearAllDialog = false) }

    fun onConfirmClearAll() {
        viewModelScope.launch {
            shoppingRepository.clearAll()
            _uiState.update { it.copy(showClearAllDialog = false) }
        }
    }

    fun onErrorShown() = _uiState.update { it.copy(errorMessage = null) }
}
