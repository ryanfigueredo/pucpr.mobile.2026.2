package br.pucpr.cookbook.ui.screens.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.cookbook.ui.components.ConfirmDialog
import br.pucpr.cookbook.ui.components.EmptyState
import br.pucpr.cookbook.ui.components.LoadingIndicator
import br.pucpr.cookbook.ui.components.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(onBack: () -> Unit, vm: JournalViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(state.message) { state.message?.let { snack.showSnackbar(it); vm.clearMessage() } }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Diário culinário") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
        }) },
        snackbarHost = { SnackbarHost(snack) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = vm::openNew, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Novo registro") }) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected = state.recipeFilter == null, onClick = { vm.filter(null) }, label = { Text("Todos") }) }
                items(state.recipes, key = { it.id }) { recipe ->
                    FilterChip(selected = state.recipeFilter == recipe.id, onClick = { vm.filter(recipe.id) }, label = { Text(recipe.nome) })
                }
            }
            when {
                state.loading -> LoadingIndicator()
                state.visibleEntries.isEmpty() -> EmptyState(Icons.AutoMirrored.Filled.MenuBook, "Nenhum preparo registrado", "Registre como ficaram suas receitas.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.visibleEntries, key = { it.id }) { entry ->
                        val nome = state.recipes.firstOrNull { it.id == entry.recipeId }?.nome ?: "Receita excluída"
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nome, style = MaterialTheme.typography.titleMedium)
                                    Text("${formatDate(entry.dia)} · ${entry.porcoes} porções · Nota ${entry.nota}/5")
                                    if (entry.observacoes.isNotBlank()) Text(entry.observacoes)
                                }
                                IconButton(onClick = { vm.openEdit(entry) }, enabled = !state.saving) { Icon(Icons.Filled.Edit, "Editar registro de $nome") }
                                IconButton(onClick = { vm.requestDelete(entry) }, enabled = !state.saving) { Icon(Icons.Filled.Delete, "Excluir registro de $nome") }
                            }
                        }
                    }
                }
            }
        }
        state.form?.let { form ->
            ModalBottomSheet(onDismissRequest = vm::dismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                Column(
                    modifier = Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(if (form.id == 0L) "Novo preparo" else "Editar preparo", style = MaterialTheme.typography.titleLarge)
                    Text("Receita", style = MaterialTheme.typography.titleMedium)
                    if (state.recipes.isEmpty()) Text("Cadastre uma receita em Explorar antes de registrar um preparo.")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.recipes, key = { it.id }) { recipe ->
                            FilterChip(selected = form.recipeId == recipe.id, onClick = { vm.change(form.copy(recipeId = recipe.id, porcoes = recipe.porcoes.toString())) }, label = { Text(recipe.nome) }, enabled = !state.saving)
                        }
                    }
                    OutlinedTextField(form.data, { vm.change(form.copy(data = it)) }, label = { Text("Data (dd/mm/aaaa)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), enabled = !state.saving, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(form.porcoes, { vm.change(form.copy(porcoes = it)) }, label = { Text("Porções") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), enabled = !state.saving, modifier = Modifier.fillMaxWidth())
                    Text("Nota", style = MaterialTheme.typography.titleMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items((1..5).toList()) { nota -> FilterChip(selected = form.nota == nota, onClick = { vm.change(form.copy(nota = nota)) }, label = { Text("$nota") }, enabled = !state.saving) }
                    }
                    OutlinedTextField(form.observacoes, { vm.change(form.copy(observacoes = it)) }, label = { Text("Observações (opcional)") }, minLines = 3, enabled = !state.saving, modifier = Modifier.fillMaxWidth())
                    form.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = vm::dismiss, enabled = !state.saving, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                        Button(onClick = vm::save, enabled = !state.saving && state.recipes.isNotEmpty(), modifier = Modifier.weight(1f)) { Text("Salvar") }
                    }
                }
            }
        }
        state.pendingDelete?.let {
            ConfirmDialog("Excluir preparo", "Excluir este registro do diário? A receita será preservada.", vm::delete, vm::dismissDelete, confirmLabel = "Excluir")
        }
    }
}
