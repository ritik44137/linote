package com.linote.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class DocumentJsonTest {
    private val pieces = listOf(
        Piece.Text("a\"b\\c\n"),
        Piece.Media(
            UUID.fromString("00000000-0000-7000-8000-000000000001"),
            "forest.jpg", MediaKind.IMAGE, "file:///path",
        ),
    )

    @Test fun pieces_roundTrip() {
        assertEquals(pieces, readContent(writePieces(pieces)))
    }

    @Test fun plainAndInvalid_areOneTextPiece_validArrayIsADocument() {
        assertEquals(listOf(Piece.Text("hello")), readContent("hello"))
        assertEquals(listOf(Piece.Text("{")), readContent("{"))
        assertEquals(listOf(Piece.Text("hi")), readContent("""[{"type":"text","value":"hi"}]"""))
    }

    @Test fun draft_keepsTimestamp_andRejectsGarbage() {
        val editor = Editor(pieces, 1_700_000_000_123L, 0, 0, null)
        val loaded = readDraft(writeDraft(editor))
        assertEquals(pieces, loaded!!.pieces)
        assertEquals(1_700_000_000_123L, loaded.timestamp)
        assertNull(readDraft("nope"))
        assertNull(readDraft(writeDraft(idleEditor()))!!.timestamp)
    }
}
