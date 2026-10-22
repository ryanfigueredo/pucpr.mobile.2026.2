package br.pucpr.cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import br.pucpr.cookbook.data.seed.DatabaseSeeder
import br.pucpr.cookbook.ui.navigation.CookBookNavHost
import br.pucpr.cookbook.ui.theme.CookBookTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

// @AndroidEntryPoint habilita a injeção de campos (databaseSeeder) e hiltViewModel()
// dentro das telas do NavHost abaixo.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch { databaseSeeder.seedIfNeeded() }
        setContent {
            CookBookTheme {
                CookBookNavHost()
            }
        }
    }
}
