package br.pucpr.cookbook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.pucpr.cookbook.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY dia DESC, id DESC")
    fun observeAll(): Flow<List<JournalEntryEntity>>
    @Insert suspend fun insert(item: JournalEntryEntity): Long
    @Update suspend fun update(item: JournalEntryEntity)
    @Delete suspend fun delete(item: JournalEntryEntity)
}
