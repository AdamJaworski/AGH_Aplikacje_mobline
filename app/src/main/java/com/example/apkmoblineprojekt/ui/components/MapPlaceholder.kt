package com.example.apkmoblineprojekt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apkmoblineprojekt.ui.theme.BliskoColors
import com.example.apkmoblineprojekt.ui.theme.BliskoText

/** Droga na atrapie mapy: odcinek między punktami w ułamkach szerokości/wysokości. */
data class MapRoad(val start: Offset, val end: Offset, val width: Dp)

val MiniMapRoads = listOf(
    MapRoad(Offset(0.60f, -0.1f), Offset(0.30f, 1.1f), 18.dp),
    MapRoad(Offset(-0.05f, 0.58f), Offset(1.05f, 0.40f), 14.dp),
    MapRoad(Offset(0.78f, -0.1f), Offset(0.95f, 1.1f), 10.dp),
)

val PickerMapRoads = listOf(
    MapRoad(Offset(0.60f, -0.1f), Offset(0.30f, 1.1f), 18.dp),
    MapRoad(Offset(-0.05f, 0.58f), Offset(1.05f, 0.40f), 14.dp),
    MapRoad(Offset(0.66f, -0.1f), Offset(0.82f, 1.1f), 11.dp),
)

/**
 * Atrapa mapy z makiety (siatka + drogi). Do podmiany na osmdroid lub Google Maps Compose
 * (HLD §9) – zawartość [content] (pinezki, przyciski) zostaje nałożona na mapę.
 */
@Composable
fun MapPlaceholder(
    modifier: Modifier = Modifier,
    roads: List<MapRoad> = MiniMapRoads,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(BliskoColors.Map)
            .drawBehind {
                val step = 48.dp.toPx()
                var x = step / 2
                while (x < size.width) {
                    drawLine(BliskoColors.MapGrid, Offset(x, 0f), Offset(x, size.height), 1f)
                    x += step
                }
                var y = step / 2
                while (y < size.height) {
                    drawLine(BliskoColors.MapGrid, Offset(0f, y), Offset(size.width, y), 1f)
                    y += step
                }
                roads.forEach { road ->
                    val start = Offset(road.start.x * size.width, road.start.y * size.height)
                    val end = Offset(road.end.x * size.width, road.end.y * size.height)
                    val width = road.width.toPx()
                    drawLine(BliskoColors.MapRoadEdge, start, end, width + 2.dp.toPx(), StrokeCap.Butt)
                    drawLine(Color.White, start, end, width, StrokeCap.Butt)
                }
            },
        content = content,
    )
}

/** Pinezka w kształcie kropli (Dom, Praca na mini-mapie). */
@Composable
fun MapDropPin(icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(
        topStart = CornerSize(50),
        topEnd = CornerSize(50),
        bottomEnd = CornerSize(50),
        bottomStart = CornerSize(8.dp),
    )
    Box(
        modifier = modifier
            .rotate(-45f)
            .shadow(8.dp, shape, ambientColor = BliskoColors.Forest, spotColor = BliskoColors.Forest)
            .size(39.dp)
            .background(color, shape)
            .border(4.dp, Color.White, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, Modifier.size(16.dp).rotate(45f), tint = Color.White)
    }
}

@Composable
fun MapLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier
            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp),
        style = BliskoText.Small.copy(fontSize = 7.sp, color = Color(0xFF46514C)),
    )
}

@Composable
fun LocateButton(onClick: () -> Unit, contentDescription: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(13.dp)
    Box(
        modifier = modifier
            .shadow(6.dp, shape)
            .size(39.dp)
            .background(Color.White, shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Outlined.MyLocation, contentDescription, Modifier.size(19.dp), tint = BliskoColors.Forest)
    }
}
