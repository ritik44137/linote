package com.linote.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class DocumentLayoutTest {
    private val m = Metrics(200f, 10f, 20f, 100f, 2f, 10f, 10f)
    private val media = Piece.Media(
        UUID.fromString("00000000-0000-7000-8000-000000000001"),
        "a.jpg",
        MediaKind.IMAGE,
        "file:///a",
    )

    @Test fun textSitsBesideABlock_thenUnderTheName_andASecondBlockStartsThere() {
        val laid = layout(listOf(media, Piece.Text("a".repeat(46))), m)
        assertEquals(110f, laid.lines.first().x, 0.01f)
        assertEquals(0f, laid.lines.first().y, 0.01f)
        assertEquals(0f, laid.lines.last().x, 0.01f)
        assertEquals(112f, laid.lines.last().y, 0.01f)

        val two = layout(listOf(media, Piece.Text("Hi"), media), m)
        assertEquals(112f, two.blocks[1].y, 0.01f)
        assertEquals(0f, two.blocks[1].x, 0.01f)
    }

    @Test fun narrowSideColumn_startsTextUnderTheName() {
        val narrow = m.copy(columnWidth = 110f)
        val laid = layout(listOf(media, Piece.Text("Z")), narrow)
        assertEquals(0f, laid.lines.single().x, 0.01f)
        assertEquals(112f, laid.lines.single().y, 0.01f)
    }

    @Test fun cardWidth_isFullOnlyForOneMediaAndNoCharacters() {
        assertEquals(300f, cardMediaWidth(listOf(media), 300f, 96f)!!, 0.01f)
        assertEquals(96f, cardMediaWidth(listOf(Piece.Text(" "), media), 300f, 96f)!!, 0.01f)
        assertEquals(96f, cardMediaWidth(listOf(media, media), 300f, 96f)!!, 0.01f)
    }

    @Test fun hitOnABlock_isJustAfterIt_andDownKeepsX() {
        val laid = layout(listOf(media), m)
        assertEquals(1, hit(laid, 50f, 50f, 10f))
        val lines = layout(listOf(Piece.Text("aaaabbbb")), m.copy(columnWidth = 40f))
        assertEquals(5, moveVertical(lines, 1, 1, 10f, 10f))
    }

    @Test fun wordThatOutgrowsTheLine_movesWholeToTheNextLine() {
        val laid = layout(listOf(Piece.Text("hi Linote")), m.copy(columnWidth = 60f))
        assertEquals(listOf("hi ", "Linote"), laid.lines.map { it.text })
    }

    @Test fun hitInTheLowerHalf_staysOnThatLine() {
        val lines = layout(listOf(Piece.Text("aaaabbbb")), m.copy(columnWidth = 40f))
        assertEquals(2, hit(lines, 15f, 15f, 10f))
    }

    @Test fun widerThanTheCell_wrapsOnMeasuredWidth() {
        val wide = m.copy(widthOf = { text -> text.length * 20f })
        val laid = layout(listOf(Piece.Text("abcd")), wide.copy(columnWidth = 50f))
        assertEquals(listOf("ab", "cd"), laid.lines.map { it.text })
        assertEquals(40f, laid.lines.first().caretX[2], 0.01f)
    }

    @Test fun composerGrowsByLines_thenCaps() {
        assertEquals(0, composerExtraLines(20f, 20f))
        assertEquals(1, composerExtraLines(40f, 20f))
        assertEquals(4, composerExtraLines(20f * 8, 20f))
    }

    @Test fun caret_onWrappedLine_newline_andBlock() {
        val wrapped = layout(listOf(Piece.Text("aaaabbbb")), m.copy(columnWidth = 40f))
        assertEquals(20f, caretPosition(wrapped, 2, 10f).first, 0.01f)
        assertEquals(0f, caretPosition(wrapped, 2, 10f).second, 0.01f)
        assertEquals(0f, caretPosition(wrapped, 4, 10f).first, 0.01f)
        assertEquals(20f, caretPosition(wrapped, 4, 10f).second, 0.01f)

        val broken = layout(listOf(Piece.Text("ab\n")), m.copy(columnWidth = 1000f))
        assertEquals(20f, caretPosition(broken, 2, 10f).first, 0.01f)
        assertEquals(0f, caretPosition(broken, 2, 10f).second, 0.01f)
        assertEquals(0f, caretPosition(broken, 3, 10f).first, 0.01f)
        assertEquals(20f, caretPosition(broken, 3, 10f).second, 0.01f)

        val block = layout(listOf(media), m)
        assertEquals(0f, caretPosition(block, 0, 10f).first, 0.01f)
        assertEquals(100f, caretPosition(block, 1, 10f).first, 0.01f)
    }

    @Test fun selectionInk_drawsEachGlyph_andABlockSquare() {
        val text = layout(listOf(Piece.Text("abcd")), m.copy(columnWidth = 40f))
        val glyphs = ArrayList<String>()
        selectionInk(text, 1, 3, 10f, 20f) { x, y, w, h -> glyphs += "$x,$y,$w,$h" }
        assertEquals(listOf("10.0,0.0,10.0,20.0", "20.0,0.0,10.0,20.0"), glyphs)

        val block = layout(listOf(media), m)
        val squares = ArrayList<String>()
        selectionInk(block, 0, 1, 10f, 20f) { x, y, w, h -> squares += "$x,$y,$w,$h" }
        assertEquals(listOf("0.0,0.0,100.0,100.0"), squares)
    }

    @Test fun longLine_caretIsPrefixSum_andWidthsAreNotRevisited() {
        val text = "word ".repeat(1600)
        var measured = 0
        val widthOf: (String) -> Float = { s ->
            measured += s.length
            s.length * 10f
        }
        val laid = layout(listOf(Piece.Text(text)), m.copy(columnWidth = 200f, widthOf = widthOf))
        assertEquals(400, laid.lines.size)
        assertEquals(200f, laid.lines.first().caretX.last(), 0.01f)
        assertEquals(0f, caretPosition(laid, 20, 10f).first, 0.01f)
        assertEquals(20f, caretPosition(laid, 20, 10f).second, 0.01f)
        assertTrue(measured <= text.length * 2)
    }
}
