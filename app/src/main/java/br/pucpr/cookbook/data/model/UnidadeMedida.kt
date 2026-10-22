package br.pucpr.cookbook.data.model

/**
 * Lista única de unidades de medida usada no formulário de receitas (RecipeFormSheet),
 * no seed (RecipeSeedData) e na lista de compras (ShoppingScreen). Evita variações como
 * "unidade" vs "un" ou "litro" vs "l" que impediriam a soma automática de ingredientes
 * repetidos em ShoppingRepository.addItem/addIngredientsFromRecipe.
 */
enum class UnidadeMedida(val label: String, val pluralLabel: String = label) {
    UNIDADE("un"),
    GRAMA("g"),
    QUILO("kg"),
    MILILITRO("ml"),
    LITRO("l"),
    XICARA("xícara", "xícaras"),
    COLHER_SOPA("colher (sopa)", "colheres (sopa)"),
    COLHER_CHA("colher (chá)", "colheres (chá)"),
    DENTE("dente", "dentes"),
    PITADA("pitada", "pitadas"),
    PACOTE("pct"),
    FOLHA("folha", "folhas"),
    LATA("lata", "latas"),
    RAMO("ramo", "ramos"),
    ;

    companion object {
        val default: UnidadeMedida = UNIDADE

        fun fromLabel(label: String): UnidadeMedida? = entries.firstOrNull { it.label == label }

        /** Rótulo singular ou plural adequado à quantidade informada (ex.: "dente"/"dentes"). */
        fun displayLabel(label: String, quantidade: Double): String {
            val unidade = fromLabel(label) ?: return label
            return if (quantidade == 1.0) unidade.label else unidade.pluralLabel
        }
    }
}
