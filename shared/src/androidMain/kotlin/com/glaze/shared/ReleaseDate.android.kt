package com.glaze.shared

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

internal actual fun releaseDaysUntil(date: String, now: Long): Long? = try {
    ChronoUnit.DAYS.between(Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.parse(date))
} catch (_: java.time.DateTimeException) { null }
