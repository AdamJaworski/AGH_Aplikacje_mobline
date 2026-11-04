package com.example.apkmoblineprojekt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.apkmoblineprojekt.ui.navigation.GeoPlanerApp
import com.example.apkmoblineprojekt.ui.theme.GeoPlanerTheme
// Jeśli IDE podświetli GeoPlanerApp na czerwono, kliknij na to i wciśnij Alt+Enter, żeby zaimportować
// import com.example.apkmoblineprojekt.ui.GeoPlanerApp 

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoPlanerTheme {
                // To jest wywołanie głównego pliku nawigacji, który spina wszystkie ekrany
                GeoPlanerApp()
            }
        }
    }
}