package br.pucpr.cookbook.data.local

import androidx.room.TypeConverter
import br.pucpr.cookbook.data.local.entity.RecipeCategory
import br.pucpr.cookbook.data.local.entity.RecipeDifficulty

private const val STEP_SEPARATOR = "|||"

/**
 * Room só persiste tipos primitivos/String; estes conversores traduzem os
 * tipos ricos do domínio (lista de passos do modo de preparo, enums de
 * categoria/dificuldade) para texto e de volta.
 */
class Converters {

    @TypeConverter
    fun fromStepList(steps: List<String>): String = steps.joinToString(STEP_SEPARATOR)

    @TypeConverter
    fun toStepList(raw: String): List<String> =
        if (raw.isBlank()) emptyList() else raw.split(STEP_SEPARATOR)

    @TypeConverter
    fun fromCategory(category: RecipeCategory): String = category.name

    @TypeConverter
    fun toCategory(raw: String): RecipeCategory =
        RecipeCategory.entries.firstOrNull { it.name == raw } ?: RecipeCategory.MASSAS

    @TypeConverter
    fun fromDifficulty(difficulty: RecipeDifficulty): String = difficulty.name

    @TypeConverter
    fun toDifficulty(raw: String): RecipeDifficulty =
        RecipeDifficulty.entries.firstOrNull { it.name == raw } ?: RecipeDifficulty.FACIL
}
