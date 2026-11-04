package com.example.apkmoblineprojekt.ui.places

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apkmoblineprojekt.data.MockData
import com.example.apkmoblineprojekt.data.PlaceType
import com.example.apkmoblineprojekt.data.SavedPlaceUi
import com.example.apkmoblineprojekt.ui.components.CircularIconActionButton
import com.example.apkmoblineprojekt.ui.components.MapPlaceMarkerPin
import com.example.apkmoblineprojekt.ui.components.ScreenTopHeader
import com.example.apkmoblineprojekt.ui.components.StylizedMapCard
import com.example.apkmoblineprojekt.ui.theme.*

@Composable
fun PlacesScreen(
    savedPlaces: List<SavedPlaceUi>,
    onAddPlaceClick: () -> Unit,
    onPlaceClick: (SavedPlaceUi) -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            ScreenTopHeader(
                overlineText = "TWOJA MAPA",
                title = "Miejsca",
                actionButton = {
                    CircularIconActionButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Dodaj miejsce",
                        onClick = onAddPlaceClick,
                        containerColor = ForestGreen,
                        contentColor = Color.White,
                        hasBorder = false
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mapa z pinezkami Dom i Praca
            StylizedMapCard(
                height = 200.dp,
                onMyLocationClick = onMyLocationClick
            ) {
                MapPlaceMarkerPin(
                    icon = Icons.Outlined.Home,
                    backgroundColor = ForestGreen,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 95.dp, top = 62.dp)
                )

                MapPlaceMarkerPin(
                    icon = Icons.Outlined.WorkOutline,
                    backgroundColor = AccentBlue,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 92.dp, bottom = 46.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Zapisane miejsca",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${savedPlaces.size} miejsca",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        items(savedPlaces, key = { it.id }) { place ->
            SavedPlaceCard(
                place = place,
                onClick = { onPlaceClick(place) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun SavedPlaceCard(
    place: SavedPlaceUi,
    onClick: () -> Unit
) {
    val (icon, iconTint, iconBg) = when (place.type) {
        PlaceType.HOME -> Triple(Icons.Outlined.Home, AccentCoral, AccentCoralSoft)
        PlaceType.WORK -> Triple(Icons.Outlined.WorkOutline, AccentBlue, AccentBlueSoft)
        PlaceType.OTHER -> Triple(Icons.Outlined.LocationOn, AccentGreenPin, ForestGreenLight)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Licznik zadań przypisanych do miejsca
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfacePillGray)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = place.taskCount.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextPlaceholder
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlacesScreenPreview() {
    GeoPlanerTheme {
        PlacesScreen(
            savedPlaces = MockData.savedPlaces,
            onAddPlaceClick = {},
            onPlaceClick = {},
            onMyLocationClick = {}
        )
    }
}