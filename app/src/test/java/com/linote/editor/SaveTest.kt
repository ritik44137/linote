package com.linote.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class SaveTest {
    @Test fun mediaOnly_buildsANote() {
        val media = Piece.Media(
            UUID.fromString("00000000-0000-7000-8000-000000000001"),
            "a.jpg",
            MediaKind.IMAGE,
            "file:///a",
        )
        val editor = idleEditor().insertMedia(media, 1_700_000_000_123L)
        val note = noteFrom(editor)!!
        assertEquals(1_700_000_000_123L, note.createdAt)
        assertEquals(note.createdAt, note.modifiedAt)
        assertEquals(7, note.uuid.version())
        assertNull(note.title)
        assertNull(note.referencedNoteID)
        assertEquals(writePieces(editor.pieces), note.content)
        assertNull(noteFrom(idleEditor().commitText(" \n", 1)))
    }

    @Test fun caretMove_isTheSameDocument_textChangeIsNot() {
        val saved = idleEditor().commitText("hi", 5)
        assertTrue(sameDocument(saved, saved.setCaret(1)))
        assertFalse(sameDocument(saved, saved.commitText("!", 5)))
    }

    @Test fun throwingInsert_leavesTheEditor() {
        val editor = idleEditor().commitText("hi", 5)
        val again = finishInsert(editor) { throw IllegalStateException("full") }
        assertEquals(editor, again)
        assertEquals(idleEditor(), finishInsert(editor) { })
    }
}
