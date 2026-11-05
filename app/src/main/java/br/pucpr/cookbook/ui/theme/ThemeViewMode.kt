package br.pucpr.cookbook.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.cookbook.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val repository: ThemeRepository
) : ViewModel() {

    val darkMode: StateFlow<Boolean?> = repository.darkMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun onToggleTheme(currentIsDark: Boolean) {
        viewModelScope.launch { repository.setDarkMode(!currentIsDark) }
    }
}