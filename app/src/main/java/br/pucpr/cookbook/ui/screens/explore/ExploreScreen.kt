package br.pucpr.cookbook.ui.screens.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.cookbook.data.local.entity.RecipeCategory
import br.pucpr.cookbook.data.local.entity.RecipeDifficulty
import br.pucpr.cookbook.ui.components.CategoryFilterChip
import br.pucpr.cookbook.ui.components.EmptyState
import br.pucpr.cookbook.ui.components.LoadingIndicator
import br.pucpr.cookbook.ui.components.RecipeCard
import br.pucpr.cookbook.ui.components.RecipeFormSheet

@Composable
fun ExploreScreen(
    onOpenRecipe: (Long) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExploreScreenContent(
        uiState = uiState,
        onOpenRecipe = onOpenRecipe,
        onSearchChange = viewModel::onSearchChange,
        onCategoryToggled = viewModel::onCategoryToggled,
        onDifficultyToggled = viewModel::onDifficultyToggled,
        onOpenForm = viewModel::onOpenForm,
        onFormChange = viewModel::onFormChange,
        onDismissForm = viewModel::onDismissForm,
        onSaveForm = viewModel::onSaveForm,
        onErrorShown = viewModel::onErrorShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreScreenContent(
    uiState: ExploreUiState,
    onOpenRecipe: (Long) -> Unit,
    onSearchChange: (String) -> Unit,
    onCategoryToggled: (RecipeCategory) -> Unit,
    onDifficultyToggled: (RecipeDifficulty) -> Unit,
    onOpenForm: () -> Unit,
    onFormChange: (br.pucpr.cookbook.ui.components.RecipeFormState) -> Unit,
    onDismissForm: () -> Unit,
    onSaveForm: () -> Unit,
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
        topBar = { TopAppBar(title = { Text("Explorar receitas") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenForm) {
                Icon(Icons.Filled.Add, contentDescription = "Nova receita")
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) }
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                OutlinedTextField(
                    value = uiState.termoBusca,
                    onValueChange = onSearchChange,
                    label = { Text("Buscar por nome") },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(RecipeCategory.entries) { categoria ->
                        CategoryFilterChip(
                            categoria = categoria,
                            selected = uiState.categoriaSelecionada == categoria,
                            onClick = { onCategoryToggled(categoria) },
                        )
                    }
                    items(RecipeDifficulty.entries) { dificuldade ->
                        FilterChip(
                            selected = uiState.dificuldadeSelecionada == dificuldade,
                            onClick = { onDifficultyToggled(dificuldade) },
                            label = { Text(dificuldade.label) },
                        )
                    }
                }
                if (uiState.recipes.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.SearchOff,
                        title = "Nenhuma receita encontrada",
                        description = "Ajuste a busca ou os filtros, ou cadastre uma nova receita.",
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.recipes, key = { it.id }) { recipe ->
                            RecipeCard(recipe = recipe, onClick = { onOpenRecipe(recipe.id) })
                        }
                    }
                }
            }
        }

        uiState.formState?.let { form ->
            RecipeFormSheet(
                formState = form,
                isEditing = false,
                onChange = onFormChange,
                onSave = onSaveForm,
                onDismiss = onDismissForm,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreScreenPreview() {
    br.pucpr.cookbook.ui.theme.CookBookTheme {
        ExploreScreenContent(
            uiState = ExploreUiState(isLoading = false),
            onOpenRecipe = {},
            onSearchChange = {},
            onCategoryToggled = {},
            onDifficultyToggled = {},
            onOpenForm = {},
            onFormChange = {},
            onDismissForm = {},
            onSaveForm = {},
            onErrorShown = {},
        )
    }
}
