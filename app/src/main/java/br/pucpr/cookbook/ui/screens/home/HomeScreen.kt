package br.pucpr.cookbook.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.cookbook.data.local.entity.RecipeCategory
import br.pucpr.cookbook.data.local.entity.RecipeDifficulty
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.ui.components.CategoryFilterChip
import br.pucpr.cookbook.ui.components.LoadingIndicator
import br.pucpr.cookbook.ui.components.RecipeCard
import br.pucpr.cookbook.ui.theme.CookBookTheme

@Composable
fun HomeScreen(
    onSearch: (String) -> Unit,
    onOpenRecipe: (Long) -> Unit,
    onOpenCategory: (RecipeCategory) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    // A tela só fala com o ViewModel: coleta o StateFlow com collectAsStateWithLifecycle,
    // nunca acessa o Repository ou o Room diretamente.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreenContent(
        uiState = uiState,
        onSearch = onSearch,
        onOpenRecipe = onOpenRecipe,
        onOpenCategory = onOpenCategory,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onSearch: (String) -> Unit,
    onOpenRecipe: (Long) -> Unit,
    onOpenCategory: (RecipeCategory) -> Unit,
) {
    var termoBusca by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.background,
                            ),
                        ),
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp),
            ) {
                Column {
                    Text(
                        "Olá, chef!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        "O que vamos cozinhar hoje?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                OutlinedTextField(
                    value = termoBusca,
                    onValueChange = { termoBusca = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Buscar receitas") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { if (termoBusca.isNotBlank()) onSearch(termoBusca) },
                    ),
                    trailingIcon = {
                        IconButton(onClick = { if (termoBusca.isNotBlank()) onSearch(termoBusca) }) {
                            Icon(Icons.Filled.Search, contentDescription = "Buscar")
                        }
                    },
                )
            }
            item {
                SectionTitle("Categorias")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.categorias) { categoria ->
                        CategoryFilterChip(
                            categoria = categoria,
                            selected = false,
                            onClick = { onOpenCategory(categoria) },
                        )
                    }
                }
            }
            if (uiState.destaque.isNotEmpty()) {
                item {
                    SectionTitle("Receitas em destaque")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.destaque, key = { it.id }) { recipe ->
                            Box(modifier = Modifier.width(260.dp)) {
                                RecipeCard(recipe = recipe, onClick = { onOpenRecipe(recipe.id) })
                            }
                        }
                    }
                }
            }
            item { SectionTitle("Adicionadas recentemente") }
            items(uiState.recentes, key = { it.id }) { recipe ->
                RecipeCard(recipe = recipe, onClick = { onOpenRecipe(recipe.id) })
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val fakeRecipe = RecipeEntity(
        id = 1,
        nome = "Espaguete ao Sugo",
        categoria = RecipeCategory.MASSAS,
        tempoPreparoMin = 30,
        dificuldade = RecipeDifficulty.FACIL,
    )
    CookBookTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                isLoading = false,
                destaque = listOf(fakeRecipe),
                recentes = listOf(fakeRecipe),
            ),
            onSearch = {},
            onOpenRecipe = {},
            onOpenCategory = {},
        )
    }
}
