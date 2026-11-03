package br.pucpr.cookbook

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.pucpr.cookbook.data.local.entity.MealType
import br.pucpr.cookbook.data.local.entity.PantryItemEntity
import br.pucpr.cookbook.data.local.entity.PlannedMealEntity
import br.pucpr.cookbook.data.local.entity.RecipeEntity
import br.pucpr.cookbook.data.local.relation.PlannedMealWithRecipe
import br.pucpr.cookbook.ui.components.parseDate
import br.pucpr.cookbook.ui.components.startOfWeek
import br.pucpr.cookbook.ui.screens.pantry.PantryFormState
import br.pucpr.cookbook.ui.screens.pantry.PantryScreenContent
import br.pucpr.cookbook.ui.screens.pantry.PantryUiState
import br.pucpr.cookbook.ui.screens.planner.MealFormState
import br.pucpr.cookbook.ui.screens.planner.PlannerScreenContent
import br.pucpr.cookbook.ui.screens.planner.PlannerUiState
import br.pucpr.cookbook.ui.theme.CookBookTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35], qualifiers = "w360dp-h800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NewScreensTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun screenshot(name: String) {
        val file = File("build/screenshots/$name.png")
        file.parentFile!!.mkdirs()
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test
    fun pantryShowsStockOpensFormAndConfirmsDeletion() {
        val arroz = PantryItemEntity(1, "Arroz integral", 2.0, "kg", parseDate("20/12/2026"))
        var state by mutableStateOf(PantryUiState(isLoading = false, items = listOf(arroz)))
        var deletes = 0
        compose.setContent {
            CookBookTheme {
                PantryScreenContent(
                    uiState = state,
                    onBack = {},
                    onSearchChange = { state = state.copy(termoBusca = it) },
                    onFilterChange = { state = state.copy(somenteValidadeProxima = it) },
                    onOpenForm = { state = state.copy(formState = PantryFormState()) },
                    onEditItem = { state = state.copy(formState = PantryFormState(id = it.id, nome = it.nome, quantidade = "2", unidade = it.unidade)) },
                    onRequestDelete = { state = state.copy(pendingDelete = it) },
                    onDismissDelete = { state = state.copy(pendingDelete = null) },
                    onConfirmDelete = { deletes++; state = state.copy(pendingDelete = null) },
                    onFormChange = { state = state.copy(formState = it) },
                    onSaveForm = {},
                    onDismissForm = { state = state.copy(formState = null) },
                    onMessageShown = {},
                )
            }
        }
        compose.onNodeWithText("Arroz integral").assertIsDisplayed()
        compose.onNodeWithText("2 kg").assertIsDisplayed()
        screenshot("despensa")
        compose.onNodeWithContentDescription("Editar alimento Arroz integral").performClick()
        compose.onNodeWithText("Editar alimento").assertIsDisplayed()
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithContentDescription("Excluir alimento Arroz integral").performClick()
        assertEquals(0, deletes)
        compose.onNodeWithText("Excluir").performClick()
        assertEquals(1, deletes)
        compose.onNodeWithText("Novo alimento", useUnmergedTree = true).performClick()
        compose.onNodeWithText("Validade (dd/mm/aaaa)").assertIsDisplayed()
    }

    @Test
    fun plannerShowsRecipeAndWiresRecipeShoppingAndFormActions() {
        val dia = checkNotNull(parseDate("03/11/2026"))
        val recipe = RecipeEntity(id = 7, nome = "Sopa de legumes", porcoes = 2)
        val meal = PlannedMealEntity(id = 3, recipeId = recipe.id, dia = dia, tipo = MealType.JANTAR, porcoes = 4)
        var state by mutableStateOf(PlannerUiState(isLoading = false, inicioSemana = startOfWeek(dia), recipes = listOf(recipe), meals = listOf(PlannedMealWithRecipe(meal, recipe))))
        var openedRecipe = 0L
        var shoppingMeal = 0L
        compose.setContent {
            CookBookTheme {
                PlannerScreenContent(
                    uiState = state,
                    onBack = {},
                    onOpenRecipe = { openedRecipe = it },
                    onChangeWeek = {},
                    onCurrentWeek = {},
                    onOpenForm = { state = state.copy(formState = MealFormState()) },
                    onEditMeal = { state = state.copy(formState = MealFormState(id = it.id, recipeId = it.recipeId, data = "03/11/2026", tipo = it.tipo, porcoes = it.porcoes.toString())) },
                    onRequestDelete = { state = state.copy(pendingDelete = it) },
                    onDismissDelete = { state = state.copy(pendingDelete = null) },
                    onConfirmDelete = {},
                    onAddToShopping = { shoppingMeal = it.id },
                    onFormChange = { state = state.copy(formState = it) },
                    onSaveForm = {},
                    onDismissForm = { state = state.copy(formState = null) },
                    onMessageShown = {},
                )
            }
        }
        compose.onNodeWithText("Sopa de legumes").assertIsDisplayed().performClick()
        assertEquals(recipe.id, openedRecipe)
        compose.onNodeWithText("4 porções").assertIsDisplayed()
        screenshot("planejamento")
        compose.onNodeWithText("Enviar para compras").performClick()
        assertEquals(meal.id, shoppingMeal)
        compose.onNodeWithContentDescription("Editar refeição Sopa de legumes").performClick()
        compose.onNodeWithText("Editar refeição").assertIsDisplayed()
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("Nova refeição", useUnmergedTree = true).performClick()
        compose.onAllNodesWithText("Receita").onFirst().assertIsDisplayed()
    }
}
