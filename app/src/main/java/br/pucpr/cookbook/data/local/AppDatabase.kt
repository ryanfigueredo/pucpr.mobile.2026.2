package br.pucpr.cookbook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.pucpr.cookbook.data.local.dao.CollectionDao
import br.pucpr.cookbook.data.local.dao.JournalDao
import br.pucpr.cookbook.data.local.dao.PantryDao
import br.pucpr.cookbook.data.local.dao.PlannerDao
import br.pucpr.cookbook.data.local.dao.RecipeDao
import br.pucpr.cookbook.data.local.dao.ShoppingDao
import br.pucpr.cookbook.data.local.entity.CollectionEntity
import br.pucpr.cookbook.data.local.entity.CollectionRecipeEntity
import br.pucpr.cookbook.data.local.entity.JournalEntryEntity
import br.pucpr.cookbook.data.local.entity.IngredientEntity
import br.pucpr.cookbook.data.local.entity.PantryItemEntity
import br.pucpr.cookbook.data.local.entity.PlannedMealEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.local.entity.ShoppingItemEntity

// exportSchema = true grava o histórico do schema em app/schemas (configurado via KSP
// no app/build.gradle.kts), necessário para validar migrações entre versões do banco.
@Database(
    entities = [
        RecipeEntity::class, IngredientEntity::class, ShoppingItemEntity::class,
        PlannedMealEntity::class, PantryItemEntity::class,
        CollectionEntity::class, CollectionRecipeEntity::class, JournalEntryEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun plannerDao(): PlannerDao
    abstract fun pantryDao(): PantryDao
    abstract fun collectionDao(): CollectionDao
    abstract fun journalDao(): JournalDao
}
