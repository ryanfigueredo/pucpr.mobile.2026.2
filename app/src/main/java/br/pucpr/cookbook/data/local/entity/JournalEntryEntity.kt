package br.pucpr.cookbook.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "journal_entries",
    foreignKeys = [ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index("recipeId")],
)
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long?,
    val dia: Long,
    val porcoes: Int,
    val nota: Int,
    val observacoes: String = "",
)
