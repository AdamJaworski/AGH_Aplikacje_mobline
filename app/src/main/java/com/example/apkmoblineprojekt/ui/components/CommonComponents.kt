package com.example.apkmoblineprojekt.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apkmoblineprojekt.ui.theme.*

@Composable
fun ScreenTopHeader(
    overlineText: String,
    title: String,
    modifier: Modifier = Modifier,
    actionButton: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = overlineText.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = AccentCoral
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (actionButton != null) {
            actionButton()
        }
    }
}

@Composable
fun CircularIconActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    containerColor: Color = SurfaceWhite,
    contentColor: Color = TextPrimary,
    hasBorder: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(containerColor)
            .then(
                if (hasBorder) Modifier.border(1.dp, BorderLight, CircleShape) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextPlaceholder,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPlaceholder
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Komponent wizualizujący mapę zgodnie z projektem z Figmy.
 * W docelowym spięciu z osmdroid / Google Maps Compose można podmienić zawartość Boxa.
 */
@Composable
fun StylizedMapCard(
    height: Dp,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
    overlayContent: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(24.dp))
            .background(MapBackground)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
    ) {
        // Siatka i drogi z makiety Figmy
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 42.dp.toPx()
            for (x in 0..(size.width / step).toInt()) {
                drawLine(
                    color = MapGridLine,
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            for (y in 0..(size.height / step).toInt()) {
                drawLine(
                    color = MapGridLine,
                    start = Offset(0f, y * step),
                    end = Offset(size.width, y * step),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Główne białe arterie (drogi)
            drawLine(
                color = MapRoadWhite,
                start = Offset(0f, size.height * 0.65f),
                end = Offset(size.width, size.height * 0.42f),
                strokeWidth = 18.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = MapRoadWhite,
                start = Offset(size.width * 0.62f, 0f),
                end = Offset(size.width * 0.32f, size.height),
                strokeWidth = 20.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = MapRoadWhite,
                start = Offset(size.width * 0.66f, 0f),
                end = Offset(size.width * 0.78f, size.height),
                strokeWidth = 14.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Zawartość specyficzna dla ekranu (pinezki, strefa geofence, etykiety)
        overlayContent()

        // Przycisk "Moja lokalizacja" w prawym dolnym rogu
        Surface(
            onClick = onMyLocationClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp)
                .size(44.dp),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceWhite,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Moja lokalizacja",
                    tint = ForestGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MapPlaceMarkerPin(
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(42.dp),
        shape = CircleShape,
        color = SurfaceWhite,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}