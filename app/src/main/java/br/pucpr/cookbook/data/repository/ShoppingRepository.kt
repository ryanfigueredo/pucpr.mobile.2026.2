package br.pucpr.cookbook.data.repository

import br.pucpr.cookbook.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    fun observeAll(): Flow<List<ShoppingItemEntity>>
    suspend fun addItem(nome: String, quantidade: Double, unidade: String, recipeId: Long? = null)
    suspend fun addIngredientsFromRecipe(ingredients: List<NewIngredient>, recipeId: Long)
    suspend fun setComprado(item: ShoppingItemEntity, comprado: Boolean)
    suspend fun deleteItem(item: ShoppingItemEntity)
    suspend fun clearComprados()
    suspend fun clearAll()
}
