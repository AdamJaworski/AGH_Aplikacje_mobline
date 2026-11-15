package com.example.apkmoblineprojekt.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.example.apkmoblineprojekt.R
import com.example.apkmoblineprojekt.ui.theme.BliskoColors
import com.example.apkmoblineprojekt.ui.theme.BliskoText

/**
 * Prośba o uprawnienia potrzebne do przypomnień lokalizacyjnych, w momencie potrzeby (HLD §8):
 * 1. powiadomienia (Android 13+) i dokładna lokalizacja – systemowy dialog,
 * 2. lokalizacja w tle – najpierw nasz dialog z wyjaśnieniem, potem system (na Androidzie 11+
 *    otwiera ekran ustawień z opcją „Zezwalaj zawsze”).
 *
 * Zwraca funkcję uruchamiającą ten proces. [onFinished] wołane na końcu, niezależnie od decyzji.
 */
@Composable
fun rememberReminderPermissionRequest(onFinished: () -> Unit = {}): () -> Unit {
    val context = LocalContext.current
    val finished by rememberUpdatedState(onFinished)
    var showBackgroundRationale by remember { mutableStateOf(false) }

    val backgroundLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        finished()
    }
    val foregroundLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (needsBackgroundLocation(context)) showBackgroundRationale = true else finished()
    }

    if (showBackgroundRationale) {
        AlertDialog(
            onDismissRequest = {
                showBackgroundRationale = false
                finished()
            },
            containerColor = BliskoColors.Card,
            title = { Text(stringResource(R.string.background_location_title), style = BliskoText.RowLabel) },
            text = { Text(stringResource(R.string.background_location_body), style = BliskoText.Small) },
            confirmButton = {
                TextButton(onClick = {
                    showBackgroundRationale = false
                    backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }) { Text(stringResource(R.string.background_location_allow), color = BliskoColors.Forest) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackgroundRationale = false
                    finished()
                }) { Text(stringResource(R.string.background_location_skip), color = BliskoColors.Muted) }
            },
        )
    }

    return {
        val missing = foregroundPermissions().filterNot { isGranted(context, it) }
        when {
            missing.isNotEmpty() -> foregroundLauncher.launch(missing.toTypedArray())
            needsBackgroundLocation(context) -> showBackgroundRationale = true
            else -> finished()
        }
    }
}

private fun foregroundPermissions(): List<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
}

/** Lokalizacja w tle ma sens dopiero po zgodzie na dokładną lokalizację (Android 10+). */
private fun needsBackgroundLocation(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
        !isGranted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)

private fun isGranted(context: Context, permission: String) =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
