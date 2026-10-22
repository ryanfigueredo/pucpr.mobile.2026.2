package br.pucpr.cookbook.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.cookbook.data.local.entity.IngredientEntity
import br.pucpr.cookbook.data.local.entity.RecipeCategory
import br.pucpr.cookbook.data.local.entity.RecipeDifficulty
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.ui.components.ConfirmDialog
import br.pucpr.cookbook.ui.components.LoadingIndicator
import br.pucpr.cookbook.ui.components.RecipeFormSheet
import br.pucpr.cookbook.ui.components.RecipeImagePlaceholder
import br.pucpr.cookbook.ui.components.categoryColor
import br.pucpr.cookbook.ui.components.formatQuantidadeComUnidade
import br.pucpr.cookbook.ui.theme.CookBookTheme

@Composable
fun DetailsScreen(
    onBack: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onBack()
    }

    DetailsScreenContent(
        uiState = uiState,
        onBack = onBack,
        onAdjustPortions = viewModel::onAdjustPortions,
        onToggleFavorite = viewModel::onToggleFavorite,
        onAddToShoppingList = viewModel::onAddToShoppingList,
        onOpenEditForm = viewModel::onOpenEditForm,
        onFormChange = viewModel::onFormChange,
        onDismissForm = viewModel::onDismissForm,
        onSaveForm = viewModel::onSaveForm,
        onRequestDelete = viewModel::onRequestDelete,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
        onConfirmDelete = viewModel::onConfirmDelete,
        onSnackbarShown = viewModel::onSnackbarShown,
        onErrorShown = viewModel::onErrorShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsScreenContent(
    uiState: DetailsUiState,
    onBack: () -> Unit,
    onAdjustPortions: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToShoppingList: () -> Unit,
    onOpenEditForm: () -> Unit,
    onFormChange: (br.pucpr.cookbook.ui.components.RecipeFormState) -> Unit,
    onDismissForm: () -> Unit,
    onSaveForm: () -> Unit,
    onRequestDelete: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onSnackbarShown: () -> Unit,
    onErrorShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val mensagem = uiState.snackbarMessage ?: uiState.errorMessage
    LaunchedEffect(mensagem) {
        mensagem?.let {
            snackbarHostState.showSnackbar(it)
            onSnackbarShown()
            onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.recipe?.nome.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (uiState.recipe?.criadaPeloUsuario == true) {
                        IconButton(onClick = onOpenEditForm) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = onRequestDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) } },
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
            uiState.recipe != null -> RecipeDetailsBody(
                modifier = Modifier.padding(innerPadding),
                recipe = uiState.recipe,
                ingredients = uiState.ingredients,
                porcoesAjustadas = uiState.porcoesAjustadas,
                onAdjustPortions = onAdjustPortions,
                onToggleFavorite = onToggleFavorite,
                onAddToShoppingList = onAddToShoppingList,
            )
        }

        if (uiState.showDeleteDialog) {
            ConfirmDialog(
                title = "Excluir receita",
                message = "Tem certeza que deseja excluir \"${uiState.recipe?.nome}\"? Essa ação não pode ser desfeita.",
                confirmLabel = "Excluir",
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDeleteDialog,
            )
        }

        uiState.formState?.let { form ->
            RecipeFormSheet(
                formState = form,
                isEditing = true,
                onChange = onFormChange,
                onSave = onSaveForm,
                onDismiss = onDismissForm,
            )
        }
    }
}

@Composable
private fun RecipeDetailsBody(
    recipe: RecipeEntity,
    ingredients: List<IngredientEntity>,
    porcoesAjustadas: Int,
    onAdjustPortions: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToShoppingList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fator = porcoesAjustadas.toDouble() / recipe.porcoes.toDouble()
    val corCategoria = categoryColor(recipe.categoria)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RecipeImagePlaceholder(categoria = recipe.categoria, size = 112.dp, cornerRadius = 24.dp)
                Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(
                        recipe.categoria.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = corCategoria,
                        modifier = Modifier
                            .background(corCategoria.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "${recipe.tempoPreparoMin} min · ${recipe.dificuldade.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FilledIconButton(
                    onClick = onToggleFavorite,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (recipe.favorita) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                ) {
                    Icon(
                        imageVector = if (recipe.favorita) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (recipe.favorita) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item { Text(recipe.descricao, style = MaterialTheme.typography.bodyLarge) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Porções", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                FilledIconButton(
                    onClick = { onAdjustPortions(porcoesAjustadas - 1) },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Diminuir porções", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "$porcoesAjustadas",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                FilledIconButton(onClick = { onAdjustPortions(porcoesAjustadas + 1) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Aumentar porções")
                }
            }
        }
        item { Text("Ingredientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    ingredients.forEach { ingredient ->
                        val quantidadeAjustada = ingredient.quantidade * fator
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(corCategoria, androidx.compose.foundation.shape.CircleShape),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "${formatQuantidadeComUnidade(quantidadeAjustada, ingredient.unidade)} de ${ingredient.nome}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = onAddToShoppingList,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adicionar ingredientes à lista de compras")
            }
        }
        item { Text("Modo de preparo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        itemsIndexed(recipe.modoPreparo) { index, passo ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(corCategoria.copy(alpha = 0.16f), androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = corCategoria,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(passo, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsScreenPreview() {
    val fakeRecipe = RecipeEntity(
        id = 1,
        nome = "Espaguete ao Sugo",
        descricao = "Massa clássica com molho de tomate fresco.",
        categoria = RecipeCategory.MASSAS,
        tempoPreparoMin = 30,
        dificuldade = RecipeDifficulty.FACIL,
        porcoes = 4,
        modoPreparo = listOf("Cozinhe a massa.", "Prepare o molho.", "Misture e sirva."),
    )
    val fakeIngredients = listOf(
        IngredientEntity(id = 1, recipeId = 1, nome = "Espaguete", quantidade = 500.0, unidade = "g"),
        IngredientEntity(id = 2, recipeId = 1, nome = "Tomate", quantidade = 6.0, unidade = "unidade"),
    )
    CookBookTheme {
        DetailsScreenContent(
            uiState = DetailsUiState(
                isLoading = false,
                recipe = fakeRecipe,
                ingredients = fakeIngredients,
                porcoesAjustadas = 4,
            ),
            onBack = {},
            onAdjustPortions = {},
            onToggleFavorite = {},
            onAddToShoppingList = {},
            onOpenEditForm = {},
            onFormChange = {},
            onDismissForm = {},
            onSaveForm = {},
            onRequestDelete = {},
            onDismissDeleteDialog = {},
            onConfirmDelete = {},
            onSnackbarShown = {},
            onErrorShown = {},
        )
    }
}
