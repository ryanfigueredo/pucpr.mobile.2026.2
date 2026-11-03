package br.pucpr.cookbook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.pucpr.cookbook.data.local.entity.PantryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY nome COLLATE NOCASE, id")
    fun observeAll(): Flow<List<PantryItemEntity>>

    @Insert
    suspend fun insert(item: PantryItemEntity): Long

    @Update
    suspend fun update(item: PantryItemEntity)

    @Delete
    suspend fun delete(item: PantryItemEntity)
}
