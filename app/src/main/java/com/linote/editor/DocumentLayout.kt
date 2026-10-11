package com.linote.editor

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

data class Metrics(
    val columnWidth: Float,
    val charWidth: Float,
    val lineHeight: Float,
    val block: Float,
    val nameGap: Float,
    val nameHeight: Float,
    val sideGap: Float,
    val widthOf: (String) -> Float = { text -> text.length * charWidth },
)

data class VisualLine(
    val flatStart: Int,
    val flatEnd: Int,
    val x: Float,
    val y: Float,
    val text: String,
    val caretX: List<Float> = listOf(0f),
)

data class PlacedBlock(
    val flatIndex: Int,
    val x: Float,
    val y: Float,
    val size: Float,
    val nameTop: Float,
    val bottom: Float,
)

data class DocLayout(
    val lines: List<VisualLine>,
    val blocks: List<PlacedBlock>,
    val height: Float,
    val lineHeight: Float,
)

fun layout(pieces: List<Piece>, metrics: Metrics): DocLayout {
    // shortcut: each glyph is measured alone, so kerning can miss the caret by a pixel; upgrade when that miss is visible.
    val normalized = normalize(pieces)
    if (normalized.size == 1 && normalized[0] is Piece.Text && (normalized[0] as Piece.Text).value.isEmpty()) {
        return DocLayout(
            lines = listOf(VisualLine(0, 0, 0f, 0f, "")),
            blocks = emptyList(),
            height = metrics.lineHeight,
            lineHeight = metrics.lineHeight,
        )
    }
    val lines = mutableListOf<VisualLine>()
    val blocks = mutableListOf<PlacedBlock>()
    var y = 0f
    var flat = 0
    var openBottom: Float? = null
    var sideY = 0f

    fun closeBlock() {
        val bottom = openBottom ?: return
        if (y < bottom) y = bottom
        openBottom = null
    }

    data class Slot(val x: Float, val y: Float, val width: Float, val side: Boolean)

    fun slot(): Slot {
        val bottom = openBottom
        if (bottom != null) {
            val sideWidth = metrics.columnWidth - metrics.block - metrics.sideGap
            if (sideY + metrics.lineHeight <= bottom && sideWidth >= metrics.charWidth) {
                return Slot(metrics.block + metrics.sideGap, sideY, sideWidth, true)
            }
            y = maxOf(y, bottom)
            openBottom = null
        }
        return Slot(0f, y, metrics.columnWidth.coerceAtLeast(metrics.charWidth), false)
    }

    fun advance(slot: Slot) {
        if (slot.side) sideY += metrics.lineHeight else y = slot.y + metrics.lineHeight
    }

    fun flow(text: String) {
        var i = 0
        while (i < text.length) {
            val place = slot()
            val take = takeLine(text, i, place.width, metrics.widthOf)
            if (take.advance == 0) break
            lines += VisualLine(
                flatStart = flat,
                flatEnd = flat + take.text.length + if (take.newline) 1 else 0,
                x = place.x,
                y = place.y,
                text = take.text,
                caretX = take.caretX,
            )
            flat += take.text.length + if (take.newline) 1 else 0
            i += take.advance
            advance(place)
            if (take.newline && i >= text.length) {
                val blank = slot()
                lines += VisualLine(flat, flat, blank.x, blank.y, "", caretX = listOf(0f))
                advance(blank)
            }
        }
    }

    for (piece in normalized) when (piece) {
        is Piece.Text -> flow(piece.value)
        is Piece.Media -> {
            closeBlock()
            val top = y
            val bottom = top + metrics.block + metrics.nameGap + metrics.nameHeight
            blocks += PlacedBlock(
                flatIndex = flat,
                x = 0f,
                y = top,
                size = metrics.block,
                nameTop = top + metrics.block + metrics.nameGap,
                bottom = bottom,
            )
            flat += 1
            openBottom = bottom
            sideY = top
        }
    }
    closeBlock()
    val lineBottom = lines.maxOfOrNull { it.y + metrics.lineHeight } ?: 0f
    val blockBottom = blocks.maxOfOrNull { it.bottom } ?: 0f
    return DocLayout(lines, blocks, maxOf(y, lineBottom, blockBottom), metrics.lineHeight)
}

private data class Take(
    val text: String,
    val advance: Int,
    val newline: Boolean,
    val caretX: List<Float>,
)

private fun takeLine(text: String, start: Int, maxWidth: Float, widthOf: (String) -> Float): Take {
    if (start >= text.length) return Take("", 0, false, listOf(0f))
    if (text[start] == '\n') return Take("", 1, true, listOf(0f))
    // shortcut: sum per character, same result as stringWidth; a kerning widthOf needs the prefix walk back
    val caretX = ArrayList<Float>(64)
    caretX.add(0f)
    var count = 0
    var lastBreak = -1
    var width = 0f
    while (start + count < text.length && text[start + count] != '\n') {
        val next = width + widthOf(text.substring(start + count, start + count + 1))
        if (count > 0 && next > maxWidth) break
        width = next
        count++
        caretX.add(width)
        if (text[start + count - 1] == ' ') lastBreak = count
    }
    val remains = start + count < text.length
    if (remains && text[start + count] == '\n') {
        return Take(text.substring(start, start + count), count + 1, true, caretX)
    }
    if (remains && lastBreak > 0) {
        while (caretX.size > lastBreak + 1) caretX.removeAt(caretX.lastIndex)
        return Take(text.substring(start, start + lastBreak), lastBreak, false, caretX)
    }
    return Take(text.substring(start, start + count), count, false, caretX)
}

fun cardMediaWidth(pieces: List<Piece>, contentWidth: Float, preview: Float): Float? {
    val normalized = normalize(pieces)
    val media = normalized.filterIsInstance<Piece.Media>()
    if (media.isEmpty()) return null
    val chars = normalized.filterIsInstance<Piece.Text>().sumOf { it.value.length }
    return if (media.size == 1 && chars == 0) contentWidth else preview
}

fun composerExtraLines(layoutHeight: Float, lineHeight: Float): Int {
    if (layoutHeight <= lineHeight) return 0
    return min(4, floor((layoutHeight - lineHeight) / lineHeight).toInt())
}

fun caretPosition(layout: DocLayout, index: Int, charWidth: Float): Pair<Float, Float> {
    val line = lineOf(layout, index)
    if (line != null) {
        val cells = (index - line.flatStart).coerceAtMost(line.text.length)
        return (line.x + offsetAt(line, cells, charWidth)) to line.y
    }
    var x = 0f
    var y = 0f
    var found = false
    for (block in layout.blocks) {
        if (index == block.flatIndex) {
            x = block.x
            y = block.y
            found = true
        } else if (index == block.flatIndex + 1) {
            x = block.x + block.size
            y = block.y
            found = true
        }
    }
    return if (found) x to y else 0f to 0f
}

fun selectionInk(
    layout: DocLayout,
    start: Int,
    end: Int,
    charWidth: Float,
    lineHeight: Float,
    draw: (x: Float, y: Float, w: Float, h: Float) -> Unit,
) {
    if (start >= end) return
    val blocks = HashSet<Int>(layout.blocks.size)
    for (block in layout.blocks) {
        if (block.flatIndex in start until end) {
            blocks.add(block.flatIndex)
            draw(block.x, block.y, block.size, block.size)
        }
    }
    for (line in layout.lines) {
        val lineEnd = inclusiveEnd(layout, line)
        val first = max(start, line.flatStart)
        val last = min(end - 1, lineEnd)
        for (index in first..last) {
            if (index in blocks) continue
            val cells = (index - line.flatStart).coerceAtLeast(0)
            val clamped = cells.coerceAtMost(line.text.length)
            val x = line.x + offsetAt(line, clamped, charWidth)
            val w = if (cells >= line.text.length) {
                charWidth
            } else {
                (offsetAt(line, cells + 1, charWidth) - offsetAt(line, cells, charWidth)).coerceAtLeast(1f)
            }
            draw(x, line.y, w, lineHeight)
        }
    }
}

fun glyphWidth(layout: DocLayout, index: Int, charWidth: Float): Float {
    val line = lineOf(layout, index) ?: return charWidth
    val cells = (index - line.flatStart).coerceAtLeast(0)
    if (cells >= line.text.length) return charWidth
    return (offsetAt(line, cells + 1, charWidth) - offsetAt(line, cells, charWidth)).coerceAtLeast(1f)
}

private fun offsetAt(line: VisualLine, cells: Int, charWidth: Float): Float {
    val clamped = cells.coerceAtMost(line.text.length)
    return line.caretX.getOrElse(clamped) { clamped * charWidth }
}

fun mediaAt(pieces: List<Piece>, flatIndex: Int): Piece.Media? {
    var index = 0
    for (piece in normalize(pieces)) when (piece) {
        is Piece.Text -> index += piece.value.length
        is Piece.Media -> {
            if (index == flatIndex) return piece
            index += 1
        }
    }
    return null
}

fun hit(layout: DocLayout, x: Float, y: Float, charWidth: Float): Int {
    for (block in layout.blocks) {
        if (x >= block.x && x < block.x + block.size && y >= block.y && y < block.y + block.size) {
            return block.flatIndex + 1
        }
    }
    val line = layout.lines.lastOrNull { y >= it.y && y < it.y + layout.lineHeight }
        ?: layout.lines.minByOrNull { abs(it.y + layout.lineHeight / 2f - y) }
        ?: return 0
    val end = inclusiveEnd(layout, line)
    var best = line.flatStart
    var bestDist = Float.POSITIVE_INFINITY
    for (index in line.flatStart..end) {
        val cells = (index - line.flatStart).coerceAtMost(line.text.length)
        val dist = abs(line.x + offsetAt(line, cells, charWidth) - x)
        if (dist < bestDist || (dist == bestDist && index > best)) {
            best = index
            bestDist = dist
        }
    }
    return best
}

fun moveVertical(layout: DocLayout, caret: Int, direction: Int, xGoal: Float, charWidth: Float): Int {
    val current = lineOf(layout, caret) ?: return caret
    val nextY = if (direction > 0) {
        layout.lines.filter { it.y > current.y }.minOfOrNull { it.y }
    } else {
        layout.lines.filter { it.y < current.y }.maxOfOrNull { it.y }
    } ?: return caret
    val line = layout.lines.filter { it.y == nextY }.minBy { abs(it.x - xGoal) }
    val end = inclusiveEnd(layout, line)
    var best = line.flatStart
    var bestDist = Float.POSITIVE_INFINITY
    for (index in line.flatStart..end) {
        val cells = (index - line.flatStart).coerceAtMost(line.text.length)
        val dist = abs(line.x + offsetAt(line, cells, charWidth) - xGoal)
        if (dist < bestDist || (dist == bestDist && index > best)) {
            best = index
            bestDist = dist
        }
    }
    return best
}

private fun lineOf(layout: DocLayout, caret: Int): VisualLine? {
    val lines = layout.lines
    var lo = 0
    var hi = lines.lastIndex
    while (lo <= hi) {
        val mid = (lo + hi) ushr 1
        val line = lines[mid]
        val end = inclusiveEnd(layout, line)
        when {
            caret < line.flatStart -> hi = mid - 1
            caret > end -> lo = mid + 1
            else -> return line
        }
    }
    return null
}

private fun inclusiveEnd(layout: DocLayout, line: VisualLine): Int {
    val last = layout.lines.last()
    return if (line == last) line.flatEnd else (line.flatEnd - 1).coerceAtLeast(line.flatStart)
}
