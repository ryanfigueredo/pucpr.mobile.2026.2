package br.pucpr.cookbook.data.repository

import br.pucpr.cookbook.data.local.entity.RecipeCategory
import br.pucpr.cookbook.data.local.entity.RecipeDifficulty
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.local.relation.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

/** DTO usado ao criar/editar uma receita, antes de existir um recipeId. */
data class NewIngredient(
    val nome: String,
    val quantidade: Double,
    val unidade: String,
)

interface RecipeRepository {
    fun observeAllRecipes(): Flow<List<RecipeEntity>>
    fun observeRecipes(
        termo: String,
        categoria: RecipeCategory?,
        dificuldade: RecipeDifficulty?,
    ): Flow<List<RecipeEntity>>

    fun observeRecipeWithIngredients(recipeId: Long): Flow<RecipeWithIngredients?>
    fun observeFavorites(): Flow<List<RecipeEntity>>
    suspend fun countRecipes(): Int
    suspend fun setFavorite(recipeId: Long, favorita: Boolean)
    suspend fun createRecipe(recipe: RecipeEntity, ingredients: List<NewIngredient>): Long
    suspend fun updateRecipe(recipe: RecipeEntity, ingredients: List<NewIngredient>)
    suspend fun deleteRecipe(recipe: RecipeEntity)
}
