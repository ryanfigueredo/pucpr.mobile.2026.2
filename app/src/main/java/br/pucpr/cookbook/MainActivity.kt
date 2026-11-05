package br.pucpr.cookbook

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import br.pucpr.cookbook.data.repository.AuthRepository
import br.pucpr.cookbook.data.seed.DatabaseSeeder
import br.pucpr.cookbook.ui.navigation.CookBookNavHost
import br.pucpr.cookbook.ui.navigation.Routes
import br.pucpr.cookbook.ui.theme.CookBookTheme
import br.pucpr.cookbook.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch { databaseSeeder.seedIfNeeded() }
        val startDestination = if (authRepository.isLoggedIn) Routes.HOME else Routes.LOGIN

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val savedDark by themeViewModel.darkMode.collectAsStateWithLifecycle()
            val isDark = savedDark ?: isSystemInDarkTheme()

            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT, Color.TRANSPARENT
                    ) { isDark },
                    navigationBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT, Color.TRANSPARENT
                    ) { isDark }
                )
                onDispose { }
            }

            CookBookTheme(darkTheme = isDark) {
                CookBookNavHost(
                    startDestination = startDestination,
                    isDark = isDark,
                    onToggleTheme = { themeViewModel.onToggleTheme(isDark) }
                )
            }
        }
    }
}