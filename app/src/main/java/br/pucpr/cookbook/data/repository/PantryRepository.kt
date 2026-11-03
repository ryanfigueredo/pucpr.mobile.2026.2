package br.pucpr.cookbook.data.repository

import br.pucpr.cookbook.data.local.entity.PantryItemEntity
import kotlinx.coroutines.flow.Flow

interface PantryRepository {
    fun observeAll(): Flow<List<PantryItemEntity>>
    suspend fun createItem(item: PantryItemEntity)
    suspend fun updateItem(item: PantryItemEntity)
    suspend fun deleteItem(item: PantryItemEntity)
}
