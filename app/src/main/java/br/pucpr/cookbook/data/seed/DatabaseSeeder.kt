package br.pucpr.cookbook.data.seed

import br.pucpr.cookbook.data.repository.RecipeRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Popula o banco com receitas de exemplo apenas na primeira execução (tabela vazia).
 * Chamado a partir de uma coroutine no MainActivity; o acesso ao Room sempre passa
 * pelo RecipeRepository, nunca direto pelo DAO.
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val recipeRepository: RecipeRepository,
) {
    suspend fun seedIfNeeded() {
        if (recipeRepository.countRecipes() == 0) {
            RecipeSeedData.recipes.forEach { (recipe, ingredients) ->
                recipeRepository.createRecipe(recipe, ingredients)
            }
        }
    }
}
