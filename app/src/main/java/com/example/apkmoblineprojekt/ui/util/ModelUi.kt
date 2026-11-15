package com.example.apkmoblineprojekt.ui.util

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalAtm
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocalPostOffice
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.apkmoblineprojekt.R
import com.example.apkmoblineprojekt.data.model.LocationType
import com.example.apkmoblineprojekt.data.model.PlaceCategory
import com.example.apkmoblineprojekt.data.model.PlaceKind
import com.example.apkmoblineprojekt.data.model.TaskColor
import com.example.apkmoblineprojekt.data.model.TaskLocation
import com.example.apkmoblineprojekt.ui.theme.BliskoColors

// Mapowanie modelu danych na etykiety, ikony i kolory UI.

@get:StringRes
val PlaceCategory.labelRes: Int
    get() = when (this) {
        PlaceCategory.GROCERY -> R.string.category_grocery
        PlaceCategory.PHARMACY -> R.string.category_pharmacy
        PlaceCategory.PARCEL_LOCKER -> R.string.category_parcel_locker
        PlaceCategory.HARDWARE_STORE -> R.string.category_hardware_store
        PlaceCategory.POST_OFFICE -> R.string.category_post_office
        PlaceCategory.ATM -> R.string.category_atm
    }

@get:StringRes
val PlaceCategory.shortLabelRes: Int
    get() = when (this) {
        PlaceCategory.GROCERY -> R.string.category_grocery_short
        PlaceCategory.HARDWARE_STORE -> R.string.category_hardware_store_short
        else -> labelRes
    }

val PlaceCategory.icon: ImageVector
    get() = when (this) {
        PlaceCategory.GROCERY -> Icons.Outlined.Storefront
        PlaceCategory.PHARMACY -> Icons.Outlined.AddCircleOutline
        PlaceCategory.PARCEL_LOCKER -> Icons.Outlined.LocalOffer
        PlaceCategory.HARDWARE_STORE -> Icons.Outlined.Handyman
        PlaceCategory.POST_OFFICE -> Icons.Outlined.LocalPostOffice
        PlaceCategory.ATM -> Icons.Outlined.LocalAtm
    }

@Composable
fun TaskLocation.displayName(): String =
    if (type == LocationType.CATEGORY && category != null) stringResource(category.labelRes) else name

val TaskColor.color: Color
    get() = when (this) {
        TaskColor.CORAL -> BliskoColors.Coral
        TaskColor.BLUE -> BliskoColors.Blue
        TaskColor.GREEN -> BliskoColors.Green
    }

val PlaceKind.color: Color
    get() = when (this) {
        PlaceKind.HOME -> BliskoColors.Coral
        PlaceKind.WORK -> BliskoColors.Blue
        PlaceKind.SAVED -> BliskoColors.Green
    }

val PlaceKind.icon: ImageVector
    get() = when (this) {
        PlaceKind.HOME -> Icons.Outlined.Home
        PlaceKind.WORK -> Icons.Outlined.BusinessCenter
        PlaceKind.SAVED -> Icons.Outlined.LocationOn
    }

/** Opcje wyprzedzenia przypomnienia z makiety (minuty). */
val ReminderOptions = listOf(0, 5, 15, 30, 60)

@Composable
fun reminderLabel(minutes: Int?): String = when {
    minutes == null -> stringResource(R.string.reminder_none)
    minutes == 0 -> stringResource(R.string.reminder_on_time)
    minutes % 60 == 0 -> stringResource(R.string.reminder_hours_before, minutes / 60)
    else -> stringResource(R.string.reminder_minutes_before, minutes)
}

@Composable
fun tasksCountLabel(count: Int): String =
    pluralPl(count, R.string.tasks_one, R.string.tasks_few, R.string.tasks_many)

/**
 * Polska odmiana liczebników (1 zadanie, 2 zadania, 5 zadań). Nie używamy `<plurals>`,
 * bo Android wybiera formę wg języka urządzenia, a aplikacja jest tylko po polsku.
 */
@Composable
fun pluralPl(count: Int, @StringRes one: Int, @StringRes few: Int, @StringRes many: Int): String {
    val res = when {
        count == 1 -> one
        count % 10 in 2..4 && count % 100 !in 12..14 -> few
        else -> many
    }
    return stringResource(res, count)
}
