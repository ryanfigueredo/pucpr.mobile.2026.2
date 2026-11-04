package br.pucpr.cookbook.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.pucpr.cookbook.ui.screens.collections.CollectionsScreen
import br.pucpr.cookbook.ui.screens.cooking.CookingModeScreen
import br.pucpr.cookbook.ui.screens.details.DetailsScreen
import br.pucpr.cookbook.ui.screens.explore.ExploreScreen
import br.pucpr.cookbook.ui.screens.favorites.FavoritesScreen
import br.pucpr.cookbook.ui.screens.home.HomeScreen
import br.pucpr.cookbook.ui.screens.journal.JournalScreen
import br.pucpr.cookbook.ui.screens.login.LoginScreen
import br.pucpr.cookbook.ui.screens.pantry.PantryScreen
import br.pucpr.cookbook.ui.screens.planner.PlannerScreen
import br.pucpr.cookbook.ui.screens.shopping.ShoppingScreen

private data class BottomDestination(val route: String, val label: String, val icon: ImageVector)

private val bottomDestinations = listOf(
    BottomDestination(Routes.HOME, "Início", Icons.Filled.Home),
    BottomDestination(Routes.EXPLORE, "Explorar", Icons.Filled.Search),
    BottomDestination(Routes.FAVORITES, "Favoritos", Icons.Filled.Favorite),
    BottomDestination(Routes.SHOPPING, "Compras", Icons.Filled.ShoppingCart),
)

@Composable
fun CookBookNavHost(
    startDestination: String = Routes.LOGIN,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomDestinations.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            // LOGIN é a porta de entrada quando não há sessão. Ao autenticar,
            // substitui a si mesmo na backstack para o usuário não "voltar" ao login.
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onSearch = { termo -> navController.navigate(Routes.exploreWithTerm(termo)) },
                    onOpenRecipe = { id -> navController.navigate(Routes.details(id)) },
                    onOpenCategory = { categoria -> navController.navigate(Routes.exploreWithCategory(categoria.name)) },
                    onOpenPlanner = { navController.navigate(Routes.PLANNER) },
                    onOpenPantry = { navController.navigate(Routes.PANTRY_BASE) },
                    onOpenCollections = { navController.navigate(Routes.COLLECTIONS) },
                    onOpenJournal = { navController.navigate(Routes.JOURNAL_BASE) },
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            // EXPLORE declara os argumentos opcionais "termo" e "categoria": a rota é a
            // mesma usada pela barra inferior ("explore") e pela busca/categoria da Início.
            composable(
                route = Routes.EXPLORE,
                arguments = listOf(
                    navArgument("termo") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("categoria") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                ExploreScreen(onOpenRecipe = { id -> navController.navigate(Routes.details(id)) })
            }
            // DETAILS recebe recipeId obrigatório: o argumento é lido pelo DetailsViewModel
            // via SavedStateHandle.
            composable(
                route = Routes.DETAILS,
                arguments = listOf(navArgument("recipeId") { type = NavType.LongType }),
            ) {
                DetailsScreen(
                    onBack = { navController.popBackStack() },
                    onStartCooking = { recipeId, porcoes ->
                        // launchSingleTop evita empilhar duas telas iguais num toque duplo.
                        navController.navigate(Routes.cooking(recipeId, porcoes)) { launchSingleTop = true }
                    },
                )
            }
            // Modo Cozinha: recipeId obrigatório e porcoes opcional (0 = porções da receita),
            // ambos lidos pelo CookingModeViewModel via SavedStateHandle.
            composable(
                route = Routes.COOKING,
                arguments = listOf(
                    navArgument("recipeId") { type = NavType.LongType },
                    navArgument("porcoes") {
                        type = NavType.IntType
                        defaultValue = 0
                    },
                ),
            ) {
                CookingModeScreen(
                    onBack = { navController.popBackStack() },
                    onGoHome = { navController.popBackStack(Routes.HOME, inclusive = false) },
                    onRegister = { id, porcoes -> navController.navigate(Routes.journalFromCooking(id, porcoes)) },
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onOpenRecipe = { id -> navController.navigate(Routes.details(id)) },
                    onGoExplore = {
                        navController.navigate(Routes.EXPLORE_BASE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Routes.SHOPPING) {
                ShoppingScreen(onAddToPantry = { id -> navController.navigate(Routes.pantryFromShopping(id)) })
            }
            composable(Routes.COLLECTIONS) {
                CollectionsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenRecipe = { id -> navController.navigate(Routes.details(id)) },
                )
            }
            composable(
                route = Routes.JOURNAL,
                arguments = listOf(
                    navArgument("recipeId") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("porcoes") { type = NavType.IntType; defaultValue = 0 },
                ),
            ) {
                JournalScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.PLANNER) {
                PlannerScreen(
                    onBack = { navController.popBackStack() },
                    onOpenRecipe = { id -> navController.navigate(Routes.details(id)) },
                )
            }
            composable(
                route = Routes.PANTRY,
                arguments = listOf(navArgument("shoppingItemId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }),
            ) {
                PantryScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
