package br.pucpr.cookbook.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import br.pucpr.cookbook.data.local.entity.CollectionEntity
import br.pucpr.cookbook.data.local.entity.CollectionRecipeEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity

data class CollectionWithRecipes(
    @Embedded val collection: CollectionEntity,
    @Relation(parentColumn = "id", entityColumn = "id", associateBy = Junction(CollectionRecipeEntity::class, parentColumn = "collectionId", entityColumn = "recipeId"))
    val recipes: List<RecipeEntity>,
)
