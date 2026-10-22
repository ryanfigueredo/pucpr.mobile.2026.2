package br.pucpr.cookbook.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import br.pucpr.cookbook.data.local.entity.IngredientEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity

data class RecipeWithIngredients(
    @Embedded
    val recipe: RecipeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId",
    )
    val ingredients: List<IngredientEntity>,
)
