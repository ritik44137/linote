package com.linote.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NoteTest {
    @Test
    fun create_usesUuidV7AndSameTimestamps() {
        val now = 1_700_000_000_123L
        val note = Note.create(content = "hello", now = now)

        assertEquals(now, note.createdAt)
        assertEquals(now, note.modifiedAt)
        assertEquals(7, note.uuid.version())
        assertEquals(2, note.uuid.variant())
        assertEquals(now, note.uuid.mostSignificantBits ushr 16)
    }

    @Test
    fun uuidV7_differsAtTheSameMillisecond() {
        val now = 1_700_000_000_123L
        assertNotEquals(UuidV7.generate(now), UuidV7.generate(now))
    }

    @Test
    fun schema_primaryKeyIsUuid_andCreatedAtIsIndexed() {
        val schema = File("schemas/com.linote.data.LinoteDatabase/1.json").readText()
        val createTable = "CREATE TABLE IF NOT EXISTS `\${TABLE_NAME}` (`uuid` TEXT NOT NULL, " +
            "`createdAt` INTEGER NOT NULL, `modifiedAt` INTEGER NOT NULL, `content` TEXT NOT NULL, " +
            "`title` TEXT, `referencedNoteID` INTEGER, PRIMARY KEY(`uuid`))"
        assertTrue(schema.contains(createTable))
        assertTrue(
            schema.contains(
                "CREATE INDEX IF NOT EXISTS `index_Note_createdAt` ON `\${TABLE_NAME}` (`createdAt`)",
            ),
        )
    }
}
