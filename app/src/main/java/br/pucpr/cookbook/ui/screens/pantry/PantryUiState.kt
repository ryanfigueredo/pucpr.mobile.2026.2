package br.pucpr.cookbook.ui.screens.pantry

import br.pucpr.cookbook.data.local.entity.PantryItemEntity
import br.pucpr.cookbook.data.model.UnidadeMedida
import br.pucpr.cookbook.ui.components.parseDate
import java.time.LocalDate

data class PantryFormState(
    val id: Long = 0L,
    val nome: String = "",
    val quantidade: String = "",
    val unidade: String = UnidadeMedida.default.label,
    val validade: String = "",
    val errorMessage: String? = null,
) {
    val quantidadeValida: Double?
        get() = quantidade.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }

    fun validate(): String? = when {
        nome.isBlank() -> "Informe o nome do alimento."
        quantidadeValida == null -> "Informe uma quantidade maior que zero."
        UnidadeMedida.fromLabel(unidade) == null -> "Escolha uma unidade válida."
        validade.isNotBlank() && parseDate(validade) == null -> "Informe uma validade no formato dd/mm/aaaa ou deixe em branco."
        else -> null
    }

    fun toEntity(): PantryItemEntity = PantryItemEntity(
        id = id,
        nome = nome.trim(),
        quantidade = checkNotNull(quantidadeValida),
        unidade = unidade,
        validade = parseDate(validade),
    )
}

data class PantryUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val items: List<PantryItemEntity> = emptyList(),
    val termoBusca: String = "",
    val somenteValidadeProxima: Boolean = false,
    val formState: PantryFormState? = null,
    val pendingDelete: PantryItemEntity? = null,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null,
) {
    val visibleItems: List<PantryItemEntity>
        get() {
            val limite = LocalDate.now().toEpochDay() + 7
            return items.filter {
                it.nome.contains(termoBusca.trim(), ignoreCase = true) &&
                    (!somenteValidadeProxima || it.validade?.let { dia -> dia <= limite } == true)
            }
        }
}
