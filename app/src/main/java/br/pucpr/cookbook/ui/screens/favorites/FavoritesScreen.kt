package br.pucpr.cookbook.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.ui.components.EmptyState
import br.pucpr.cookbook.ui.components.LoadingIndicator
import br.pucpr.cookbook.ui.components.RecipeCard
import br.pucpr.cookbook.ui.theme.CookBookTheme

@Composable
fun FavoritesScreen(
    onOpenRecipe: (Long) -> Unit,
    onGoExplore: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreenContent(
        uiState = uiState,
        onOpenRecipe = onOpenRecipe,
        onGoExplore = onGoExplore,
        onRemoveFavorite = viewModel::onRemoveFavorite,
        onUndoRemove = viewModel::onUndoRemove,
        onSnackbarShown = viewModel::onSnackbarShown,
        onErrorShown = viewModel::onErrorShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesScreenContent(
    uiState: FavoritesUiState,
    onOpenRecipe: (Long) -> Unit,
    onGoExplore: () -> Unit,
    onRemoveFavorite: (RecipeEntity) -> Unit,
    onUndoRemove: () -> Unit,
    onSnackbarShown: () -> Unit,
    onErrorShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val mensagem = uiState.snackbarMessage ?: uiState.errorMessage

    LaunchedEffect(mensagem) {
        mensagem?.let {
            val podeDesfazer = uiState.snackbarMessage != null && uiState.lastRemovedId != null
            val resultado = snackbarHostState.showSnackbar(
                message = it,
                actionLabel = if (podeDesfazer) "Desfazer" else null,
            )
            if (resultado == SnackbarResult.ActionPerformed) onUndoRemove()
            onSnackbarShown()
            onErrorShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Favoritos") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) } },
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
            uiState.favoritos.isEmpty() -> EmptyState(
                icon = Icons.Filled.FavoriteBorder,
                title = "Nenhuma receita favorita ainda",
                description = "Favorite receitas em Explorar para encontrá-las rapidamente aqui.",
                actionLabel = "Ir para Explorar",
                onAction = onGoExplore,
                modifier = Modifier.padding(innerPadding),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.favoritos, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onOpenRecipe(recipe.id) },
                        onToggleFavorite = { onRemoveFavorite(recipe) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    CookBookTheme {
        FavoritesScreenContent(
            uiState = FavoritesUiState(isLoading = false),
            onOpenRecipe = {},
            onGoExplore = {},
            onRemoveFavorite = {},
            onUndoRemove = {},
            onSnackbarShown = {},
            onErrorShown = {},
        )
    }
}
