package br.pucpr.cookbook.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MealType(val label: String) {
    CAFE_DA_MANHA("Café da manhã"),
    ALMOCO("Almoço"),
    LANCHE("Lanche"),
    JANTAR("Jantar"),
}

@Entity(
    tableName = "planned_meals",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recipeId"), Index("dia")],
)
data class PlannedMealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val recipeId: Long,
    // Dias desde 01/01/1970 (LocalDate.toEpochDay), sem horário ou fuso.
    val dia: Long,
    val tipo: MealType = MealType.ALMOCO,
    val porcoes: Int = 1,
)
