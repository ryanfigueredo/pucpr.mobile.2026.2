package br.pucpr.cookbook.ui.components

import br.pucpr.cookbook.data.local.entity.RecipeCategory
import br.pucpr.cookbook.data.local.entity.RecipeDifficulty
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.repository.NewIngredient

data class IngredientDraft(
    val nome: String = "",
    val quantidade: String = "",
    val unidade: String = "",
)

/**
 * Estado do formulário de cadastro/edição de receita, compartilhado pelo FAB da
 * tela Explorar (criação) e pelo botão Editar da tela Detalhes (edição).
 * É um modelo puro de UI: a validação roda aqui, longe do Room/Repository.
 */
data class RecipeFormState(
    val recipeId: Long = 0L,
    val nome: String = "",
    val descricao: String = "",
    val categoria: RecipeCategory = RecipeCategory.MASSAS,
    val dificuldade: RecipeDifficulty = RecipeDifficulty.FACIL,
    val tempoPreparoMin: String = "",
    val porcoes: String = "",
    val ingredientes: List<IngredientDraft> = listOf(IngredientDraft()),
    val passos: List<String> = listOf(""),
    val errorMessage: String? = null,
)

fun RecipeFormState.validate(): String? {
    if (nome.isBlank()) return "Informe o nome da receita."
    if (tempoPreparoMin.toIntOrNull() == null || tempoPreparoMin.toInt() <= 0) {
        return "Informe um tempo de preparo válido (em minutos)."
    }
    if (porcoes.toIntOrNull() == null || porcoes.toInt() <= 0) return "Informe um número de porções válido."
    val ingredientesValidos = ingredientes.filter { it.nome.isNotBlank() }
    if (ingredientesValidos.isEmpty()) return "Adicione pelo menos um ingrediente."
    if (ingredientesValidos.any { it.quantidade.toDoubleOrNull() == null || it.unidade.isBlank() }) {
        return "Informe quantidade e unidade válidas para cada ingrediente."
    }
    val passosValidos = passos.filter { it.isNotBlank() }
    if (passosValidos.isEmpty()) return "Adicione pelo menos um passo do modo de preparo."
    return null
}

fun RecipeFormState.toRecipeEntity(criadaEm: Long): RecipeEntity = RecipeEntity(
    id = recipeId,
    nome = nome.trim(),
    descricao = descricao.trim(),
    categoria = categoria,
    tempoPreparoMin = tempoPreparoMin.toInt(),
    dificuldade = dificuldade,
    porcoes = porcoes.toInt(),
    modoPreparo = passos.filter { it.isNotBlank() },
    criadaPeloUsuario = true,
    criadaEm = criadaEm,
)

fun RecipeFormState.toNewIngredients(): List<NewIngredient> = ingredientes
    .filter { it.nome.isNotBlank() }
    .map { NewIngredient(it.nome.trim(), it.quantidade.toDouble(), it.unidade.trim()) }

fun RecipeEntity.toFormState(ingredientes: List<IngredientDraft>): RecipeFormState = RecipeFormState(
    recipeId = id,
    nome = nome,
    descricao = descricao,
    categoria = categoria,
    dificuldade = dificuldade,
    tempoPreparoMin = tempoPreparoMin.toString(),
    porcoes = porcoes.toString(),
    ingredientes = ingredientes.ifEmpty { listOf(IngredientDraft()) },
    passos = modoPreparo.ifEmpty { listOf("") },
)
