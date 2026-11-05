package com.example.apkmoblineprojekt.ui.placepicker


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.apkmoblineprojekt.ui.util.pluralPl
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apkmoblineprojekt.R
import com.example.apkmoblineprojekt.data.model.LocationType
import com.example.apkmoblineprojekt.data.model.NearbyPlace
import com.example.apkmoblineprojekt.data.model.PlaceCategory
import com.example.apkmoblineprojekt.data.model.TaskLocation
import com.example.apkmoblineprojekt.ui.components.BliskoCard
import com.example.apkmoblineprojekt.ui.components.CircleIconButton
import com.example.apkmoblineprojekt.ui.components.LocateButton
import com.example.apkmoblineprojekt.ui.components.MapLabel
import com.example.apkmoblineprojekt.ui.components.MapPlaceholder
import com.example.apkmoblineprojekt.ui.components.PickerMapRoads
import com.example.apkmoblineprojekt.ui.components.SearchField
import com.example.apkmoblineprojekt.ui.components.SectionHeading
import com.example.apkmoblineprojekt.ui.components.SheetHeader
import com.example.apkmoblineprojekt.ui.components.SheetPrimaryButton
import com.example.apkmoblineprojekt.ui.theme.BliskoColors
import com.example.apkmoblineprojekt.ui.theme.BliskoText
import com.example.apkmoblineprojekt.ui.util.appViewModel
import com.example.apkmoblineprojekt.ui.util.formatDistance
import com.example.apkmoblineprojekt.ui.util.hhmm
import com.example.apkmoblineprojekt.ui.util.icon
import com.example.apkmoblineprojekt.ui.util.shortLabelRes
import kotlinx.coroutines.launch

private val QuickCategories = listOf(PlaceCategory.GROCERY, PlaceCategory.PHARMACY, PlaceCategory.PARCEL_LOCKER)
private val OtherCategories = PlaceCategory.entries - QuickCategories.toSet()

/** Arkusz „Wybierz miejsce”: wyszukiwarka, mapa, kategorie, lista „W pobliżu”. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacePickerSheet(
    initial: TaskLocation?,
    onDismiss: () -> Unit,
    onConfirm: (TaskLocation) -> Unit,
) {
    val viewModel = appViewModel(key = "placePicker") {
        PlacePickerViewModel(it.placeSearchRepository, it.settingsRepository)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.start(initial) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun hideThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = BliskoColors.Surface,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 9.dp, bottom = 3.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .background(Color(0xFFC9CCC8), RoundedCornerShape(5.dp)),
            )
        },
    ) {
        PlacePickerContent(
            state = state,
            onClose = { hideThen(onDismiss) },
            onDone = { state.selection?.let { selection -> hideThen { onConfirm(selection) } } },
            onQueryChange = viewModel::onQueryChange,
            onSelectCategory = viewModel::selectCategory,
            onSelectNearby = viewModel::selectNearby,
            onLocateMe = viewModel::locateMe,
        )
    }
}

@Composable
fun PlacePickerContent(
    state: PlacePickerUiState,
    onClose: () -> Unit,
    onDone: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSelectCategory: (PlaceCategory) -> Unit,
    onSelectNearby: (NearbyPlace) -> Unit,
    onLocateMe: () -> Unit,
) {
    Column(Modifier.fillMaxHeight()) {
        SheetHeader(
            title = stringResource(R.string.picker_title),
            leading = {
                CircleIconButton(
                    Icons.Outlined.Close,
                    stringResource(R.string.action_close),
                    onClose,
                    size = 33.dp,
                    iconSize = 20.dp,
                )
            },
            trailing = {
                SheetPrimaryButton(stringResource(R.string.action_done), onDone, enabled = state.selection != null)
            },
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 19.dp, end = 19.dp, top = 18.dp, bottom = 42.dp),
        ) {
            SearchField(state.query, onQueryChange, stringResource(R.string.picker_search_placeholder))
            PickerMap(state, onLocateMe)
            CategoryRow(state, onSelectCategory)
            SectionHeading(
                title = stringResource(R.string.picker_nearby),
                trailing = if (state.searching) {
                    stringResource(R.string.picker_searching)
                } else {
                    pluralPl(state.results.size, R.string.results_one, R.string.results_few, R.string.results_many)
                },
                modifier = Modifier.padding(bottom = 10.dp),
            )
            if (state.results.isEmpty() && !state.searching) {
                Text(
                    stringResource(R.string.picker_empty),
                    Modifier.padding(horizontal = 2.dp),
                    style = BliskoText.Small,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                state.results.forEachIndexed { index, place ->
                    val selected = state.selection?.takeIf { it.type == LocationType.POINT }?.name == place.name
                    NearbyRow(index + 1, place, selected) { onSelectNearby(place) }
                }
            }
        }
    }
}

@Composable
private fun PickerMap(state: PlacePickerUiState, onLocateMe: () -> Unit) {
    MapPlaceholder(
        Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .height(234.dp),
        roads = PickerMapRoads,
    ) {
        // Okrąg ~112 dp odpowiada 300 m, jak w makiecie.
        val circle = (112f * state.radiusMeters / 300f).coerceIn(60f, 200f).dp
        Box(
            Modifier
                .align(Alignment.Center)
                .size(circle)
                .background(BliskoColors.Coral.copy(alpha = 0.09f), CircleShape)
                .border(1.dp, BliskoColors.Coral.copy(alpha = 0.42f), CircleShape),
        )
        Box(
            Modifier
                .align(Alignment.Center)
                .shadow(8.dp, CircleShape)
                .size(40.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.LocationOn, null, Modifier.size(22.dp), tint = BliskoColors.Coral)
        }
        state.results.getOrNull(0)?.let { MapLabel(it.name, Modifier.align(BiasAlignment(-0.75f, -0.55f))) }
        state.results.getOrNull(1)?.let { MapLabel(it.name, Modifier.align(BiasAlignment(0.8f, 0.5f))) }
        LocateButton(
            onLocateMe,
            stringResource(R.string.cd_locate_me),
            Modifier
                .align(Alignment.BottomEnd)
                .padding(13.dp),
        )
    }
}

@Composable
private fun CategoryRow(state: PlacePickerUiState, onSelect: (PlaceCategory) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 13.dp, bottom = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        QuickCategories.forEach { category ->
            CategoryTile(
                icon = category.icon,
                label = stringResource(category.shortLabelRes),
                selected = state.category == category,
                onClick = { onSelect(category) },
                modifier = Modifier.weight(1f),
            )
        }
        var otherMenu by remember { mutableStateOf(false) }
        val other = state.category?.takeIf { it in OtherCategories }
        Box(Modifier.weight(1f)) {
            CategoryTile(
                icon = other?.icon ?: Icons.Outlined.LocationOn,
                label = other?.let { stringResource(it.shortLabelRes) } ?: stringResource(R.string.category_other),
                selected = other != null,
                onClick = { otherMenu = true },
                modifier = Modifier.fillMaxWidth(),
            )
            DropdownMenu(
                expanded = otherMenu,
                onDismissRequest = { otherMenu = false },
                containerColor = BliskoColors.Card,
            ) {
                OtherCategories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(stringResource(category.shortLabelRes)) },
                        leadingIcon = { Icon(category.icon, null) },
                        onClick = {
                            onSelect(category)
                            otherMenu = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (selected) BliskoColors.Coral else Color(0xFF656E69)
    BliskoCard(
        modifier = modifier
            .height(65.dp)
            .semantics { this.selected = selected },
        cornerRadius = 15.dp,
        color = if (selected) Color(0xFFFFF5F0) else BliskoColors.Card,
        borderColor = if (selected) BliskoColors.Coral else BliskoColors.Line,
        onClick = onClick,
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, null, Modifier.size(19.dp), tint = color)
            Text(
                label,
                style = BliskoText.Button.copy(fontSize = 8.5.sp, color = color),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun NearbyRow(number: Int, place: NearbyPlace, selected: Boolean, onClick: () -> Unit) {
    BliskoCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { this.selected = selected },
        cornerRadius = 15.dp,
        color = if (selected) Color(0xFFFFFAF7) else BliskoColors.Card,
        borderColor = if (selected) Color(0xFFEAA38E) else BliskoColors.Line,
        onClick = onClick,
    ) {
        Row(
            Modifier
                .heightIn(min = 59.dp)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(29.dp)
                    .background(BliskoColors.Forest, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(number.toString(), style = BliskoText.Eyebrow.copy(color = Color.White, letterSpacing = 0.sp))
            }
            Column(Modifier.weight(1f)) {
                Text(place.name, style = BliskoText.RowLabel)
                val hours = when {
                    place.alwaysOpen -> stringResource(R.string.picker_open_always)
                    place.openUntil != null -> stringResource(R.string.picker_open_until, place.openUntil.hhmm())
                    else -> null
                }
                Text(
                    hours ?: stringResource(R.string.picker_hours_unknown),
                    Modifier.padding(top = 3.dp),
                    style = BliskoText.Small.copy(
                        fontSize = 8.sp,
                        color = if (hours != null) Color(0xFF5F896E) else BliskoColors.Muted,
                    ),
                )
            }
            Text(formatDistance(place.distanceMeters), style = BliskoText.Small)
            if (selected) Icon(Icons.Outlined.Check, null, Modifier.size(18.dp), tint = BliskoColors.Coral)
        }
    }
}
