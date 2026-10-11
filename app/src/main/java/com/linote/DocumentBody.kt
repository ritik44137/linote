package com.linote

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.linote.editor.DocLayout
import com.linote.editor.Editor
import com.linote.editor.Piece
import com.linote.editor.caretPosition
import com.linote.editor.glyphWidth
import com.linote.editor.mediaAt
import com.linote.editor.selectionInk
import com.linote.editor.showsHint
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.roundToInt

fun stringWidth(measurer: TextMeasurer, style: TextStyle): (String) -> Float {
    val cache = HashMap<String, Float>()
    return { text ->
        var total = 0f
        for (index in text.indices) {
            val ch = text.substring(index, index + 1)
            total += cache.getOrPut(ch) {
                measurer.measure(
                    ch,
                    style,
                    overflow = TextOverflow.Visible,
                    softWrap = false,
                    maxLines = 1,
                ).size.width.toFloat()
            }
        }
        total
    }
}

@Composable
fun DocumentBody(
    editor: Editor,
    layout: DocLayout,
    charWidth: Float,
    lineHeightPx: Float,
    ink: Color,
    inkDim: Color,
    textMeasurer: TextMeasurer,
    bodyStyle: TextStyle,
    hintStyle: TextStyle,
    nameStyle: TextStyle,
    showCursor: Boolean,
    showHint: Boolean = true,
    modifier: Modifier = Modifier,
    tile: @Composable (Piece.Media) -> Unit = { MediaTile(it) },
) {
    val density = LocalDensity.current
    Box(modifier.fillMaxWidth().height(with(density) { layout.height.toDp() })) {
        Canvas(Modifier.matchParentSize()) {
            // drawText clips to the canvas width left of x, so a second character was clipped away.
            fun paintText(text: String, style: TextStyle, topLeft: Offset, width: Float) {
                drawText(
                    textMeasurer,
                    text,
                    topLeft = topLeft,
                    style = style,
                    overflow = TextOverflow.Visible,
                    softWrap = false,
                    maxLines = 1,
                    size = Size(width.coerceAtLeast(1f), lineHeightPx),
                )
            }
            val start = min(editor.anchor, editor.caret)
            val end = kotlin.math.max(editor.anchor, editor.caret)
            if (showCursor && start != end) {
                val tint = ink.copy(alpha = 0.2f)
                selectionInk(layout, start, end, charWidth, lineHeightPx) { x, y, w, h ->
                    drawRect(tint, Offset(x, y), Size(w, h))
                }
            }
            val composition = editor.composition
            if (showCursor && composition != null) {
                for (index in composition.first until composition.second) {
                    if (layout.blocks.any { it.flatIndex == index }) continue
                    val (x, y) = caretPosition(layout, index, charWidth)
                    drawLine(
                        ink,
                        Offset(x, y + lineHeightPx - 1.dp.toPx()),
                        Offset(x + glyphWidth(layout, index, charWidth), y + lineHeightPx - 1.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
            for (line in layout.lines) {
                if (line.text.isEmpty()) continue
                val width = (line.caretX.lastOrNull() ?: (line.text.length * charWidth)) + 2.dp.toPx()
                paintText(line.text, bodyStyle, Offset(line.x, line.y), width)
            }
            if (showHint && showsHint(editor)) {
                val hint = "Something to say?"
                val hintWidth = textMeasurer.measure(
                    hint,
                    hintStyle,
                    overflow = TextOverflow.Visible,
                    softWrap = false,
                    maxLines = 1,
                ).size.width.toFloat()
                paintText(hint, hintStyle, Offset.Zero, hintWidth)
            }
        }
        if (showCursor) {
            BlinkingCaret(editor, layout, charWidth, lineHeightPx, ink)
        }
        for (block in layout.blocks) {
            val piece = mediaAt(editor.pieces, block.flatIndex) ?: continue
            Box(
                Modifier
                    .offset { IntOffset(block.x.roundToInt(), block.y.roundToInt()) }
                    .size(with(density) { block.size.toDp() })
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
            ) {
                tile(piece)
            }
            BasicText(
                piece.name,
                modifier = Modifier
                    .offset { IntOffset(block.x.roundToInt(), block.nameTop.roundToInt()) }
                    .width(with(density) { block.size.toDp() }),
                style = nameStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BoxScope.BlinkingCaret(
    editor: Editor,
    layout: DocLayout,
    charWidth: Float,
    lineHeightPx: Float,
    ink: Color,
) {
    var cursorOn by remember { mutableStateOf(true) }
    LaunchedEffect(editor.caret) {
        cursorOn = true
        while (true) {
            delay(500)
            cursorOn = !cursorOn
        }
    }
    if (!cursorOn) return
    val (x, y) = caretPosition(layout, editor.caret, charWidth)
    Canvas(Modifier.matchParentSize()) {
        drawRect(ink, Offset(x, y), Size(1.dp.toPx(), lineHeightPx))
    }
}
