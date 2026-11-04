package br.pucpr.cookbook

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import br.pucpr.cookbook.data.local.AppDatabase
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.repository.NewIngredient
import br.pucpr.cookbook.data.repository.RecipeRepositoryImpl
import br.pucpr.cookbook.ui.screens.cooking.CookingModeViewModel
import br.pucpr.cookbook.ui.screens.cooking.TimerStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class CookingModeViewModelTest {
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

    private suspend fun criarReceita(passos: List<String>, porcoes: Int = 4): Long =
        RecipeRepositoryImpl(database.recipeDao()).createRecipe(
            RecipeEntity(nome = "Bolo", porcoes = porcoes, tempoPreparoMin = 40, modoPreparo = passos),
            listOf(NewIngredient("Farinha", 200.0, "g")),
        )

    private fun viewModel(recipeId: Long, porcoes: Int = 0): CookingModeViewModel {
        val handle = SavedStateHandle(mapOf("recipeId" to recipeId, "porcoes" to porcoes))
        return CookingModeViewModel(RecipeRepositoryImpl(database.recipeDao()), handle).also {
            store.put("cooking-${System.nanoTime()}", it)
        }
    }

    @Test
    fun loadsRecipeStepsInOriginalOrderAndRespectsNavigationLimits() = runTest(dispatcher) {
        val vm = viewModel(criarReceita(listOf("Misture", "Asse", "Sirva")))
        advanceUntilIdle()

        var s = vm.uiState.value
        assertEquals("Bolo", s.recipe?.nome)
        assertEquals(listOf("Misture", "Asse", "Sirva"), s.steps)
        assertTrue(s.isFirstStep)
        assertEquals(1f / 3f, s.progress, 0.001f)

        vm.onPreviousStep() // não volta antes da primeira
        assertEquals(0, vm.uiState.value.currentStep)

        vm.onNextStep(); vm.onNextStep()
        s = vm.uiState.value
        assertTrue(s.isLastStep)
        assertEquals(1f, s.progress, 0.001f)

        vm.onNextStep() // não avança além da última
        assertEquals(2, vm.uiState.value.currentStep)

        vm.onPreviousStep()
        assertEquals(1, vm.uiState.value.currentStep)
    }

    @Test
    fun finishOnlyWorksOnLastStep() = runTest(dispatcher) {
        val vm = viewModel(criarReceita(listOf("A", "B")))
        advanceUntilIdle()

        vm.onFinishRecipe()
        assertFalse(vm.uiState.value.finished)

        vm.onNextStep()
        vm.onFinishRecipe()
        assertTrue(vm.uiState.value.finished)
    }

    @Test
    fun recipeWithoutStepsOrWithBlankStepsIsHandled() = runTest(dispatcher) {
        val vm = viewModel(criarReceita(listOf("", "   ")))
        advanceUntilIdle()

        val s = vm.uiState.value
        assertFalse(s.isLoading)
        assertNotNull(s.recipe)
        assertTrue(s.steps.isEmpty())
        assertEquals(0f, s.progress, 0.001f)

        vm.onNextStep()
        vm.onFinishRecipe()
        assertEquals(0, vm.uiState.value.currentStep)
        assertFalse(vm.uiState.value.finished)
    }

    @Test
    fun invalidOrMissingRecipeShowsNotFoundState() = runTest(dispatcher) {
        val invalido = viewModel(-1L)
        assertFalse(invalido.uiState.value.isLoading)
        assertNull(invalido.uiState.value.recipe)

        val inexistente = viewModel(9_999L)
        advanceUntilIdle()
        assertFalse(inexistente.uiState.value.isLoading)
        assertNull(inexistente.uiState.value.recipe)
    }

    @Test
    fun adjustedPortionsScaleIngredientsWithoutChangingTheDatabase() = runTest(dispatcher) {
        val id = criarReceita(listOf("Misture"), porcoes = 4)
        val vm = viewModel(id, porcoes = 8)
        advanceUntilIdle()

        assertEquals(8, vm.uiState.value.porcoes)
        assertEquals(2.0, vm.uiState.value.fatorPorcoes, 0.0001)

        val original = RecipeRepositoryImpl(database.recipeDao()).observeRecipeWithIngredients(id).first()
        assertEquals(200.0, original!!.ingredients.single().quantidade, 0.0001)
        assertEquals(4, original.recipe.porcoes)

        val padrao = viewModel(id) // sem ajuste: usa as porções da receita
        advanceUntilIdle()
        assertEquals(4, padrao.uiState.value.porcoes)
        assertEquals(1.0, padrao.uiState.value.fatorPorcoes, 0.0001)
    }

    @Test
    fun ingredientCheckboxesToggle() = runTest(dispatcher) {
        val vm = viewModel(criarReceita(listOf("Misture")))
        advanceUntilIdle()
        val ingredienteId = vm.uiState.value.ingredients.single().id

        vm.onToggleIngredient(ingredienteId)
        assertTrue(ingredienteId in vm.uiState.value.checkedIngredientIds)
        vm.onToggleIngredient(ingredienteId)
        assertFalse(ingredienteId in vm.uiState.value.checkedIngredientIds)
    }

    @Test
    fun timerStartsPausesResumesResetsAndFinishesWithoutDuplicatedCounting() = runTest(dispatcher) {
        val vm = viewModel(criarReceita(listOf("Misture", "Asse")))
        advanceUntilIdle()

        vm.onTimerSetMinutes(1)
        assertEquals(60, vm.uiState.value.timerRemainingSeconds)

        vm.onTimerStart()
        vm.onTimerStart() // segundo toque não cria segunda contagem
        advanceTimeBy(3_100); runCurrent()
        assertEquals(TimerStatus.RUNNING, vm.uiState.value.timerStatus)
        assertEquals(57, vm.uiState.value.timerRemainingSeconds)

        vm.onTimerPause()
        val pausado = vm.uiState.value.timerRemainingSeconds
        advanceTimeBy(5_000); runCurrent()
        assertEquals(TimerStatus.PAUSED, vm.uiState.value.timerStatus)
        assertEquals(pausado, vm.uiState.value.timerRemainingSeconds)

        vm.onTimerResume()
        advanceTimeBy(2_100); runCurrent()
        assertEquals(pausado - 2, vm.uiState.value.timerRemainingSeconds)

        vm.onNextStep() // navegar entre etapas não interrompe o temporizador
        assertEquals(TimerStatus.RUNNING, vm.uiState.value.timerStatus)

        vm.onTimerReset()
        assertEquals(TimerStatus.IDLE, vm.uiState.value.timerStatus)
        assertEquals(60, vm.uiState.value.timerRemainingSeconds)

        vm.onTimerStart()
        advanceTimeBy(61_000); runCurrent()
        assertEquals(TimerStatus.FINISHED, vm.uiState.value.timerStatus)
        assertEquals(0, vm.uiState.value.timerRemainingSeconds)
    }

    @Test
    fun timerMinutesAreClampedAndLockedWhileRunning() = runTest(dispatcher) {
        val vm = viewModel(criarReceita(listOf("Misture")))
        advanceUntilIdle()

        vm.onTimerSetMinutes(0)
        assertEquals(60, vm.uiState.value.timerConfiguredSeconds)
        vm.onTimerSetMinutes(10_000)
        assertEquals(180 * 60, vm.uiState.value.timerConfiguredSeconds)

        vm.onTimerSetMinutes(5)
        vm.onTimerStart()
        vm.onTimerSetMinutes(20) // ignorado com o temporizador ativo
        assertEquals(300, vm.uiState.value.timerConfiguredSeconds)
    }
}
