package com.example.apkmoblineprojekt.ui.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.util.Locale

/** Polskie nazwy dat – bez zależności od danych ICU urządzenia (mianownik vs dopełniacz). */
object PolishDates {
    val locale: Locale = Locale.forLanguageTag("pl-PL")

    private val monthsNominative = listOf(
        "styczeń", "luty", "marzec", "kwiecień", "maj", "czerwiec",
        "lipiec", "sierpień", "wrzesień", "październik", "listopad", "grudzień",
    )
    private val monthsGenitive = listOf(
        "stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca",
        "lipca", "sierpnia", "września", "października", "listopada", "grudnia",
    )
    private val monthsShort = listOf(
        "sty", "lut", "mar", "kwi", "maj", "cze", "lip", "sie", "wrz", "paź", "lis", "gru",
    )
    private val weekdays = listOf(
        "poniedziałek", "wtorek", "środa", "czwartek", "piątek", "sobota", "niedziela",
    )

    /** Nagłówki tygodnia, od poniedziałku. */
    val weekdayShort = listOf("PON", "WT", "ŚR", "CZW", "PT", "SOB", "ND")
    val weekdayInitials = listOf("P", "W", "Ś", "C", "P", "S", "N")

    fun monthNominative(month: Int) = monthsNominative[month - 1]
    fun monthGenitive(month: Int) = monthsGenitive[month - 1]
    fun monthShort(month: Int) = monthsShort[month - 1]
    fun weekday(date: LocalDate) = weekdays[date.dayOfWeek.value - 1]
}

fun String.capitalized(): String = replaceFirstChar { it.titlecase(PolishDates.locale) }

/** „Środa, 21 maja” */
fun LocalDate.longTitle(): String =
    "${PolishDates.weekday(this).capitalized()}, ${dayMonth()}"

/** „21 maja”, z rokiem gdy inny niż bieżący. */
fun LocalDate.dayMonth(currentYear: Int = LocalDate.now().year): String {
    val base = "$dayOfMonth ${PolishDates.monthGenitive(monthValue)}"
    return if (year == currentYear) base else "$base $year"
}

/** „Październik 2026” */
fun YearMonth.title(): String = "${PolishDates.monthNominative(monthValue).capitalized()} $year"

/** „19–25 maja” albo „27 paź – 2 lis” */
fun weekRangeLabel(weekStart: LocalDate): String {
    val end = weekStart.plusDays(6)
    return if (weekStart.month == end.month) {
        "${weekStart.dayOfMonth}–${end.dayOfMonth} ${PolishDates.monthGenitive(end.monthValue)}"
    } else {
        "${weekStart.dayOfMonth} ${PolishDates.monthShort(weekStart.monthValue)} – " +
            "${end.dayOfMonth} ${PolishDates.monthShort(end.monthValue)}"
    }
}

fun LocalTime.hhmm(): String = "%02d:%02d".format(hour, minute)

fun LocalDate.startOfWeek(): LocalDate = minusDays(dayOfWeek.value - 1L)

/** „220 m”, „1,2 km” */
fun formatDistance(meters: Int): String =
    if (meters < 1000) "$meters m" else String.format(PolishDates.locale, "%.1f km", meters / 1000.0)
