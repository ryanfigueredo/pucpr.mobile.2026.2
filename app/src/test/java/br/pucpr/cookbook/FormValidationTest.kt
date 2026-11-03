package br.pucpr.cookbook

import br.pucpr.cookbook.ui.components.formatDate
import br.pucpr.cookbook.ui.components.parseDate
import br.pucpr.cookbook.ui.components.startOfWeek
import br.pucpr.cookbook.ui.screens.pantry.PantryFormState
import br.pucpr.cookbook.ui.screens.planner.MealFormState
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class FormValidationTest {
    @Test
    fun datesRejectImpossibleDaysAndAcceptLeapYears() {
        assertNull(parseDate("31/02/2026"))
        assertNull(parseDate("29/02/2025"))
        assertNull(parseDate("2026-11-03"))
        assertNull(parseDate("01/01/0000"))
        assertEquals("29/02/2024", formatDate(checkNotNull(parseDate("29/02/2024"))))
    }

    @Test
    fun weekStartsOnMondayIncludingSundayAndYearBoundary() {
        assertEquals(parseDate("02/11/2026"), startOfWeek(checkNotNull(parseDate("08/11/2026"))))
        assertEquals(parseDate("28/12/2026"), startOfWeek(checkNotNull(parseDate("03/01/2027"))))
    }

    @Test
    fun pantryValidatesQuantityUnitAndOptionalExpiration() {
        val valid = PantryFormState(nome = " Arroz ", quantidade = "1,5", unidade = "kg")
        assertNull(valid.validate())
        assertEquals("Arroz", valid.toEntity().nome)
        assertEquals(1.5, valid.toEntity().quantidade, 0.0)
        assertNull(valid.toEntity().validade)
        listOf("0", "-1", "NaN", "Infinity", "1e309", "1,2,3", "").forEach {
            assertNotNull("Quantidade inválida: $it", valid.copy(quantidade = it).validate())
        }
        assertNotNull(valid.copy(nome = " ").validate())
        assertNotNull(valid.copy(unidade = "inválida").validate())
        assertNotNull(valid.copy(validade = "31/04/2026").validate())
        assertNull(valid.copy(validade = "30/04/2026").validate())
    }

    @Test
    fun mealRequiresRecipeValidDateAndPositiveIntegerPortions() {
        val valid = MealFormState(recipeId = 1, data = "03/11/2026", porcoes = "4")
        assertNull(valid.validate())
        assertEquals(4, valid.toEntity().porcoes)
        assertEquals(parseDate("03/11/2026"), valid.toEntity().dia)
        assertNotNull(valid.copy(recipeId = null).validate())
        assertNotNull(valid.copy(data = "31/11/2026").validate())
        listOf("0", "-1", "1,5", "2147483648", "").forEach {
            assertNotNull(valid.copy(porcoes = it).validate())
        }
    }
}
