package br.pucpr.cookbook.data.repository

import br.pucpr.cookbook.data.local.entity.PlannedMealEntity
import br.pucpr.cookbook.data.local.relation.PlannedMealWithRecipe
import kotlinx.coroutines.flow.Flow

interface PlannerRepository {
    fun observeWeek(inicio: Long, fim: Long): Flow<List<PlannedMealWithRecipe>>
    suspend fun createMeal(meal: PlannedMealEntity)
    suspend fun updateMeal(meal: PlannedMealEntity)
    suspend fun deleteMeal(meal: PlannedMealEntity)
}
