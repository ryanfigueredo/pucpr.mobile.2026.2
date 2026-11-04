package br.pucpr.cookbook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import br.pucpr.cookbook.data.local.entity.CollectionEntity
import br.pucpr.cookbook.data.local.entity.CollectionRecipeEntity
import br.pucpr.cookbook.data.local.relation.CollectionWithRecipes
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Transaction
    @Query("SELECT * FROM collections ORDER BY nome COLLATE NOCASE")
    fun observeAll(): Flow<List<CollectionWithRecipes>>
    @Insert suspend fun insert(item: CollectionEntity): Long
    @Update suspend fun update(item: CollectionEntity)
    @Delete suspend fun delete(item: CollectionEntity)
    @Insert suspend fun insertLinks(links: List<CollectionRecipeEntity>)
    @Query("DELETE FROM collection_recipes WHERE collectionId = :id")
    suspend fun deleteLinks(id: Long)
}
