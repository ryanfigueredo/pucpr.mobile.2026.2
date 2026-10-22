package br.pucpr.cookbook.data.repository

import br.pucpr.cookbook.data.local.dao.RecipeDao
import br.pucpr.cookbook.data.local.entity.IngredientEntity
import br.pucpr.cookbook.data.local.entity.RecipeCategory
import br.pucpr.cookbook.data.local.entity.RecipeDifficulty
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.local.relation.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Implementação Room do repositório: a única camada que fala com o RecipeDao.
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao,
) : RecipeRepository {

    override fun observeAllRecipes(): Flow<List<RecipeEntity>> = recipeDao.observeAll()

    override fun observeRecipes(
        termo: String,
        categoria: RecipeCategory?,
        dificuldade: RecipeDifficulty?,
    ): Flow<List<RecipeEntity>> = recipeDao.observeFiltered(termo, categoria?.name, dificuldade?.name)

    override fun observeRecipeWithIngredients(recipeId: Long): Flow<RecipeWithIngredients?> =
        recipeDao.observeRecipeWithIngredients(recipeId)

    override fun observeFavorites(): Flow<List<RecipeEntity>> = recipeDao.observeFavorites()

    override suspend fun countRecipes(): Int = recipeDao.count()

    override suspend fun setFavorite(recipeId: Long, favorita: Boolean) {
        recipeDao.updateFavorite(recipeId, favorita)
    }

    override suspend fun createRecipe(recipe: RecipeEntity, ingredients: List<NewIngredient>): Long {
        val recipeId = recipeDao.insertRecipe(recipe)
        if (ingredients.isNotEmpty()) {
            recipeDao.insertIngredients(ingredients.map { it.toEntity(recipeId) })
        }
        return recipeId
    }

    override suspend fun updateRecipe(recipe: RecipeEntity, ingredients: List<NewIngredient>) {
        recipeDao.updateRecipe(recipe)
        recipeDao.deleteIngredientsOf(recipe.id)
        if (ingredients.isNotEmpty()) {
            recipeDao.insertIngredients(ingredients.map { it.toEntity(recipe.id) })
        }
    }

    override suspend fun deleteRecipe(recipe: RecipeEntity) = recipeDao.deleteRecipe(recipe)

    private fun NewIngredient.toEntity(recipeId: Long) = IngredientEntity(
        recipeId = recipeId,
        nome = nome,
        quantidade = quantidade,
        unidade = unidade,
    )
}
