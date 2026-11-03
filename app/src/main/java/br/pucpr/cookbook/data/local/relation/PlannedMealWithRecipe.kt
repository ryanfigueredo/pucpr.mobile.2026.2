package br.pucpr.cookbook.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import br.pucpr.cookbook.data.local.entity.PlannedMealEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity

data class PlannedMealWithRecipe(
    @Embedded val meal: PlannedMealEntity,
    @Relation(parentColumn = "recipeId", entityColumn = "id")
    val recipe: RecipeEntity,
)
