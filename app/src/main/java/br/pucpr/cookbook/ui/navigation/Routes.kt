package br.pucpr.cookbook.ui.navigation

/**
 * Nomes de rota e builders de destino do NavHost. [EXPLORE] e [DETAILS] levam
 * argumentos (termo de busca / categoria opcionais e recipeId) lidos pelo
 * ViewModel de cada tela via SavedStateHandle.
 */
object Routes {
    const val HOME = "home"
    const val EXPLORE_BASE = "explore"
    const val EXPLORE = "$EXPLORE_BASE?termo={termo}&categoria={categoria}"
    const val DETAILS = "details/{recipeId}"
    const val FAVORITES = "favorites"
    const val SHOPPING = "shopping"

    fun exploreWithTerm(termo: String): String = "$EXPLORE_BASE?termo=$termo"
    fun exploreWithCategory(categoria: String): String = "$EXPLORE_BASE?categoria=$categoria"
    fun details(recipeId: Long): String = "details/$recipeId"
}
