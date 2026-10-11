package com.linote.editor

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val noteTime = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.ENGLISH)

fun formatNoteTime(epochMillis: Long, zone: ZoneId): String =
    noteTime.format(Instant.ofEpochMilli(epochMillis).atZone(zone))
