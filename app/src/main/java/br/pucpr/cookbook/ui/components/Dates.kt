package br.pucpr.cookbook.ui.components

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
    .withResolverStyle(ResolverStyle.STRICT)
val dayTitleFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale.forLanguageTag("pt-BR"))

fun formatDate(dia: Long): String = LocalDate.ofEpochDay(dia).format(dateFormatter)

fun parseDate(text: String): Long? = try {
    LocalDate.parse(text.trim(), dateFormatter).takeIf { it.year in 1..9999 }?.toEpochDay()
} catch (_: DateTimeParseException) {
    null
}

fun startOfWeek(dia: Long): Long = LocalDate.ofEpochDay(dia)
    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay()
