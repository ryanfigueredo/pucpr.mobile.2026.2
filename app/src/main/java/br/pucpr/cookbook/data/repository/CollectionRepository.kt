package br.pucpr.cookbook.data.repository

import br.pucpr.cookbook.data.local.entity.CollectionEntity
import br.pucpr.cookbook.data.local.relation.CollectionWithRecipes
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun observeAll(): Flow<List<CollectionWithRecipes>>
    suspend fun save(item: CollectionEntity, recipeIds: Set<Long>)
    suspend fun delete(item: CollectionEntity)
}
