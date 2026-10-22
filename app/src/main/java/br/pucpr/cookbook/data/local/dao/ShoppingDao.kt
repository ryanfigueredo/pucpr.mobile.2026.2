package br.pucpr.cookbook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.pucpr.cookbook.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {

    @Query("SELECT * FROM shopping_items ORDER BY comprado ASC, nome ASC")
    fun observeAll(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE nome = :nome AND unidade = :unidade LIMIT 1")
    suspend fun findByNameAndUnit(nome: String, unidade: String): ShoppingItemEntity?

    @Insert
    suspend fun insert(item: ShoppingItemEntity): Long

    @Insert
    suspend fun insertAll(items: List<ShoppingItemEntity>)

    @Update
    suspend fun update(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET comprado = :comprado WHERE id = :id")
    suspend fun setComprado(id: Long, comprado: Boolean)

    @Delete
    suspend fun delete(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE comprado = 1")
    suspend fun deleteComprados()

    @Query("DELETE FROM shopping_items")
    suspend fun clearAll()
}
