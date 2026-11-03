package br.pucpr.cookbook.data.repository

import br.pucpr.cookbook.data.local.dao.PlannerDao
import br.pucpr.cookbook.data.local.entity.PlannedMealEntity
import br.pucpr.cookbook.data.local.relation.PlannedMealWithRecipe
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class PlannerRepositoryImpl @Inject constructor(
    private val plannerDao: PlannerDao,
) : PlannerRepository {
    override fun observeWeek(inicio: Long, fim: Long): Flow<List<PlannedMealWithRecipe>> =
        plannerDao.observeWeek(inicio, fim)

    override suspend fun createMeal(meal: PlannedMealEntity) {
        plannerDao.insert(meal)
    }

    override suspend fun updateMeal(meal: PlannedMealEntity) = plannerDao.update(meal)

    override suspend fun deleteMeal(meal: PlannedMealEntity) = plannerDao.delete(meal)
}
