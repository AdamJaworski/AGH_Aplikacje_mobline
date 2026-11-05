package com.example.apkmoblineprojekt.ui.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.example.apkmoblineprojekt.ui.util.pluralPl
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apkmoblineprojekt.R
import com.example.apkmoblineprojekt.data.SampleData
import com.example.apkmoblineprojekt.data.model.LocationType
import com.example.apkmoblineprojekt.data.model.Place
import com.example.apkmoblineprojekt.data.model.PlaceKind
import com.example.apkmoblineprojekt.ui.components.BliskoCard
import com.example.apkmoblineprojekt.ui.components.ChevronRight
import com.example.apkmoblineprojekt.ui.components.CircleIconButton
import com.example.apkmoblineprojekt.ui.components.IconTile
import com.example.apkmoblineprojekt.ui.components.LocateButton
import com.example.apkmoblineprojekt.ui.components.MapDropPin
import com.example.apkmoblineprojekt.ui.components.MapPlaceholder
import com.example.apkmoblineprojekt.ui.components.ScreenHeader
import com.example.apkmoblineprojekt.ui.components.SectionHeading
import com.example.apkmoblineprojekt.ui.components.dashedBorder
import com.example.apkmoblineprojekt.ui.placepicker.PlacePickerSheet
import com.example.apkmoblineprojekt.ui.theme.BliskoColors
import com.example.apkmoblineprojekt.ui.theme.BliskoText
import com.example.apkmoblineprojekt.ui.theme.BliskoTheme
import com.example.apkmoblineprojekt.ui.util.appViewModel
import com.example.apkmoblineprojekt.ui.util.color
import com.example.apkmoblineprojekt.ui.util.icon
import com.example.apkmoblineprojekt.ui.util.labelRes

/** [onOpenPlace] – szczegóły/edycja miejsca, do podpięcia (np. ustawienie DOM/PRACA na mapie). */
@Composable
fun PlacesRoute(onOpenPlace: (Place) -> Unit) {
    val viewModel = appViewModel { PlacesViewModel(it.placeRepository, it.taskRepository) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pickerOpen by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    PlacesScreen(
        state = state,
        onAddPlace = { pickerOpen = true },
        onOpenPlace = onOpenPlace,
        onLocateMe = { /* TODO: bieżąca lokalizacja na mapie */ },
    )

    if (pickerOpen) {
        PlacePickerSheet(
            initial = null,
            onDismiss = { pickerOpen = false },
            onConfirm = { location ->
                val category = location.category
                val name = if (location.type == LocationType.CATEGORY && category != null) {
                    context.getString(category.labelRes)
                } else {
                    location.name
                }
                viewModel.addPlace(name)
                pickerOpen = false
            },
        )
    }
}

@Composable
fun PlacesScreen(
    state: PlacesUiState,
    onAddPlace: () -> Unit,
    onOpenPlace: (Place) -> Unit,
    onLocateMe: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 19.dp, end = 19.dp, top = 25.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        item {
            ScreenHeader(
                eyebrow = stringResource(R.string.places_eyebrow),
                title = stringResource(R.string.places_title),
            ) {
                CircleIconButton(Icons.Outlined.Add, stringResource(R.string.cd_add_place), onAddPlace, filled = true)
            }
        }
        item {
            MapPlaceholder(
                Modifier
                    .fillMaxWidth()
                    .height(206.dp),
            ) {
                MapDropPin(PlaceKind.HOME.icon, BliskoColors.Forest, Modifier.align(BiasAlignment(-0.4f, -0.35f)))
                MapDropPin(PlaceKind.WORK.icon, BliskoColors.Blue, Modifier.align(BiasAlignment(0.45f, 0.4f)))
                LocateButton(
                    onLocateMe,
                    stringResource(R.string.cd_locate_me),
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(13.dp),
                )
            }
        }
        item {
            SectionHeading(
                title = stringResource(R.string.places_saved),
                trailing = pluralPl(state.places.size, R.string.places_one, R.string.places_few, R.string.places_many),
                modifier = Modifier.padding(top = 16.dp, bottom = 3.dp),
            )
        }
        items(state.places, key = { it.place.id }) { item ->
            PlaceCard(item, onClick = { onOpenPlace(item.place) })
        }
        item {
            AddPlaceButton(onAddPlace, Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun PlaceCard(item: SavedPlaceItem, onClick: () -> Unit) {
    val kind = item.place.kind
    BliskoCard(Modifier.fillMaxWidth(), cornerRadius = 17.dp, onClick = onClick) {
        Row(
            Modifier
                .heightIn(min = 74.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            IconTile(
                kind.icon,
                background = kind.color.copy(alpha = 0.094f),
                tint = kind.color,
                size = 43.dp,
                cornerRadius = 14.dp,
                iconSize = 21.dp,
            )
            Column(Modifier.weight(1f)) {
                Text(item.place.name, style = BliskoText.CardTitle)
                Text(
                    item.place.address.ifBlank { stringResource(R.string.places_no_address) },
                    Modifier.padding(top = 4.dp),
                    style = BliskoText.Detail,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                Modifier
                    .widthIn(min = 25.dp)
                    .height(25.dp)
                    .background(BliskoColors.Badge, RoundedCornerShape(9.dp))
                    .padding(horizontal = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    item.openTaskCount.toString(),
                    style = BliskoText.Eyebrow.copy(color = BliskoColors.Forest, letterSpacing = 0.sp),
                )
            }
            ChevronRight()
        }
    }
}


@Composable
private fun AddPlaceButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .fillMaxWidth()
            .height(47.dp)
            .clip(shape)
            .dashedBorder(BliskoColors.Dashed, 16.dp)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Outlined.LocationOn, null, Modifier.size(18.dp), tint = BliskoColors.Forest)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.places_add_new), style = BliskoText.Button.copy(color = BliskoColors.Forest))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F6F1, widthDp = 412, heightDp = 760)
@Composable
private fun PlacesScreenPreview() {
    BliskoTheme {
        PlacesScreen(
            PlacesUiState(SampleData.places().mapIndexed { i, place -> SavedPlaceItem(place, 2 - i) }),
            {}, {}, {},
        )
    }
}
