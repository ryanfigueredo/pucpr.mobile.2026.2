package br.pucpr.cookbook.data.repository

import br.pucpr.cookbook.data.local.dao.PantryDao
import br.pucpr.cookbook.data.local.entity.PantryItemEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class PantryRepositoryImpl @Inject constructor(
    private val pantryDao: PantryDao,
) : PantryRepository {
    override fun observeAll(): Flow<List<PantryItemEntity>> = pantryDao.observeAll()

    override suspend fun createItem(item: PantryItemEntity) {
        pantryDao.insert(item)
    }

    override suspend fun updateItem(item: PantryItemEntity) = pantryDao.update(item)

    override suspend fun deleteItem(item: PantryItemEntity) = pantryDao.delete(item)
}
