package br.pucpr.cookbook.ui.screens.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.cookbook.ui.components.ConfirmDialog
import br.pucpr.cookbook.ui.components.EmptyState
import br.pucpr.cookbook.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(onBack: () -> Unit, onOpenRecipe: (Long) -> Unit, vm: CollectionsViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(state.message) { state.message?.let { snack.showSnackbar(it); vm.clearMessage() } }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Minhas coleções") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
        }) },
        snackbarHost = { SnackbarHost(snack) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = vm::openNew, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Nova coleção") }) },
    ) { padding ->
        when {
            state.loading -> LoadingIndicator(modifier = Modifier.padding(padding))
            state.collections.isEmpty() -> EmptyState(
                Icons.Filled.CollectionsBookmark, "Nenhuma coleção", "Organize suas receitas em coleções.",
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.collections, key = { it.collection.id }) { group ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(group.collection.nome, style = MaterialTheme.typography.titleMedium)
                                    if (group.collection.descricao.isNotBlank()) Text(group.collection.descricao)
                                    Text("${group.recipes.size} receitas", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { vm.openEdit(group) }, enabled = !state.saving) { Icon(Icons.Filled.Edit, "Editar ${group.collection.nome}") }
                                IconButton(onClick = { vm.requestDelete(group.collection) }, enabled = !state.saving) { Icon(Icons.Filled.Delete, "Excluir ${group.collection.nome}") }
                            }
                            group.recipes.sortedBy { it.nome }.forEach { recipe ->
                                TextButton(onClick = { onOpenRecipe(recipe.id) }) { Text(recipe.nome) }
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
                    Text(if (form.id == 0L) "Nova coleção" else "Editar coleção", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(form.nome, { vm.change(form.copy(nome = it)) }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth(), enabled = !state.saving)
                    OutlinedTextField(form.descricao, { vm.change(form.copy(descricao = it)) }, label = { Text("Descrição (opcional)") }, modifier = Modifier.fillMaxWidth(), enabled = !state.saving)
                    Text("Receitas", style = MaterialTheme.typography.titleMedium)
                    if (state.recipes.isEmpty()) Text("Cadastre receitas em Explorar para adicioná-las aqui.")
                    state.recipes.forEach { recipe ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = recipe.id in form.recipeIds,
                                onCheckedChange = { selected -> vm.change(form.copy(recipeIds = if (selected) form.recipeIds + recipe.id else form.recipeIds - recipe.id)) },
                                enabled = !state.saving,
                            )
                            Text(recipe.nome)
                        }
                    }
                    form.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = vm::dismiss, enabled = !state.saving, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                        Button(onClick = vm::save, enabled = !state.saving, modifier = Modifier.weight(1f)) { Text("Salvar") }
                    }
                }
            }
        }
        state.pendingDelete?.let { item ->
            ConfirmDialog("Excluir coleção", "Excluir ${item.nome}? As receitas serão preservadas.", vm::delete, vm::dismissDelete, confirmLabel = "Excluir")
        }
    }
}
