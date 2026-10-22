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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.cookbook.data.local.entity.ShoppingItemEntity
import br.pucpr.cookbook.ui.components.ConfirmDialog
import br.pucpr.cookbook.ui.components.EmptyState
import br.pucpr.cookbook.ui.components.LoadingIndicator
import br.pucpr.cookbook.ui.components.UnidadeDropdownField
import br.pucpr.cookbook.ui.components.formatQuantidadeComUnidade
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
        onSnackbarShown = viewModel::onSnackbarShown,
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
    onSnackbarShown: () -> Unit,
    onErrorShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val itemFocusRequester = remember { FocusRequester() }
    val mensagem = uiState.snackbarMessage ?: uiState.errorMessage

    LaunchedEffect(mensagem) {
        mensagem?.let {
            snackbarHostState.showSnackbar(it)
            if (uiState.snackbarMessage != null) itemFocusRequester.requestFocus()
            onSnackbarShown()
            onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de compras") },
                actions = {
                    TooltipIconButton(
                        text = "Limpar comprados",
                        icon = Icons.Filled.Delete,
                        onClick = onRequestClearBought,
                    )
                    TooltipIconButton(
                        text = "Limpar lista",
                        icon = Icons.Filled.DeleteSweep,
                        onClick = onRequestClearAll,
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) } },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AdicionarItemForm(
                uiState = uiState,
                itemFocusRequester = itemFocusRequester,
                onNovoItemNomeChange = onNovoItemNomeChange,
                onNovoItemQuantidadeChange = onNovoItemQuantidadeChange,
                onNovoItemUnidadeChange = onNovoItemUnidadeChange,
                onAddManualItem = onAddManualItem,
            )

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
private fun AdicionarItemForm(
    uiState: ShoppingUiState,
    itemFocusRequester: FocusRequester,
    onNovoItemNomeChange: (String) -> Unit,
    onNovoItemQuantidadeChange: (String) -> Unit,
    onNovoItemUnidadeChange: (String) -> Unit,
    onAddManualItem: () -> Unit,
) {
    val itemInvalido = uiState.tentouAdicionar && uiState.novoItemNome.isBlank()
    val quantidadeInvalida = uiState.tentouAdicionar && uiState.novoItemQuantidadeValida == null

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Adicionar item", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = uiState.novoItemNome,
                onValueChange = onNovoItemNomeChange,
                label = { Text("Item") },
                singleLine = true,
                isError = itemInvalido,
                supportingText = if (itemInvalido) {
                    { Text("Informe o nome do item") }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(itemFocusRequester),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = uiState.novoItemQuantidade,
                    onValueChange = onNovoItemQuantidadeChange,
                    label = { Text("Qtd.") },
                    singleLine = true,
                    isError = quantidadeInvalida,
                    supportingText = if (quantidadeInvalida) {
                        { Text("Inválida") }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                UnidadeDropdownField(
                    value = uiState.novoItemUnidade,
                    onValueChange = onNovoItemUnidadeChange,
                    modifier = Modifier.weight(1.2f),
                )
                FilledIconButton(
                    onClick = onAddManualItem,
                    enabled = uiState.podeAdicionarItem,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Adicionar item")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TooltipIconButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(text) } },
        state = rememberTooltipState(),
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = text)
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItemEntity,
    onToggleComprado: (ShoppingItemEntity) -> Unit,
    onDeleteItem: (ShoppingItemEntity) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.comprado) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.comprado) 0.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = item.comprado, onCheckedChange = { onToggleComprado(item) })
            Text(
                "${item.nome} — ${formatQuantidadeComUnidade(item.quantidade, item.unidade)}",
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
            onSnackbarShown = {},
            onErrorShown = {},
        )
    }
}
