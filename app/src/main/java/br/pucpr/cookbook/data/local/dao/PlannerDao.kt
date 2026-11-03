package br.pucpr.cookbook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import br.pucpr.cookbook.data.local.entity.PlannedMealEntity
import br.pucpr.cookbook.data.local.relation.PlannedMealWithRecipe
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {
    @Transaction
    @Query("SELECT * FROM planned_meals WHERE dia BETWEEN :inicio AND :fim ORDER BY dia, id")
    fun observeWeek(inicio: Long, fim: Long): Flow<List<PlannedMealWithRecipe>>

    @Insert
    suspend fun insert(meal: PlannedMealEntity): Long

    @Update
    suspend fun update(meal: PlannedMealEntity)

    @Delete
    suspend fun delete(meal: PlannedMealEntity)
}
