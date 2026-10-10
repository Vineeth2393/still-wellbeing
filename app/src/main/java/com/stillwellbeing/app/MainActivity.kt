package com.stillwellbeing.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.stillwellbeing.app.data.settings.UserPreferences
import com.stillwellbeing.app.ui.navigation.StillNavHost
import com.stillwellbeing.app.ui.theme.LocalStillPalette
import com.stillwellbeing.app.ui.theme.StillTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as StillApplication
        setContent {
            val prefs by app.settingsRepository.preferences.collectAsState(initial = UserPreferences())

            StillTheme(themeChoice = prefs.themeChoice) {
                Surface(modifier = Modifier.fillMaxSize(), color = LocalStillPalette.current.paper) {
                    StillNavHost()
                }
            }
        }
    }
}
