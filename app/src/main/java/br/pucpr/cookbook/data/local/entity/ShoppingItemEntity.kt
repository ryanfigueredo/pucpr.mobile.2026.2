package br.pucpr.cookbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nome: String = "",
    val quantidade: Double = 0.0,
    val unidade: String = "",
    val comprado: Boolean = false,
    val recipeId: Long? = null,
)
