package br.pucpr.cookbook.data.repository

import androidx.room.withTransaction
import br.pucpr.cookbook.data.local.AppDatabase
import br.pucpr.cookbook.data.local.dao.CollectionDao
import br.pucpr.cookbook.data.local.entity.CollectionEntity
import br.pucpr.cookbook.data.local.entity.CollectionRecipeEntity
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val dao: CollectionDao,
    private val database: AppDatabase,
) : CollectionRepository {
    override fun observeAll() = dao.observeAll()
    override suspend fun save(item: CollectionEntity, recipeIds: Set<Long>) {
        database.withTransaction {
            val id = if (item.id == 0L) dao.insert(item) else {
                dao.update(item)
                dao.deleteLinks(item.id)
                item.id
            }
            dao.insertLinks(recipeIds.map { CollectionRecipeEntity(id, it) })
        }
    }
    override suspend fun delete(item: CollectionEntity) = dao.delete(item)
}
