package com.linote

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PaperclipIcon(modifier: Modifier = Modifier, tint: Color, size: Dp = 22.dp) {
    StrokeIcon(
        modifier,
        tint,
        listOf(
            "M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l8.57-8.57A4 4 0 1 1 18 8.84l-8.59 8.57a2 2 0 0 1-2.83-2.83l8.49-8.48",
        ),
        size,
    )
}

@Composable
fun ArrowUpIcon(modifier: Modifier = Modifier, tint: Color, size: Dp = 22.dp) {
    StrokeIcon(
        modifier,
        tint,
        listOf(
            "M12 19V5",
            "M5 12l7-7 7 7",
        ),
        size,
    )
}

@Composable
fun SearchIcon(modifier: Modifier = Modifier, tint: Color, size: Dp = 22.dp) {
    Canvas(modifier.size(size)) {
        val unit = this.size.minDimension / 24f
        val stroke = Stroke(width = 2f * unit, cap = StrokeCap.Round)
        drawCircle(tint, radius = 7f * unit, center = Offset(11f * unit, 11f * unit), style = stroke)
        drawLine(
            tint,
            Offset(16.2f * unit, 16.2f * unit),
            Offset(21f * unit, 21f * unit),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun OverflowIcon(modifier: Modifier = Modifier, tint: Color, size: Dp = 22.dp) {
    Canvas(modifier.size(size)) {
        val radius = size.toPx() * 0.07f
        val cx = this.size.width / 2f
        listOf(0.22f, 0.5f, 0.78f).forEach { fraction ->
            drawCircle(tint, radius, Offset(cx, this.size.height * fraction))
        }
    }
}

@Composable
fun FileGlyph(modifier: Modifier = Modifier, tint: Color, size: Dp = 22.dp) {
    StrokeIcon(
        modifier,
        tint,
        listOf(
            "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z",
            "M14 2v6h6",
        ),
        size,
    )
}

@Composable
private fun StrokeIcon(
    modifier: Modifier,
    tint: Color,
    paths: List<String>,
    size: Dp = 22.dp,
) {
    val parsed = remember(paths) {
        paths.map { PathParser().parsePathString(it).toPath() }
    }
    Canvas(modifier.size(size)) {
        val stroke = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        scale(this.size.minDimension / 24f, this.size.minDimension / 24f, pivot = Offset.Zero) {
            parsed.forEach { drawPath(it, tint, style = stroke) }
        }
    }
}
