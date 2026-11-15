package com.example.apkmoblineprojekt.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Makieta używa fontu Manrope. Aby go włączyć, dodaj pliki do `res/font/`
 * (np. manrope_regular.ttf … manrope_extrabold.ttf) i zbuduj z nich FontFamily tutaj.
 */
val BliskoFont: FontFamily = FontFamily.SansSerif

private val Base = TextStyle(fontFamily = BliskoFont, color = BliskoColors.Ink)

/** Style tekstu odpowiadające klasom z makiety. */
object BliskoText {
    val Eyebrow = Base.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.14.em)
    val ScreenTitle = Base.copy(
        fontSize = 27.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.045).em,
        lineHeight = 31.sp,
    )
    val SectionTitle = Base.copy(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.03).em)
    val SectionTitleSmall = SectionTitle.copy(fontSize = 17.sp)
    val SheetTitle = Base.copy(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    val CardTitle = Base.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em)
    val NoteTitle = Base.copy(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.02).em)
    val Body = Base.copy(fontSize = 11.sp, lineHeight = 18.sp, color = BliskoColors.BodyText)
    val Detail = Base.copy(fontSize = 10.5.sp, color = BliskoColors.Muted)
    val RowLabel = Base.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    val Value = Base.copy(fontSize = 9.5.sp, color = BliskoColors.Muted)
    val Meta = Base.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = BliskoColors.MetaText)
    val Small = Base.copy(fontSize = 9.sp, color = BliskoColors.Muted)
    val Caption = Base.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BliskoColors.Muted)
    val Button = Base.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
    val FieldLabel = Base.copy(
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.12.em,
        color = BliskoColors.Label,
    )
}

val BliskoTypography = Typography().run {
    copy(
        bodyLarge = bodyLarge.copy(fontFamily = BliskoFont),
        bodyMedium = bodyMedium.copy(fontFamily = BliskoFont),
        bodySmall = bodySmall.copy(fontFamily = BliskoFont),
        labelLarge = labelLarge.copy(fontFamily = BliskoFont),
        titleLarge = titleLarge.copy(fontFamily = BliskoFont),
    )
}
