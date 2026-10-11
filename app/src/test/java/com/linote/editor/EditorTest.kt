package com.linote.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class EditorTest {
    private val media = Piece.Media(
        UUID.fromString("00000000-0000-7000-8000-000000000001"),
        "forest.jpg", MediaKind.IMAGE, "file:///path",
    )

    @Test fun tab_startsTimestamp_carriageReturnDoesNot() {
        assertEquals(7L, idleEditor().commitText("\t", 7).timestamp)
        assertNull(idleEditor().commitText("\r", 7).timestamp)
        assertNull(idleEditor().commitText(" \n", 7).timestamp)
    }

    @Test fun timestamp_startsOnLetterAndMedia_andClearsWhenGone() {
        var e = idleEditor().commitText(" \n", 1)
        assertNull(e.timestamp)
        e = e.commitText("a", 2)
        assertEquals(2L, e.timestamp)
        e = e.commitText("b", 3)
        assertEquals(2L, e.timestamp)
        e = e.backspace(4)
        assertEquals(2L, e.timestamp)
        e = e.backspace(5)
        assertNull(e.timestamp)
        assertEquals(" \n", e.flat())

        val saved = idleEditor().insertMedia(media, 50)
        assertEquals(50L, saved.timestamp)
        assertEquals("\uFFFC", saved.flat())
        assertNull(saved.backspace(99).timestamp)
    }

    @Test fun spacesShowArrow_andDoNotSave() {
        val spaces = idleEditor().commitText(" \n", 1)
        assertTrue(showsArrow(spaces))
        assertFalse(showsHint(spaces))
        assertNull(spaces.timestamp)
        assertTrue(showsHint(idleEditor()))
        assertTrue(showsArrow(idleEditor().insertMedia(media, 1)))
    }

    @Test fun backspace_forwardDelete_andSelection_removeMediaAndCloseText() {
        val e = idleEditor().commitText("ab", 1).insertMedia(media, 1).commitText("c", 1)
        assertEquals("ab\uFFFCc", e.flat())
        assertEquals("abc", e.setCaret(3).backspace(1).flat())
        assertEquals("abc", e.setCaret(2).deleteForward(1).flat())
        assertEquals("abc", e.selectRange(2, 3).backspace(1).flat())
        assertEquals("a", e.selectRange(1, 4).backspace(1).flat())
    }

    @Test fun enter_insertsLineBreak_andDoesNotStamp() {
        val e = idleEditor().commitText("\n", 1)
        assertEquals("\n", e.flat())
        assertNull(e.timestamp)
    }

    @Test fun paste_staysPlainText() {
        val e = idleEditor().commitText("file:///x.png", 1)
        assertTrue(e.pieces.single() is Piece.Text)
    }

    @Test fun composingOverMedia_commitsFirst() {
        val e = idleEditor().commitText("a", 1).insertMedia(media, 1)
        val over = e.selectRange(0, 2).setComposing("q", 1)
        assertNull(over.composition)
        assertEquals("q", over.flat())
    }

    @Test fun normalize_keepsAnAlreadyGroupedDocument() {
        val pieces = listOf(Piece.Text("ab"), media)
        assertTrue(normalize(pieces) === pieces)
        val empty = listOf(Piece.Text(""))
        assertTrue(normalize(empty) === empty)
        assertEquals("ab", (normalize(listOf(Piece.Text("a"), Piece.Text("b"))).single() as Piece.Text).value)
    }

    @Test fun longPress_selectsWordOrMedia() {
        val word = idleEditor().commitText("hello there", 1).selectWord(1)
        assertEquals(0, word.anchor)
        assertEquals(5, word.caret)
        val piece = idleEditor().insertMedia(media, 1).selectWord(0)
        assertEquals(0, piece.anchor)
        assertEquals(1, piece.caret)
    }
}
