package br.pucpr.cookbook.ui.navigation

/**
 * Nomes de rota e builders de destino do NavHost. [EXPLORE] e [DETAILS] levam
 * argumentos (termo de busca / categoria opcionais e recipeId) lidos pelo
 * ViewModel de cada tela via SavedStateHandle.
 */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val EXPLORE_BASE = "explore"
    const val EXPLORE = "$EXPLORE_BASE?termo={termo}&categoria={categoria}"

    fun explore(termo: String = "", categoria: String = "") =
        "$EXPLORE_BASE?termo=$termo&categoria=$categoria"
    const val DETAILS = "details/{recipeId}"
    const val FAVORITES = "favorites"
    const val SHOPPING = "shopping"
    const val PLANNER = "planner"
    const val COLLECTIONS = "collections"
    const val JOURNAL_BASE = "journal"
    const val JOURNAL = "$JOURNAL_BASE?recipeId={recipeId}&porcoes={porcoes}"
    const val PANTRY_BASE = "pantry"
    const val PANTRY = "$PANTRY_BASE?shoppingItemId={shoppingItemId}"
    const val COOKING_BASE = "cooking"
    const val COOKING = "$COOKING_BASE/{recipeId}?porcoes={porcoes}"

    fun journalFromCooking(recipeId: Long, porcoes: Int): String = "$JOURNAL_BASE?recipeId=$recipeId&porcoes=$porcoes"

    fun pantryFromShopping(itemId: Long): String = "$PANTRY_BASE?shoppingItemId=$itemId"

    fun exploreWithTerm(termo: String): String = "$EXPLORE_BASE?termo=$termo"
    fun exploreWithCategory(categoria: String): String = "$EXPLORE_BASE?categoria=$categoria"
    fun details(recipeId: Long): String = "details/$recipeId"

    /** Modo Cozinha: porcoes = 0 significa "usar as porções da própria receita". */
    fun cooking(recipeId: Long, porcoes: Int = 0): String = "$COOKING_BASE/$recipeId?porcoes=$porcoes"
}
