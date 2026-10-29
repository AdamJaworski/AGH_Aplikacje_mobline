package com.example.apkmoblineprojekt.ui.placepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apkmoblineprojekt.data.MockData
import com.example.apkmoblineprojekt.data.NearbyPlaceUi
import com.example.apkmoblineprojekt.data.PlaceCategory
import com.example.apkmoblineprojekt.ui.components.AppSearchBar
import com.example.apkmoblineprojekt.ui.components.CircularIconActionButton
import com.example.apkmoblineprojekt.ui.components.StylizedMapCard
import com.example.apkmoblineprojekt.ui.theme.*

@Composable
fun PlacePickerScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: PlaceCategory,
    onCategorySelect: (PlaceCategory) -> Unit,
    nearbyPlaces: List<NearbyPlaceUi>,
    onCloseClick: () -> Unit,
    onDoneClick: () -> Unit,
    onNearbyPlaceClick: (NearbyPlaceUi) -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BorderLight)
                )
            }

            // Nagłówek: [X] | Wybierz miejsce | [Gotowe]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconActionButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Zamknij",
                    onClick = onCloseClick
                )

                Text(
                    text = "Wybierz miejsce",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
                )

                Button(
                    onClick = onDoneClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(
                        text = "Gotowe",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Adres lub nazwa miejsca"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mapa ze strefą Geofence (okrąg) i pinezką na środku
            StylizedMapCard(
                height = 220.dp,
                onMyLocationClick = onMyLocationClick
            ) {
                // Etykieta: Dom i ogród
                MapFloatingLabel(
                    text = "Dom i ogród",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 36.dp, top = 48.dp)
                )

                // Etykieta: Galeria Mokotów
                MapFloatingLabel(
                    text = "Galeria Mokotów",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 68.dp, bottom = 52.dp)
                )

                // Okrąg Geofence (150-200m) + centralny marker
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(AccentCoral.copy(alpha = 0.14f))
                        .border(1.dp, AccentCoral.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = SurfaceWhite,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = AccentCoral,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kafelki kategorii: Sklep | Apteka | Paczkomat | Inne
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PlaceCategory.entries.forEach { category ->
                    CategoryOptionTile(
                        category = category,
                        isSelected = category == selectedCategory,
                        onClick = { onCategorySelect(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "W pobliżu",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "6 wyników",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(nearbyPlaces, key = { it.index }) { place ->
            NearbyPlaceRowCard(
                place = place,
                onClick = { onNearbyPlaceClick(place) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun MapFloatingLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CategoryOptionTile(
    category: PlaceCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (category) {
        PlaceCategory.SHOP -> Icons.Outlined.Storefront
        PlaceCategory.PHARMACY -> Icons.Outlined.AddCircleOutline
        PlaceCategory.PARCEL -> Icons.Outlined.Label
        PlaceCategory.OTHER -> Icons.Outlined.LocationOn
    }

    val containerColor = if (isSelected) AccentCoralSoft else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) AccentCoral else MaterialTheme.colorScheme.outline
    val contentColor = if (isSelected) AccentCoral else TextPrimary

    Column(
        modifier = modifier
            .height(74.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
fun NearbyPlaceRowCard(
    place: NearbyPlaceUi,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ForestGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = place.index.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = place.openHours,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = place.distance,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlacePickerScreenPreview() {
    GeoPlanerTheme {
        PlacePickerScreen(
            searchQuery = "",
            onSearchQueryChange = {},
            selectedCategory = PlaceCategory.SHOP,
            onCategorySelect = {},
            nearbyPlaces = MockData.nearbyPlaces,
            onCloseClick = {},
            onDoneClick = {},
            onNearbyPlaceClick = {},
            onMyLocationClick = {}
        )
    }
}