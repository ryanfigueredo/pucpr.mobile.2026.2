package br.pucpr.cookbook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import br.pucpr.cookbook.data.local.entity.IngredientEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.local.relation.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes ORDER BY criadaEm DESC")
    fun observeAll(): Flow<List<RecipeEntity>>

    // Busca por nome + filtro opcional de categoria/dificuldade numa única query reativa,
    // usada tanto pela busca da tela Início quanto pelos FilterChips da tela Explorar.
    @Query(
        "SELECT * FROM recipes WHERE " +
            "nome LIKE '%' || :termo || '%' AND " +
            "(:categoria IS NULL OR categoria = :categoria) AND " +
            "(:dificuldade IS NULL OR dificuldade = :dificuldade) " +
            "ORDER BY nome ASC",
    )
    fun observeFiltered(termo: String, categoria: String?, dificuldade: String?): Flow<List<RecipeEntity>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun observeRecipeWithIngredients(recipeId: Long): Flow<RecipeWithIngredients?>

    @Query("SELECT * FROM recipes WHERE favorita = 1 ORDER BY nome ASC")
    fun observeFavorites(): Flow<List<RecipeEntity>>

    @Query("UPDATE recipes SET favorita = :favorita WHERE id = :recipeId")
    suspend fun updateFavorite(recipeId: Long, favorita: Boolean)

    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsOf(recipeId: Long)

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun count(): Int
}
