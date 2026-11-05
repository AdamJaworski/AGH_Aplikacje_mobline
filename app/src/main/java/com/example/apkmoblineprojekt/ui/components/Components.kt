package com.example.apkmoblineprojekt.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apkmoblineprojekt.ui.theme.BliskoColors
import com.example.apkmoblineprojekt.ui.theme.BliskoText

/** Nagłówek ekranu: mały napis nad tytułem i opcjonalny przycisk po prawej. */
@Composable
fun ScreenHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Eyebrow(eyebrow)
            Text(title, style = BliskoText.ScreenTitle)
        }
        action?.invoke()
    }
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = BliskoColors.Coral) {
    Text(text, modifier.padding(bottom = 4.dp), style = BliskoText.Eyebrow.copy(color = color))
}

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier.padding(start = 3.dp, top = 19.dp, bottom = 8.dp), style = BliskoText.FieldLabel)
}

/** Okrągły przycisk z ikoną; `filled` – wariant „+” (ciemnozielony). */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    size: Dp = 42.dp,
    iconSize: Dp = 22.dp,
) {
    val background = if (filled) BliskoColors.Forest else BliskoColors.Card
    val border = if (filled) BliskoColors.Forest else BliskoColors.Line
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, border, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription,
            Modifier.size(iconSize),
            tint = if (filled) Color.White else BliskoColors.Ink,
        )
    }
}

/** Biała karta z cienką ramką – podstawowy kontener w makiecie. */
@Composable
fun BliskoCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    color: Color = BliskoColors.Card,
    borderColor: Color = BliskoColors.Line,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val border = BorderStroke(1.dp, borderColor)
    if (onClick != null) {
        Surface(onClick, modifier, shape = shape, color = color, border = border, content = content)
    } else {
        Surface(modifier, shape = shape, color = color, border = border, content = content)
    }
}

@Composable
fun SectionHeading(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    style: TextStyle = BliskoText.SectionTitleSmall,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(title, style = style)
        if (trailing != null) Text(trailing, style = BliskoText.Caption)
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(15.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(shape)
            .background(BliskoColors.Card)
            .border(1.dp, BliskoColors.Line, shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Outlined.Search, null, Modifier.size(19.dp), tint = Color(0xFF929894))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 12.sp, color = BliskoColors.Ink),
            cursorBrush = SolidColor(BliskoColors.Forest),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, style = TextStyle(fontSize = 12.sp, color = Color(0xFF929894)))
                }
                inner()
            },
        )
    }
}

/** Filtr w kształcie pastylki (Notatki: Wszystkie / Przypięte / Powiązane). */
@Composable
fun PillChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = modifier
            .height(31.dp)
            .clip(shape)
            .background(if (selected) BliskoColors.Forest else BliskoColors.Card)
            .border(1.dp, if (selected) BliskoColors.Forest else BliskoColors.Line, shape)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = BliskoText.Button.copy(
                fontSize = 10.sp,
                color = if (selected) Color.White else BliskoColors.Muted,
            ),
        )
    }
}

/** Ikona w zaokrąglonym kwadracie (wiersze ustawień, edytora, kart miejsc). */
@Composable
fun IconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    background: Color = BliskoColors.IconTint,
    tint: Color = BliskoColors.Forest,
    size: Dp = 34.dp,
    cornerRadius: Dp = 11.dp,
    iconSize: Dp = 19.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, Modifier.size(iconSize), tint = tint)
    }
}

@Composable
fun ChevronRight(modifier: Modifier = Modifier, tint: Color = BliskoColors.Chevron) {
    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, modifier.size(18.dp), tint = tint)
}

/** Przełącznik w stylu makiety (40×24). */
@Composable
fun BliskoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val thumbOffset by animateDpAsState(if (checked) 16.dp else 0.dp, label = "thumb")
    Box(
        modifier = modifier
            .size(width = 40.dp, height = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (checked) BliskoColors.Forest else Color(0xFFCFD3D0))
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(3.dp)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Box(
            Modifier
                .offset(x = thumbOffset)
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

/** Mały ciemny przycisk w nagłówku arkusza („Zapisz”, „Gotowe”). */
@Composable
fun SheetPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(10.dp))
            .background(BliskoColors.Forest)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text, style = BliskoText.Button.copy(color = Color.White))
    }
}

/** Nagłówek arkusza: akcja po lewej, tytuł na środku, akcja po prawej. */
@Composable
fun SheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    leading: @Composable RowScope.() -> Unit,
    trailing: @Composable RowScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 18.dp),
        ) {
            Row(Modifier.align(Alignment.CenterStart), content = leading)
            Text(title, Modifier.align(Alignment.Center), style = BliskoText.SheetTitle)
            Row(Modifier.align(Alignment.CenterEnd), content = trailing)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BliskoColors.Line),
        )
    }
}

fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.dp,
    dash: Dp = 4.dp,
    gap: Dp = 3.dp,
): Modifier = drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash.toPx(), gap.toPx())),
    )
    val inset = stroke.width / 2
    drawRoundRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - stroke.width, size.height - stroke.width),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = stroke,
    )
}
