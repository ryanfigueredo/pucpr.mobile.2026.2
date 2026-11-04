package br.pucpr.cookbook.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "collection_recipes",
    primaryKeys = ["collectionId", "recipeId"],
    foreignKeys = [
        ForeignKey(entity = CollectionEntity::class, parentColumns = ["id"], childColumns = ["collectionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("recipeId")],
)
data class CollectionRecipeEntity(val collectionId: Long, val recipeId: Long)
