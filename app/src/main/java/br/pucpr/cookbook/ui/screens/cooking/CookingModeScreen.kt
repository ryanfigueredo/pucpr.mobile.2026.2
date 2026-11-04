package br.pucpr.cookbook.ui.screens.cooking

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.pucpr.cookbook.data.local.entity.IngredientEntity
import br.pucpr.cookbook.data.local.entity.RecipeCategory
import br.pucpr.cookbook.data.local.entity.RecipeDifficulty
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.ui.components.EmptyState
import br.pucpr.cookbook.ui.components.LoadingIndicator
import br.pucpr.cookbook.ui.components.RecipeImagePlaceholder
import br.pucpr.cookbook.ui.components.categoryColor
import br.pucpr.cookbook.ui.components.formatQuantidadeComUnidade
import br.pucpr.cookbook.ui.theme.CookBookTheme
import java.util.Locale

/** Agrupa os eventos do temporizador para não repetir 6 lambdas em cada composable. */
private class TimerActions(
    val onMinutesChange: (Int) -> Unit,
    val onSetMinutes: (Int) -> Unit,
    val onStart: () -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onReset: () -> Unit,
)

@Composable
fun CookingModeScreen(
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    viewModel: CookingModeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CookingModeContent(
        uiState = uiState,
        onBack = onBack,
        onGoHome = onGoHome,
        onPrevious = viewModel::onPreviousStep,
        onNext = viewModel::onNextStep,
        onFinish = viewModel::onFinishRecipe,
        onShowIngredients = viewModel::onShowIngredients,
        onHideIngredients = viewModel::onHideIngredients,
        onToggleIngredient = viewModel::onToggleIngredient,
        timer = TimerActions(
            onMinutesChange = viewModel::onTimerMinutesChange,
            onSetMinutes = viewModel::onTimerSetMinutes,
            onStart = viewModel::onTimerStart,
            onPause = viewModel::onTimerPause,
            onResume = viewModel::onTimerResume,
            onReset = viewModel::onTimerReset,
        ),
    )
}

@Composable
private fun CookingModeContent(
    uiState: CookingUiState,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    onShowIngredients: () -> Unit,
    onHideIngredients: () -> Unit,
    onToggleIngredient: (Long) -> Unit,
    timer: TimerActions,
) {
    val recipe = uiState.recipe

    Scaffold(
        topBar = { CookingTopBar(uiState, onBack, onShowIngredients) },
        bottomBar = {
            if (recipe != null && !uiState.finished && uiState.steps.isNotEmpty()) {
                CookingBottomBar(uiState, onPrevious, onNext, onFinish)
            }
        },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when {
            uiState.isLoading -> LoadingIndicator(modifier = modifier)
            recipe == null -> EmptyState(
                icon = Icons.Filled.Restaurant,
                title = "Receita não encontrada",
                description = uiState.errorMessage ?: "Não foi possível carregar esta receita.",
                modifier = modifier,
                actionLabel = "Voltar",
                onAction = onBack,
            )
            uiState.finished -> FinishedContent(recipe.nome, onBack, onGoHome, modifier)
            uiState.steps.isEmpty() -> EmptyState(
                icon = Icons.Filled.Restaurant,
                title = "Receita sem etapas",
                description = "Esta receita não tem modo de preparo cadastrado. Edite a receita para adicionar as etapas.",
                modifier = modifier,
                actionLabel = "Voltar",
                onAction = onBack,
            )
            else -> StepContent(uiState, recipe, timer, modifier)
        }
    }

    if (uiState.showIngredients && recipe != null) {
        IngredientsSheet(uiState, onHideIngredients, onToggleIngredient)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CookingTopBar(
    uiState: CookingUiState,
    onBack: () -> Unit,
    onShowIngredients: () -> Unit,
) {
    val emAndamento = uiState.steps.isNotEmpty() && !uiState.finished
    val progressoAnimado by animateFloatAsState(targetValue = uiState.progress, label = "cookingProgress")

    Column {
        TopAppBar(
            title = {
                Column {
                    Text(uiState.recipe?.nome.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (emAndamento) {
                        Text(
                            "Etapa ${uiState.currentStep + 1} de ${uiState.totalSteps}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            },
            actions = {
                if (uiState.recipe != null) {
                    IconButton(onClick = onShowIngredients) {
                        Icon(Icons.Filled.Checklist, contentDescription = "Ingredientes")
                    }
                }
            },
        )
        if (emAndamento) {
            LinearProgressIndicator(
                progress = { progressoAnimado },
                modifier = Modifier.fillMaxWidth().height(6.dp),
            )
        }
    }
}

@Composable
private fun StepContent(
    uiState: CookingUiState,
    recipe: RecipeEntity,
    timer: TimerActions,
    modifier: Modifier = Modifier,
) {
    val cor = categoryColor(recipe.categoria)
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RecipeImagePlaceholder(categoria = recipe.categoria, size = 56.dp, cornerRadius = 16.dp)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(recipe.categoria.label, style = MaterialTheme.typography.labelLarge, color = cor)
                Text(
                    "${recipe.tempoPreparoMin} min · ${recipe.dificuldade.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                val direcao = if (targetState > initialState) 1 else -1
                (slideInHorizontally { largura -> direcao * largura } + fadeIn()) togetherWith
                    (slideOutHorizontally { largura -> -direcao * largura } + fadeOut())
            },
            label = "cookingStep",
        ) { etapa ->
            StepCard(
                numero = etapa + 1,
                instrucao = uiState.steps.getOrElse(etapa) { "" },
                destaque = cor,
            )
        }

        TimerCard(uiState, timer)
    }
}

@Composable
private fun StepCard(numero: Int, instrucao: String, destaque: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(destaque.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "$numero",
                        style = MaterialTheme.typography.titleLarge,
                        color = destaque,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Etapa $numero", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(instrucao, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Normal)
        }
    }
}

@Composable
private fun TimerCard(uiState: CookingUiState, timer: TimerActions) {
    val terminou = uiState.timerStatus == TimerStatus.FINISHED
    val container by animateColorAsState(
        targetValue = if (terminou) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        label = "timerContainer",
    )
    val contentColor = if (terminou) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
    val minutos = uiState.timerConfiguredSeconds / 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = contentColor),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Timer, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Temporizador", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(
                formatTimer(uiState.timerRemainingSeconds),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (terminou) {
                Text(
                    "Tempo esgotado!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (uiState.timerStatus == TimerStatus.IDLE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledIconButton(
                        onClick = { timer.onMinutesChange(-1) },
                        enabled = minutos > MIN_TIMER_MINUTES,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Diminuir minutos", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "$minutos min",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    FilledIconButton(
                        onClick = { timer.onMinutesChange(1) },
                        enabled = minutos < MAX_TIMER_MINUTES,
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Aumentar minutos")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    listOf(5, 10, 15).forEach { preset ->
                        AssistChip(onClick = { timer.onSetMinutes(preset) }, label = { Text("$preset min") })
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                when (uiState.timerStatus) {
                    TimerStatus.IDLE -> Button(onClick = timer.onStart, modifier = Modifier.weight(1f)) {
                        ButtonLabel(Icons.Filled.PlayArrow, "Iniciar")
                    }
                    TimerStatus.RUNNING -> {
                        Button(onClick = timer.onPause, modifier = Modifier.weight(1f)) {
                            ButtonLabel(Icons.Filled.Pause, "Pausar")
                        }
                        OutlinedButton(onClick = timer.onReset, modifier = Modifier.weight(1f)) {
                            ButtonLabel(Icons.Filled.Refresh, "Reiniciar")
                        }
                    }
                    TimerStatus.PAUSED -> {
                        Button(onClick = timer.onResume, modifier = Modifier.weight(1f)) {
                            ButtonLabel(Icons.Filled.PlayArrow, "Continuar")
                        }
                        OutlinedButton(onClick = timer.onReset, modifier = Modifier.weight(1f)) {
                            ButtonLabel(Icons.Filled.Refresh, "Reiniciar")
                        }
                    }
                    TimerStatus.FINISHED -> Button(onClick = timer.onReset, modifier = Modifier.weight(1f)) {
                        ButtonLabel(Icons.Filled.Refresh, "Reiniciar")
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonLabel(icon: ImageVector, text: String) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
    Spacer(modifier = Modifier.width(8.dp))
    Text(text)
}

@Composable
private fun CookingBottomBar(
    uiState: CookingUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = !uiState.isFirstStep,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Anterior")
            }
            if (uiState.isLastStep) {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalizar receita")
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Próximo")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun FinishedContent(
    nomeReceita: String,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Receita concluída!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(nomeReceita, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Bom apetite!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) { Text("Voltar aos detalhes") }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onGoHome,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ir para o início")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientsSheet(
    uiState: CookingUiState,
    onDismiss: () -> Unit,
    onToggle: (Long) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text("Ingredientes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Para ${uiState.porcoes} ${if (uiState.porcoes == 1) "porção" else "porções"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.ingredients.isEmpty()) {
                Text("Esta receita não tem ingredientes cadastrados.", style = MaterialTheme.typography.bodyLarge)
            }
            uiState.ingredients.forEach { ingrediente ->
                val marcado = ingrediente.id in uiState.checkedIngredientIds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(ingrediente.id) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = marcado, onCheckedChange = { onToggle(ingrediente.id) })
                    Text(
                        "${formatQuantidadeComUnidade(ingrediente.quantidade * uiState.fatorPorcoes, ingrediente.unidade)} de ${ingrediente.nome}",
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (marcado) TextDecoration.LineThrough else null,
                        color = if (marcado) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

private fun formatTimer(totalSegundos: Int): String =
    String.format(Locale.ROOT, "%02d:%02d", totalSegundos / 60, totalSegundos % 60)

@Preview(showBackground = true)
@Composable
private fun CookingModePreview() {
    val receita = RecipeEntity(
        id = 1,
        nome = "Espaguete ao Sugo",
        categoria = RecipeCategory.MASSAS,
        tempoPreparoMin = 30,
        dificuldade = RecipeDifficulty.FACIL,
        porcoes = 4,
        modoPreparo = listOf("Cozinhe a massa em água fervente.", "Prepare o molho.", "Misture e sirva."),
    )
    CookBookTheme {
        CookingModeContent(
            uiState = CookingUiState(
                isLoading = false,
                recipe = receita,
                ingredients = listOf(IngredientEntity(id = 1, recipeId = 1, nome = "Espaguete", quantidade = 500.0, unidade = "g")),
                steps = receita.modoPreparo,
                currentStep = 1,
                porcoes = 4,
            ),
            onBack = {},
            onGoHome = {},
            onPrevious = {},
            onNext = {},
            onFinish = {},
            onShowIngredients = {},
            onHideIngredients = {},
            onToggleIngredient = {},
            timer = TimerActions({}, {}, {}, {}, {}, {}),
        )
    }
}
