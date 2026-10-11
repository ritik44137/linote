package com.linote.debug

import com.linote.editor.Piece
import com.linote.editor.readContent
import com.linote.editor.writePieces
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class DebugNoteSeedTest {
    @Test fun notes_areOneThousandWithUniqueKeys() {
        val notes = DebugNoteSeed.notes()

        assertEquals(1000, notes.size)
        assertEquals(1000, notes.map { it.uuid }.toSet().size)
        assertTrue(notes.all { it.uuid.version() == 7 && it.uuid.variant() == 2 })
        assertTrue(notes.all { it.createdAt == it.uuid.mostSignificantBits ushr 16 })
    }

    @Test fun notes_areChronologicalAcrossMonths() {
        val notes = DebugNoteSeed.notes()

        for (i in 1 until notes.size) {
            assertTrue(notes[i].createdAt > notes[i - 1].createdAt)
        }
        val span = notes.last().createdAt - notes.first().createdAt
        assertTrue(span >= 150L * DAY)
        assertTrue(span <= 200L * DAY)
        val listedNewestFirst = notes.sortedWith(
            compareByDescending<com.linote.data.Note> { it.createdAt }.thenByDescending { it.uuid.toString() },
        )
        assertEquals(notes.asReversed(), listedNewestFirst)
    }

    @Test fun notes_keepValidModifiedAtAndEmptyV1Fields() {
        val notes = DebugNoteSeed.notes()
        val newest = notes.last().createdAt

        assertTrue(notes.any { it.modifiedAt == it.createdAt })
        assertTrue(notes.any { it.modifiedAt > it.createdAt })
        assertTrue(notes.all { it.modifiedAt >= it.createdAt && it.modifiedAt <= newest })
        assertTrue(notes.all { it.title == null && it.referencedNoteID == null })
        notes.forEach { note ->
            val text = visible(note)
            assertTrue(text.isNotEmpty())
            assertEquals(note.content, writePieces(listOf(Piece.Text(text))))
        }
    }

    @Test fun notes_followTheLengthMix() {
        val texts = DebugNoteSeed.notes().map { visible(it) }
        val normal = texts.filter { !edge(it) }

        assertEquals(50, texts.count { edge(it) })
        assertEquals(200, normal.count { words(it) in 1..10 })
        assertEquals(400, normal.count { words(it) in 11..40 })
        assertEquals(250, normal.count { words(it) in 41..100 })
        assertEquals(100, normal.count { words(it) in 101..250 })
        assertEquals(950, normal.size)
    }

    @Test fun notes_includeEdgeCases() {
        val texts = DebugNoteSeed.notes().map { visible(it) }

        assertTrue(texts.any { it.length == 1 && it.first().isLetter() })
        assertTrue(texts.any { it.length >= 3 && it.all { c -> !c.isLetterOrDigit() && !c.isWhitespace() } })
        assertTrue(texts.any { '\n' in it && it.any { c -> c.isLetter() } })
        assertTrue(texts.any { it.split(Regex("\\s+")).any { word -> word.length >= 60 } })
    }

    @Test fun notes_areReproducibleAndNotANumberedTemplate() {
        val first = DebugNoteSeed.notes()
        val again = DebugNoteSeed.notes()
        assertEquals(first, again)

        val texts = first.map { visible(it) }
        assertEquals(1000, texts.toSet().size)
        assertTrue(texts.none { it.startsWith("Note ") || it.contains("Lorem ipsum") })
        val openings = texts.filter { !edge(it) }.map { it.split(Regex("\\s+")).take(4).joinToString(" ") }
        assertTrue(openings.groupingBy { it }.eachCount().maxOf { it.value } < openings.size / 5)
    }

    @Test fun seedWork_insertsWhenSyntheticKeysAreAbsent() {
        val synthetic = listOf(note(1), note(2))
        val user = note(9)

        val work = DebugNoteSeed.seedWork(synthetic, setOf(user.uuid), replace = false)

        assertEquals("Inserted 2 notes.", work.message)
        assertEquals(emptyList<UUID>(), work.delete)
        assertEquals(synthetic, work.insert)
    }

    @Test fun seedWork_skipsWhenSyntheticKeysExist() {
        val synthetic = listOf(note(1), note(2))
        val user = note(9)

        val work = DebugNoteSeed.seedWork(synthetic, setOf(synthetic[0].uuid, user.uuid), replace = false)

        assertEquals("Already seeded. Left every note in place.", work.message)
        assertEquals(emptyList<UUID>(), work.delete)
        assertEquals(emptyList<com.linote.data.Note>(), work.insert)
    }

    @Test fun seedWork_replaceDeletesOnlySyntheticKeys() {
        val synthetic = listOf(note(1), note(2))
        val user = note(9)

        val work = DebugNoteSeed.seedWork(synthetic, setOf(synthetic[0].uuid, user.uuid), replace = true)

        assertEquals("Replaced 1 synthetic note. Left other notes in place.", work.message)
        assertEquals(listOf(synthetic[0].uuid), work.delete)
        assertEquals(synthetic, work.insert)
        assertTrue(user.uuid !in work.delete)
    }

    private fun note(n: Int) = com.linote.data.Note(
        uuid = UUID(0, n.toLong()),
        createdAt = n.toLong(),
        modifiedAt = n.toLong(),
        content = "c$n",
        title = null,
        referencedNoteID = null,
    )

    private fun visible(note: com.linote.data.Note): String =
        (readContent(note.content).single() as Piece.Text).value

    private fun words(text: String) = text.split(Regex("\\s+")).count { it.isNotEmpty() }

    private fun edge(text: String): Boolean {
        if (text.length == 1) return true
        if ('\n' in text) return true
        if (text.none { it.isLetterOrDigit() || it.isWhitespace() }) return true
        return text.split(Regex("\\s+")).any { it.length >= 40 }
    }

    private companion object {
        const val DAY = 24L * 60 * 60 * 1000
    }
}
