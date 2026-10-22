package br.pucpr.cookbook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.pucpr.cookbook.data.local.dao.RecipeDao
import br.pucpr.cookbook.data.local.dao.ShoppingDao
import br.pucpr.cookbook.data.local.entity.IngredientEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.local.entity.ShoppingItemEntity

// exportSchema = true grava o histórico do schema em app/schemas (configurado via KSP
// no app/build.gradle.kts), necessário para validar migrações entre versões do banco.
@Database(
    entities = [RecipeEntity::class, IngredientEntity::class, ShoppingItemEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun shoppingDao(): ShoppingDao
}
