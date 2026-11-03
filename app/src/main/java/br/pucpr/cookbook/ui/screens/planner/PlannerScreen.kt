package br.pucpr.cookbook.ui.screens.planner

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import br.pucpr.cookbook.data.local.entity.PlannedMealEntity
import br.pucpr.cookbook.ui.components.ConfirmDialog
import br.pucpr.cookbook.ui.components.EmptyState
import br.pucpr.cookbook.ui.components.LoadingIndicator
import br.pucpr.cookbook.ui.components.dayTitleFormatter
import br.pucpr.cookbook.ui.components.formatDate
import br.pucpr.cookbook.ui.theme.CookBookTheme
import java.time.LocalDate

@Composable
fun PlannerScreen(
    onBack: () -> Unit,
    onOpenRecipe: (Long) -> Unit,
    viewModel: PlannerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlannerScreenContent(
        uiState = uiState,
        onBack = onBack,
        onOpenRecipe = onOpenRecipe,
        onChangeWeek = viewModel::onChangeWeek,
        onCurrentWeek = viewModel::onCurrentWeek,
        onOpenForm = viewModel::onOpenForm,
        onEditMeal = viewModel::onEditMeal,
        onRequestDelete = viewModel::onRequestDelete,
        onDismissDelete = viewModel::onDismissDelete,
        onConfirmDelete = viewModel::onConfirmDelete,
        onAddToShopping = viewModel::onAddToShopping,
        onFormChange = viewModel::onFormChange,
        onSaveForm = viewModel::onSaveForm,
        onDismissForm = viewModel::onDismissForm,
        onMessageShown = viewModel::onMessageShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlannerScreenContent(
    uiState: PlannerUiState,
    onBack: () -> Unit,
    onOpenRecipe: (Long) -> Unit,
    onChangeWeek: (Long) -> Unit,
    onCurrentWeek: () -> Unit,
    onOpenForm: () -> Unit,
    onEditMeal: (PlannedMealEntity) -> Unit,
    onRequestDelete: (PlannedMealEntity) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onAddToShopping: (PlannedMealEntity) -> Unit,
    onFormChange: (MealFormState) -> Unit,
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
                title = { Text("Planejamento semanal") },
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
                text = { Text("Nova refeição") },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onChangeWeek(-1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Semana anterior")
                }
                Text(
                    "${formatDate(uiState.inicioSemana)} a ${formatDate(uiState.inicioSemana + 6)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                )
                IconButton(onClick = { onChangeWeek(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próxima semana")
                }
            }
            TextButton(onClick = onCurrentWeek, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Esta semana") }
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.meals.isEmpty() -> EmptyState(
                    icon = Icons.Filled.CalendarMonth,
                    title = "Nenhuma refeição planejada",
                    description = "Toque em Nova refeição para montar o cardápio desta semana.",
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    uiState.meals.groupBy { it.meal.dia }.forEach { (dia, meals) ->
                        item { Text(LocalDate.ofEpochDay(dia).format(dayTitleFormatter), style = MaterialTheme.typography.titleMedium) }
                        items(meals, key = { it.meal.id }) { planned ->
                            val meal = planned.meal
                            Card(onClick = { onOpenRecipe(meal.recipeId) }, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(meal.tipo.label, style = MaterialTheme.typography.labelLarge)
                                    Text(planned.recipe.nome, style = MaterialTheme.typography.titleMedium)
                                    Text("${meal.porcoes} porções", style = MaterialTheme.typography.bodyMedium)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TextButton(
                                            onClick = { onAddToShopping(meal) },
                                            enabled = !uiState.isSaving,
                                            modifier = Modifier.weight(1f),
                                        ) { Text("Enviar para compras") }
                                        IconButton(onClick = { onEditMeal(meal) }, enabled = !uiState.isSaving) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Editar refeição ${planned.recipe.nome}")
                                        }
                                        IconButton(onClick = { onRequestDelete(meal) }, enabled = !uiState.isSaving) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Excluir refeição ${planned.recipe.nome}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        uiState.formState?.let { form ->
            MealFormSheet(form, uiState.recipes, uiState.isSaving, onFormChange, onSaveForm, onDismissForm)
        }
        if (uiState.pendingDelete != null) {
            ConfirmDialog(
                title = "Excluir refeição",
                message = "Remover esta refeição do planejamento? A receita continuará disponível.",
                confirmLabel = "Excluir",
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDelete,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlannerScreenPreview() {
    CookBookTheme {
        PlannerScreenContent(
            uiState = PlannerUiState(isLoading = false),
            onBack = {}, onOpenRecipe = {}, onChangeWeek = {}, onCurrentWeek = {},
            onOpenForm = {}, onEditMeal = {}, onRequestDelete = {}, onDismissDelete = {},
            onConfirmDelete = {}, onAddToShopping = {}, onFormChange = {},
            onSaveForm = {}, onDismissForm = {}, onMessageShown = {},
        )
    }
}
