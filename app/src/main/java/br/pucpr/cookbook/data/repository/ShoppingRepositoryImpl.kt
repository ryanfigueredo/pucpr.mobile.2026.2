package br.pucpr.cookbook.data.repository

import br.pucpr.cookbook.data.local.dao.ShoppingDao
import br.pucpr.cookbook.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ShoppingRepositoryImpl @Inject constructor(
    private val shoppingDao: ShoppingDao,
) : ShoppingRepository {

    override fun observeAll(): Flow<List<ShoppingItemEntity>> = shoppingDao.observeAll()

    override suspend fun addItem(nome: String, quantidade: Double, unidade: String, recipeId: Long?) {
        mergeOrInsert(nome, quantidade, unidade, recipeId)
    }

    override suspend fun addIngredientsFromRecipe(ingredients: List<NewIngredient>, recipeId: Long) {
        ingredients.forEach { mergeOrInsert(it.nome, it.quantidade, it.unidade, recipeId) }
    }

    // Ingredientes com mesmo nome + unidade somam a quantidade em vez de duplicar a linha.
    private suspend fun mergeOrInsert(nome: String, quantidade: Double, unidade: String, recipeId: Long?) {
        val existing = shoppingDao.findByNameAndUnit(nome, unidade)
        if (existing != null) {
            shoppingDao.update(existing.copy(quantidade = existing.quantidade + quantidade))
        } else {
            shoppingDao.insert(
                ShoppingItemEntity(nome = nome, quantidade = quantidade, unidade = unidade, recipeId = recipeId),
            )
        }
    }

    override suspend fun setComprado(item: ShoppingItemEntity, comprado: Boolean) {
        shoppingDao.setComprado(item.id, comprado)
    }

    override suspend fun deleteItem(item: ShoppingItemEntity) = shoppingDao.delete(item)

    override suspend fun clearComprados() = shoppingDao.deleteComprados()

    override suspend fun clearAll() = shoppingDao.clearAll()
}
