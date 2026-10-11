package com.linote.editor

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class NoteTimeTest {
    @Test fun fixedZone_formatsEnglishAmPm() {
        fun at(hour: Int, minute: Int) =
            ZonedDateTime.of(2024, 1, 1, hour, minute, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals("Jan 1, 2024 5:04 PM", formatNoteTime(at(17, 4), ZoneOffset.UTC))
        assertEquals("Jan 1, 2024 12:00 AM", formatNoteTime(at(0, 0), ZoneOffset.UTC))
        assertEquals("Jan 1, 2024 12:00 PM", formatNoteTime(at(12, 0), ZoneOffset.UTC))
    }
}
