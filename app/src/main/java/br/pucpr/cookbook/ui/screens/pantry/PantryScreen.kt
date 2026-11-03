package br.pucpr.cookbook.ui.screens.pantry

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.cookbook.data.local.entity.PantryItemEntity
import br.pucpr.cookbook.ui.components.ConfirmDialog
import br.pucpr.cookbook.ui.components.EmptyState
import br.pucpr.cookbook.ui.components.LoadingIndicator
import br.pucpr.cookbook.ui.components.formatDate
import br.pucpr.cookbook.ui.components.formatQuantidadeComUnidade
import br.pucpr.cookbook.ui.theme.CookBookTheme
import java.time.LocalDate

@Composable
fun PantryScreen(onBack: () -> Unit, viewModel: PantryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PantryScreenContent(
        uiState = uiState,
        onBack = onBack,
        onSearchChange = viewModel::onSearchChange,
        onFilterChange = viewModel::onFilterChange,
        onOpenForm = viewModel::onOpenForm,
        onEditItem = viewModel::onEditItem,
        onRequestDelete = viewModel::onRequestDelete,
        onDismissDelete = viewModel::onDismissDelete,
        onConfirmDelete = viewModel::onConfirmDelete,
        onFormChange = viewModel::onFormChange,
        onSaveForm = viewModel::onSaveForm,
        onDismissForm = viewModel::onDismissForm,
        onMessageShown = viewModel::onMessageShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PantryScreenContent(
    uiState: PantryUiState,
    onBack: () -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (Boolean) -> Unit,
    onOpenForm: () -> Unit,
    onEditItem: (PantryItemEntity) -> Unit,
    onRequestDelete: (PantryItemEntity) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onFormChange: (PantryFormState) -> Unit,
    onSaveForm: () -> Unit,
    onDismissForm: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val mensagem = uiState.errorMessage ?: uiState.snackbarMessage
    LaunchedEffect(mensagem) {
        mensagem?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minha despensa") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (!uiState.isSaving) onOpenForm() },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Novo alimento") },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = uiState.termoBusca,
                onValueChange = onSearchChange,
                label = { Text("Buscar alimentos") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !uiState.somenteValidadeProxima, onClick = { onFilterChange(false) }, label = { Text("Todos") })
                FilterChip(selected = uiState.somenteValidadeProxima, onClick = { onFilterChange(true) }, label = { Text("Validade próxima") })
            }
            if (uiState.somenteValidadeProxima) {
                Text(
                    "Vencidos ou que vencem nos próximos 7 dias.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            val items = uiState.visibleItems
            when {
                uiState.isLoading -> LoadingIndicator()
                items.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Kitchen,
                    title = if (uiState.items.isEmpty()) "Sua despensa está vazia" else "Nenhum alimento encontrado",
                    description = if (uiState.items.isEmpty()) "Cadastre os alimentos que você tem em casa." else "Altere a busca ou o filtro de validade.",
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        PantryItemCard(item, uiState.isSaving, onEditItem, onRequestDelete)
                    }
                }
            }
        }
        uiState.formState?.let { form ->
            PantryFormSheet(form, uiState.isSaving, onFormChange, onSaveForm, onDismissForm)
        }
        uiState.pendingDelete?.let { item ->
            ConfirmDialog(
                title = "Excluir alimento",
                message = "Remover ${item.nome} da despensa?",
                confirmLabel = "Excluir",
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDelete,
            )
        }
    }
}

@Composable
private fun PantryItemCard(
    item: PantryItemEntity,
    isSaving: Boolean,
    onEditItem: (PantryItemEntity) -> Unit,
    onRequestDelete: (PantryItemEntity) -> Unit,
) {
    val hoje = LocalDate.now().toEpochDay()
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.nome, style = MaterialTheme.typography.titleMedium)
                Text(formatQuantidadeComUnidade(item.quantidade, item.unidade))
                val validade = item.validade
                Text(
                    text = when {
                        validade == null -> "Sem validade informada"
                        validade < hoje -> "Vencido em ${formatDate(validade)}"
                        validade == hoje -> "Vence hoje"
                        else -> "Validade: ${formatDate(validade)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (validade != null && validade <= hoje + 7) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onEditItem(item) }, enabled = !isSaving) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar alimento ${item.nome}")
            }
            IconButton(onClick = { onRequestDelete(item) }, enabled = !isSaving) {
                Icon(Icons.Filled.Delete, contentDescription = "Excluir alimento ${item.nome}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PantryScreenPreview() {
    CookBookTheme {
        PantryScreenContent(
            uiState = PantryUiState(isLoading = false, items = listOf(PantryItemEntity(1, "Arroz", 2.0, "kg"))),
            onBack = {}, onSearchChange = {}, onFilterChange = {}, onOpenForm = {},
            onEditItem = {}, onRequestDelete = {}, onDismissDelete = {}, onConfirmDelete = {},
            onFormChange = {}, onSaveForm = {}, onDismissForm = {}, onMessageShown = {},
        )
    }
}
