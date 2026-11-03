package br.pucpr.cookbook

import android.app.Application
import android.content.Context
import androidx.room.Room
import br.pucpr.cookbook.data.local.AppDatabase
import br.pucpr.cookbook.data.local.MIGRATION_1_2
import br.pucpr.cookbook.data.local.entity.MealType
import br.pucpr.cookbook.data.local.entity.PantryItemEntity
import br.pucpr.cookbook.data.local.entity.PlannedMealEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.repository.PantryRepositoryImpl
import br.pucpr.cookbook.data.repository.PlannerRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class DatabaseTest {
    private lateinit var database: AppDatabase
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    }

    @After
    fun cleanup() {
        database.close()
        context.deleteDatabase("migration-test.db")
    }

    @Test
    fun pantryCrudPersistsQuantitiesDatesAndSeparateLots() = runTest {
        val repository = PantryRepositoryImpl(database.pantryDao())
        repository.createItem(PantryItemEntity(nome = "Arroz", quantidade = 1.5, unidade = "kg", validade = 21000))
        repository.createItem(PantryItemEntity(nome = "Arroz", quantidade = 2.0, unidade = "kg"))
        val items = repository.observeAll().first()
        assertEquals(2, items.size)
        assertEquals(21000L, items.first().validade)
        assertNull(items.last().validade)
        repository.updateItem(items.first().copy(nome = "Arroz integral", quantidade = 0.5, validade = null))
        val updated = repository.observeAll().first().first { it.id == items.first().id }
        assertEquals("Arroz integral", updated.nome)
        assertEquals(0.5, updated.quantidade, 0.0)
        assertNull(updated.validade)
        repository.deleteItem(updated)
        assertEquals(listOf(items.last()), repository.observeAll().first())
    }

    @Test
    fun plannerCrudRespectsWeekBoundariesAndRecipeDeletion() = runTest {
        val recipeId = database.recipeDao().insertRecipe(RecipeEntity(nome = "Sopa", porcoes = 2))
        val repository = PlannerRepositoryImpl(database.plannerDao())
        repository.createMeal(PlannedMealEntity(recipeId = recipeId, dia = 100, porcoes = 4))
        repository.createMeal(PlannedMealEntity(recipeId = recipeId, dia = 106, tipo = MealType.JANTAR))
        repository.createMeal(PlannedMealEntity(recipeId = recipeId, dia = 107))
        val week = repository.observeWeek(100, 106).first()
        assertEquals(2, week.size)
        assertEquals("Sopa", week.first().recipe.nome)
        val moved = week.first().meal.copy(dia = 108, porcoes = 6)
        repository.updateMeal(moved)
        assertEquals(1, repository.observeWeek(100, 106).first().size)
        assertEquals(6, repository.observeWeek(108, 108).first().single().meal.porcoes)
        repository.deleteMeal(week.last().meal)
        assertTrue(repository.observeWeek(100, 106).first().isEmpty())
        database.recipeDao().deleteRecipe(RecipeEntity(id = recipeId))
        assertTrue(repository.observeWeek(100, 110).first().isEmpty())
    }

    @Test
    fun migrationFromOriginalSchemaPreservesRecipesFavoritesIngredientsAndShopping() = runTest {
        // Reconstrói a versão 1 a partir do schema original, sem copiar seu SQL para o teste.
        val schema = checkNotNull(javaClass.classLoader!!.getResourceAsStream("br.pucpr.cookbook.data.local.AppDatabase/1.json"))
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        context.openOrCreateDatabase("migration-test.db", Context.MODE_PRIVATE, null).use { old ->
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                old.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
                val indices = entity.optJSONArray("indices") ?: continue
                for (i in 0 until indices.length()) {
                    old.execSQL(indices.getJSONObject(i).getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
                }
            }
            val setup = schema.getJSONArray("setupQueries")
            for (index in 0 until setup.length()) old.execSQL(setup.getString(index))
            old.execSQL("INSERT INTO recipes VALUES (1, 'Sopa', 'Caseira', 'VEGETARIANO', 30, 'FACIL', 2, 'Cozinhar', '', 1, 1, 123)")
            old.execSQL("INSERT INTO ingredients VALUES (1, 1, 'Batata', 3.0, 'un')")
            old.execSQL("INSERT INTO shopping_items VALUES (1, 'Arroz', 2.0, 'kg', 1, NULL)")
            old.version = 1
        }
        database.close()
        database = Room.databaseBuilder(context, AppDatabase::class.java, "migration-test.db")
            .addMigrations(MIGRATION_1_2).allowMainThreadQueries().build()
        val recipe = checkNotNull(database.recipeDao().observeRecipeWithIngredients(1).first())
        assertEquals("Sopa", recipe.recipe.nome)
        assertTrue(recipe.recipe.favorita)
        assertTrue(recipe.recipe.criadaPeloUsuario)
        assertEquals("Batata", recipe.ingredients.single().nome)
        val bought = database.shoppingDao().observeAll().first().single()
        assertEquals("Arroz", bought.nome)
        assertTrue(bought.comprado)
        database.plannerDao().insert(PlannedMealEntity(recipeId = 1, dia = 21000))
        database.pantryDao().insert(PantryItemEntity(nome = "Feijão", quantidade = 1.0, unidade = "kg"))
        database.close()
        database = Room.databaseBuilder(context, AppDatabase::class.java, "migration-test.db")
            .addMigrations(MIGRATION_1_2).allowMainThreadQueries().build()
        assertEquals(1, database.plannerDao().observeWeek(21000, 21006).first().size)
        assertEquals("Feijão", database.pantryDao().observeAll().first().single().nome)
        assertEquals(1, database.recipeDao().count())
    }
}
