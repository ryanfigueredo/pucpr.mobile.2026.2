package br.pucpr.cookbook.ui.screens.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.cookbook.data.local.entity.ShoppingItemEntity
import br.pucpr.cookbook.ui.components.ConfirmDialog
import br.pucpr.cookbook.ui.components.EmptyState
import br.pucpr.cookbook.ui.components.LoadingIndicator
import br.pucpr.cookbook.ui.theme.CookBookTheme

@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ShoppingScreenContent(
        uiState = uiState,
        onToggleComprado = viewModel::onToggleComprado,
        onDeleteItem = viewModel::onDeleteItem,
        onNovoItemNomeChange = viewModel::onNovoItemNomeChange,
        onNovoItemQuantidadeChange = viewModel::onNovoItemQuantidadeChange,
        onNovoItemUnidadeChange = viewModel::onNovoItemUnidadeChange,
        onAddManualItem = viewModel::onAddManualItem,
        onRequestClearBought = viewModel::onRequestClearBought,
        onDismissClearBoughtDialog = viewModel::onDismissClearBoughtDialog,
        onConfirmClearBought = viewModel::onConfirmClearBought,
        onRequestClearAll = viewModel::onRequestClearAll,
        onDismissClearAllDialog = viewModel::onDismissClearAllDialog,
        onConfirmClearAll = viewModel::onConfirmClearAll,
        onErrorShown = viewModel::onErrorShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingScreenContent(
    uiState: ShoppingUiState,
    onToggleComprado: (ShoppingItemEntity) -> Unit,
    onDeleteItem: (ShoppingItemEntity) -> Unit,
    onNovoItemNomeChange: (String) -> Unit,
    onNovoItemQuantidadeChange: (String) -> Unit,
    onNovoItemUnidadeChange: (String) -> Unit,
    onAddManualItem: () -> Unit,
    onRequestClearBought: () -> Unit,
    onDismissClearBoughtDialog: () -> Unit,
    onConfirmClearBought: () -> Unit,
    onRequestClearAll: () -> Unit,
    onDismissClearAllDialog: () -> Unit,
    onConfirmClearAll: () -> Unit,
    onErrorShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de compras") },
                actions = {
                    IconButton(onClick = onRequestClearBought) {
                        Icon(Icons.Filled.Delete, contentDescription = "Limpar comprados")
                    }
                    IconButton(onClick = onRequestClearAll) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Limpar lista")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) } },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = uiState.novoItemNome,
                    onValueChange = onNovoItemNomeChange,
                    label = { Text("Item") },
                    modifier = Modifier.weight(2f),
                )
                OutlinedTextField(
                    value = uiState.novoItemQuantidade,
                    onValueChange = onNovoItemQuantidadeChange,
                    label = { Text("Qtd.") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = uiState.novoItemUnidade,
                    onValueChange = onNovoItemUnidadeChange,
                    label = { Text("Unid.") },
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onAddManualItem) {
                    Icon(Icons.Filled.Add, contentDescription = "Adicionar item")
                }
            }

            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.isEmpty -> EmptyState(
                    icon = Icons.Filled.ShoppingCart,
                    title = "Lista de compras vazia",
                    description = "Adicione um item manualmente ou envie ingredientes a partir de uma receita.",
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    uiState.grupos.forEach { grupo ->
                        item {
                            Text(
                                grupo.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                            )
                        }
                        items(grupo.itens, key = { it.id }) { item ->
                            ShoppingItemRow(item = item, onToggleComprado = onToggleComprado, onDeleteItem = onDeleteItem)
                        }
                    }
                }
            }
        }

        if (uiState.showClearBoughtDialog) {
            ConfirmDialog(
                title = "Limpar comprados",
                message = "Remover todos os itens já marcados como comprados?",
                confirmLabel = "Limpar",
                onConfirm = onConfirmClearBought,
                onDismiss = onDismissClearBoughtDialog,
            )
        }
        if (uiState.showClearAllDialog) {
            ConfirmDialog(
                title = "Limpar lista",
                message = "Remover todos os itens da lista de compras?",
                confirmLabel = "Limpar",
                onConfirm = onConfirmClearAll,
                onDismiss = onDismissClearAllDialog,
            )
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItemEntity,
    onToggleComprado: (ShoppingItemEntity) -> Unit,
    onDeleteItem: (ShoppingItemEntity) -> Unit,
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (item.comprado) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = if (item.comprado) 0.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = item.comprado, onCheckedChange = { onToggleComprado(item) })
            Text(
                "${item.nome} — ${item.quantidade} ${item.unidade}",
                modifier = Modifier.weight(1f),
                style = if (item.comprado) {
                    MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough)
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = if (item.comprado) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            IconButton(onClick = { onDeleteItem(item) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Excluir item")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShoppingScreenPreview() {
    CookBookTheme {
        ShoppingScreenContent(
            uiState = ShoppingUiState(isLoading = false),
            onToggleComprado = {},
            onDeleteItem = {},
            onNovoItemNomeChange = {},
            onNovoItemQuantidadeChange = {},
            onNovoItemUnidadeChange = {},
            onAddManualItem = {},
            onRequestClearBought = {},
            onDismissClearBoughtDialog = {},
            onConfirmClearBought = {},
            onRequestClearAll = {},
            onDismissClearAllDialog = {},
            onConfirmClearAll = {},
            onErrorShown = {},
        )
    }
}
