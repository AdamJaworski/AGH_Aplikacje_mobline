package com.example.apkmoblineprojekt

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.apkmoblineprojekt.ui.BliskoApp
import com.example.apkmoblineprojekt.ui.theme.BliskoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            BliskoTheme {
                BliskoApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Uprawnienia do kalendarza mogły się zmienić w ustawieniach systemu.
        (application as BliskoApplication).container.deviceCalendar.refresh()
    }
}
