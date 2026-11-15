package com.example.apkmoblineprojekt.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.apkmoblineprojekt.AppContainer
import com.example.apkmoblineprojekt.BliskoApplication

/**
 * ViewModel z zależnościami z [AppContainer]. W bloku [create] dostępne jest
 * `createSavedStateHandle()` (np. argumenty nawigacji).
 */
@Composable
inline fun <reified VM : ViewModel> appViewModel(
    key: String? = null,
    crossinline create: CreationExtras.(AppContainer) -> VM,
): VM {
    val container = (LocalContext.current.applicationContext as BliskoApplication).container
    return viewModel(
        key = key,
        factory = viewModelFactory { initializer { create(container) } },
    )
}
