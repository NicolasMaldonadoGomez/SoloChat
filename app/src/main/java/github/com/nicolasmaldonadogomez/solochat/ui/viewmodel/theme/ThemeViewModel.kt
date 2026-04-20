package github.com.nicolasmaldonadogomez.solochat.ui.viewmodel.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.com.nicolasmaldonadogomez.solochat.data.preferences.ThemePreferences
import github.com.nicolasmaldonadogomez.solochat.ui.theme.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val themePreferences: ThemePreferences) : ViewModel() {

    val themeState: StateFlow<AppTheme> = themePreferences.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    fun changeTheme(newTheme: AppTheme) {
        viewModelScope.launch {
            themePreferences.saveTheme(newTheme)
        }
    }
}
