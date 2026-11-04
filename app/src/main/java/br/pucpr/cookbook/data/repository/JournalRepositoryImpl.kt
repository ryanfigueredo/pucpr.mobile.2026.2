package br.pucpr.cookbook.data.repository

import br.pucpr.cookbook.data.local.dao.JournalDao
import br.pucpr.cookbook.data.local.entity.JournalEntryEntity
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(private val dao: JournalDao) : JournalRepository {
    override fun observeAll() = dao.observeAll()
    override suspend fun save(item: JournalEntryEntity) {
        if (item.id == 0L) dao.insert(item) else dao.update(item)
    }
    override suspend fun delete(item: JournalEntryEntity) = dao.delete(item)
}
