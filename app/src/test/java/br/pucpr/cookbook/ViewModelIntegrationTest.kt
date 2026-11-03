package br.pucpr.cookbook

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import br.pucpr.cookbook.data.local.AppDatabase
import br.pucpr.cookbook.data.local.entity.IngredientEntity
import br.pucpr.cookbook.data.local.entity.PantryItemEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.local.entity.ShoppingItemEntity
import br.pucpr.cookbook.data.repository.PantryRepositoryImpl
import br.pucpr.cookbook.data.repository.PlannerRepositoryImpl
import br.pucpr.cookbook.data.repository.RecipeRepositoryImpl
import br.pucpr.cookbook.data.repository.ShoppingRepositoryImpl
import br.pucpr.cookbook.ui.components.formatDate
import br.pucpr.cookbook.ui.screens.pantry.PantryFormState
import br.pucpr.cookbook.ui.screens.pantry.PantryUiState
import br.pucpr.cookbook.ui.screens.pantry.PantryViewModel
import br.pucpr.cookbook.ui.screens.planner.MealFormState
import br.pucpr.cookbook.ui.screens.planner.PlannerViewModel
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class ViewModelIntegrationTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .setQueryCoroutineContext(dispatcher).allowMainThreadQueries().build()
    }

    @After
    fun cleanup() {
        store.clear()
        database.close()
        Dispatchers.resetMain()
    }

    private fun <T : ViewModel> keep(viewModel: T): T {
        store.put(viewModel.javaClass.name, viewModel)
        return viewModel
    }

    private fun pantry(handle: SavedStateHandle = SavedStateHandle()) = keep(
        PantryViewModel(PantryRepositoryImpl(database.pantryDao()), ShoppingRepositoryImpl(database.shoppingDao()), handle),
    )

    private fun planner() = keep(
        PlannerViewModel(
            PlannerRepositoryImpl(database.plannerDao()),
            RecipeRepositoryImpl(database.recipeDao()),
            ShoppingRepositoryImpl(database.shoppingDao()),
        ),
    )

    @Test
    fun pantryViewModelCreatesEditsAndDeletesWithConfirmation() = runTest(dispatcher) {
        val vm = pantry()
        advanceUntilIdle()
        vm.onOpenForm()
        vm.onFormChange(PantryFormState(nome = "Arroz", quantidade = "2,5", unidade = "kg"))
        vm.onSaveForm()
        vm.onSaveForm() // Toque repetido durante a gravação não deve duplicar o alimento.
        advanceUntilIdle()
        val item = vm.uiState.value.items.single()
        assertEquals(2.5, item.quantidade, 0.0)
        assertNull(vm.uiState.value.formState)
        vm.onEditItem(item)
        vm.onFormChange(checkNotNull(vm.uiState.value.formState).copy(quantidade = "1"))
        vm.onSaveForm()
        advanceUntilIdle()
        val edited = vm.uiState.value.items.single()
        assertEquals(item.id, edited.id)
        assertEquals(1.0, edited.quantidade, 0.0)
        vm.onRequestDelete(edited)
        vm.onDismissDelete()
        vm.onConfirmDelete()
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.items.size)
        vm.onRequestDelete(edited)
        vm.onConfirmDelete()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.items.isEmpty())
    }

    @Test
    fun purchasedItemPrefillsPantryAndOnlySavesAfterConfirmation() = runTest(dispatcher) {
        val id = database.shoppingDao().insert(ShoppingItemEntity(nome = "Leite", quantidade = 2.0, unidade = "l", comprado = true))
        val vm = pantry(SavedStateHandle(mapOf("shoppingItemId" to id)))
        advanceUntilIdle()
        assertTrue(database.pantryDao().observeAll().first().isEmpty())
        val form = checkNotNull(vm.uiState.value.formState)
        assertEquals("Leite", form.nome)
        assertEquals("l", form.unidade)
        vm.onFormChange(form.copy(validade = "20/12/2026"))
        vm.onSaveForm()
        advanceUntilIdle()
        assertEquals("Leite", database.pantryDao().observeAll().first().single().nome)
        assertTrue(database.shoppingDao().observeAll().first().single().comprado)
    }

    @Test
    fun invalidPantryFormDoesNotPersistAndRetainsUserInput() = runTest(dispatcher) {
        val vm = pantry()
        advanceUntilIdle()
        vm.onOpenForm()
        vm.onFormChange(PantryFormState(nome = "Feijão", quantidade = "0"))
        vm.onSaveForm()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.formState?.errorMessage)
        assertEquals("Feijão", vm.uiState.value.formState?.nome)
        assertTrue(database.pantryDao().observeAll().first().isEmpty())
    }

    @Test
    fun pantryFilterCombinesNameAndExpirationIncludingOverdueItems() {
        val hoje = LocalDate.now().toEpochDay()
        val items = listOf(
            PantryItemEntity(1, "Leite", 1.0, "l", hoje - 1),
            PantryItemEntity(2, "Leite integral", 1.0, "l", hoje + 7),
            PantryItemEntity(3, "Leite em pó", 1.0, "kg", hoje + 8),
            PantryItemEntity(4, "Arroz", 1.0, "kg", hoje),
            PantryItemEntity(5, "Leite sem validade", 1.0, "l"),
        )
        val state = PantryUiState(items = items, termoBusca = " LEITE ", somenteValidadeProxima = true)
        assertEquals(listOf(1L, 2L), state.visibleItems.map { it.id })
        assertEquals(4, state.copy(somenteValidadeProxima = false).visibleItems.size)
    }

    @Test
    fun plannerChangesWeekEditsDeletesAndScalesShoppingQuantities() = runTest(dispatcher) {
        val recipeId = database.recipeDao().insertRecipe(RecipeEntity(nome = "Sopa", porcoes = 2))
        database.recipeDao().insertIngredients(listOf(IngredientEntity(recipeId = recipeId, nome = "Batata", quantidade = 3.0, unidade = "un")))
        val vm = planner()
        advanceUntilIdle()
        val inicio = vm.uiState.value.inicioSemana
        vm.onOpenForm()
        vm.onFormChange(MealFormState(recipeId = recipeId, data = formatDate(inicio), porcoes = "4"))
        vm.onSaveForm()
        vm.onSaveForm()
        advanceUntilIdle()
        val planned = vm.uiState.value.meals.single().meal
        vm.onAddToShopping(planned)
        advanceUntilIdle()
        val shoppingItem = database.shoppingDao().observeAll().first().single()
        assertEquals(6.0, shoppingItem.quantidade, 0.0)
        assertEquals(recipeId, shoppingItem.recipeId)
        vm.onChangeWeek(1)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.meals.isEmpty())
        vm.onChangeWeek(-1)
        advanceUntilIdle()
        vm.onEditMeal(planned)
        vm.onFormChange(checkNotNull(vm.uiState.value.formState).copy(data = formatDate(inicio + 8), porcoes = "3"))
        vm.onSaveForm()
        advanceUntilIdle()
        assertEquals(inicio + 7, vm.uiState.value.inicioSemana)
        val moved = vm.uiState.value.meals.single().meal
        assertEquals(3, moved.porcoes)
        vm.onRequestDelete(moved)
        vm.onConfirmDelete()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.meals.isEmpty())
        assertEquals(1, database.recipeDao().count())
        assertEquals(1, database.shoppingDao().observeAll().first().size)
    }
}
