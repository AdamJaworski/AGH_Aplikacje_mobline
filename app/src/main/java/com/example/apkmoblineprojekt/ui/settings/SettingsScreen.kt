package com.example.apkmoblineprojekt.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apkmoblineprojekt.ui.components.ScreenTopHeader
import com.example.apkmoblineprojekt.ui.theme.*

@Composable
fun SettingsScreen(
    isQuietHoursEnabled: Boolean,
    onQuietHoursChanged: (Boolean) -> Unit,
    onNotificationPermissionClick: () -> Unit,
    onLocationPermissionClick: () -> Unit,
    onDefaultRadiusClick: () -> Unit,
    onDefaultLeadTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        ScreenTopHeader(
            overlineText = "PREFERENCJE",
            title = "Ustawienia"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Baner statusu: WSZYSTKO GOTOWE - Przypomnienia działają
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = ForestGreen
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WSZYSTKO GOTOWE",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentCoral
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Przypomnienia działają",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Powiadomimy Cię we właściwym czasie i miejscu.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sekcja: Uprawnienia
        Text(
            text = "Uprawnienia",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column {
                SettingsRowItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Powiadomienia",
                    valueText = "Włączone",
                    valueColor = AccentGreenPin,
                    onClick = onNotificationPermissionClick
                )
                HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRowItem(
                    icon = Icons.Default.MyLocation,
                    title = "Lokalizacja",
                    valueText = "Zawsze",
                    valueColor = AccentGreenPin,
                    onClick = onLocationPermissionClick
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sekcja: Przypomnienia
        Text(
            text = "Przypomnienia",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column {
                // Cisza nocna ze Switchem
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ForestGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cisza nocna",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "22:00–07:00",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = isQuietHoursEnabled,
                        onCheckedChange = onQuietHoursChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ForestGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = TextPlaceholder
                        )
                    )
                }

                HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                SettingsRowItem(
                    icon = Icons.Outlined.NearMe,
                    title = "Domyślny promień",
                    valueText = "300 m",
                    valueColor = TextSecondary,
                    onClick = onDefaultRadiusClick
                )

                HorizontalDivider(color = BorderLight, modifier = Modifier.padding(horizontal = 16.dp))

                SettingsRowItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Domyślne wyprzedzenie",
                    valueText = "15 min",
                    valueColor = TextSecondary,
                    onClick = onDefaultLeadTimeClick
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Lokalizacja jest sprawdzana energooszczędnie. Dane o Twoim położeniu zostają na urządzeniu.",
            fontSize = 11.sp,
            color = TextPlaceholder,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    valueText: String,
    valueColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ForestGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ForestGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextPlaceholder,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    GeoPlanerTheme {
        SettingsScreen(
            isQuietHoursEnabled = true,
            onQuietHoursChanged = {},
            onNotificationPermissionClick = {},
            onLocationPermissionClick = {},
            onDefaultRadiusClick = {},
            onDefaultLeadTimeClick = {}
        )
    }
}