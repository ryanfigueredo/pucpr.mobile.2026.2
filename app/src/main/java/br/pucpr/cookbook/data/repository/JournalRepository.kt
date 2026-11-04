package br.pucpr.cookbook.data.repository

import br.pucpr.cookbook.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun observeAll(): Flow<List<JournalEntryEntity>>
    suspend fun save(item: JournalEntryEntity)
    suspend fun delete(item: JournalEntryEntity)
}
