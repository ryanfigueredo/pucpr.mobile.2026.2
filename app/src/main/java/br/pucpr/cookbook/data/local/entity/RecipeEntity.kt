package br.pucpr.cookbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RecipeCategory(val label: String) {
    MASSAS("Massas"),
    CARNES("Carnes"),
    AVES("Aves"),
    VEGETARIANO("Vegetariano"),
    SOBREMESAS("Sobremesas"),
    BEBIDAS("Bebidas"),
}

enum class RecipeDifficulty(val label: String) {
    FACIL("Fácil"),
    MEDIA("Média"),
    DIFICIL("Difícil"),
}

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nome: String = "",
    val descricao: String = "",
    val categoria: RecipeCategory = RecipeCategory.MASSAS,
    val tempoPreparoMin: Int = 0,
    val dificuldade: RecipeDifficulty = RecipeDifficulty.FACIL,
    val porcoes: Int = 1,
    val modoPreparo: List<String> = emptyList(),
    val imagemRes: String = "",
    val favorita: Boolean = false,
    val criadaPeloUsuario: Boolean = false,
    val criadaEm: Long = System.currentTimeMillis(),
)
