package com.linote

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linote.data.Note
import com.linote.editor.Editor
import com.linote.editor.Metrics
import com.linote.editor.Piece
import com.linote.editor.cardMediaWidth
import com.linote.editor.formatNoteTime
import com.linote.editor.layout
import com.linote.editor.readContent
import java.time.ZoneId
import java.util.UUID

internal enum class NewestFollow { Idle, Wait, Jump, Cancel }

internal fun newestFollow(
    armed: Boolean,
    armedHead: UUID?,
    head: UUID?,
    atNewestEdge: Boolean,
): NewestFollow = when {
    !armed -> NewestFollow.Idle
    head != null && head != armedHead -> NewestFollow.Jump
    !atNewestEdge -> NewestFollow.Cancel
    else -> NewestFollow.Wait
}

internal fun atNewestEdge(firstVisibleIndex: Int, scrollOffset: Int): Boolean =
    firstVisibleIndex == 0 && scrollOffset == 0

internal fun newestNoteId(notes: LazyPagingItems<Note>): UUID? =
    notes.itemSnapshotList.getOrNull(0)?.uuid

@Composable
fun HomeHeader(modifier: Modifier = Modifier, onLongTitle: (() -> Unit)? = null) {
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val inkHot = colorResource(R.color.ink_hot)
    val inkDim = colorResource(R.color.ink_dim)
    val accent = colorResource(R.color.accent)
    val interaction = remember { MutableInteractionSource() }
    val longPress = if (onLongTitle != null) {
        Modifier.combinedClickable(
            interactionSource = interaction,
            indication = null,
            onLongClick = onLongTitle,
            onClick = {},
        )
    } else {
        Modifier
    }
    Row(
        modifier
            .fillMaxWidth()
            .then(longPress)
            .padding(top = statusTop + 12.dp, bottom = 8.dp, start = 20.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            BasicText(
                "Linote",
                style = TextStyle(
                    color = inkHot,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 34.sp,
                ),
            )
            BasicText(
                "THOUGHTS, NATURALLY",
                style = TextStyle(
                    color = inkDim,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    letterSpacing = 1.6.sp,
                ),
            )
        }
        SearchIcon(Modifier.padding(8.dp), accent)
        OverflowIcon(Modifier.padding(horizontal = 8.dp), accent)
    }
}

@Stable
private class NoteFeed(val pages: LazyPagingItems<Note>)

@Composable
fun NoteList(
    notes: LazyPagingItems<Note>,
    bottomInset: Dp,
    listState: LazyListState,
    armed: Boolean,
    armedHead: UUID?,
    scrolledAway: Boolean,
    onScrolledAway: () -> Unit,
    onFollowSettled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val feed = remember(notes) { NoteFeed(notes) }
    FollowNewest(listState, notes, armed, armedHead, scrolledAway, onScrolledAway, onFollowSettled)
    NoteColumn(feed, bottomInset, listState, modifier)
}

@Composable
private fun FollowNewest(
    listState: LazyListState,
    notes: LazyPagingItems<Note>,
    armed: Boolean,
    armedHead: UUID?,
    scrolledAway: Boolean,
    onScrolledAway: () -> Unit,
    onFollowSettled: () -> Unit,
) {
    // Scroll position is read here so a drag does not recompose the cards.
    val index = listState.firstVisibleItemIndex
    val offset = listState.firstVisibleItemScrollOffset
    val scrollingAway = armed && listState.isScrollInProgress && !atNewestEdge(index, offset)
    // A composer resize can leave the edge; only a finger scroll should cancel the jump.
    val action = newestFollow(armed, armedHead, newestNoteId(notes), atNewestEdge = !scrolledAway)
    SideEffect {
        if (scrollingAway) onScrolledAway()
        if (action == NewestFollow.Jump) listState.requestScrollToItem(0)
        if (action == NewestFollow.Jump || action == NewestFollow.Cancel) onFollowSettled()
    }
}

@Composable
private fun NoteColumn(
    feed: NoteFeed,
    bottomInset: Dp,
    listState: LazyListState,
    modifier: Modifier,
) {
    val notes = feed.pages
    val textMeasurer = rememberTextMeasurer()
    val ink = colorResource(R.color.ink)
    val inkDim = colorResource(R.color.ink_dim)
    val inkFaint = colorResource(R.color.ink_faint)
    val bodyStyle = TextStyle(color = ink, fontFamily = FontFamily.Serif, fontSize = 16.sp)
    val nameStyle = TextStyle(color = inkFaint, fontFamily = FontFamily.SansSerif, fontSize = 11.sp)
    val widthOf = remember(textMeasurer, bodyStyle) { stringWidth(textMeasurer, bodyStyle) }
    val sample = remember(textMeasurer, bodyStyle) { textMeasurer.measure("0", bodyStyle) }
    val nameSample = remember(textMeasurer, nameStyle) { textMeasurer.measure("0", nameStyle) }
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().padding(bottom = bottomInset),
        reverseLayout = true,
        contentPadding = PaddingValues(top = 4.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
    ) {
        items(
            count = notes.itemCount,
            key = notes.itemKey { it.uuid },
        ) { index ->
            notes[index]?.let { note ->
                NoteCard(note, textMeasurer, widthOf, sample, nameSample, ink, inkDim, inkFaint, bodyStyle, nameStyle)
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    widthOf: (String) -> Float,
    sample: TextLayoutResult,
    nameSample: TextLayoutResult,
    ink: androidx.compose.ui.graphics.Color,
    inkDim: androidx.compose.ui.graphics.Color,
    inkFaint: androidx.compose.ui.graphics.Color,
    bodyStyle: TextStyle,
    nameStyle: TextStyle,
) {
    val pieces = remember(note.content) { readContent(note.content) }
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .border(1.dp, colorResource(R.color.rule), shape)
            .background(colorResource(R.color.paper_2), shape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                formatNoteTime(note.createdAt, ZoneId.systemDefault()),
                modifier = Modifier.weight(1f),
                style = TextStyle(color = inkFaint, fontFamily = FontFamily.SansSerif, fontSize = 12.sp),
            )
            OverflowIcon(tint = inkDim, size = 16.dp)
        }
        Spacer(Modifier.height(10.dp))
        CardBody(pieces, textMeasurer, widthOf, sample, nameSample, ink, inkFaint, bodyStyle, nameStyle)
    }
}

@Composable
private fun CardBody(
    pieces: List<Piece>,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    widthOf: (String) -> Float,
    sample: TextLayoutResult,
    nameSample: TextLayoutResult,
    ink: androidx.compose.ui.graphics.Color,
    inkFaint: androidx.compose.ui.graphics.Color,
    bodyStyle: TextStyle,
    nameStyle: TextStyle,
) {
    val charWidth = sample.size.width.toFloat().coerceAtLeast(1f)
    val lineHeight = sample.size.height.toFloat().coerceAtLeast(1f)
    val nameHeight = nameSample.size.height.toFloat().coerceAtLeast(1f)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val contentPx = with(density) { maxWidth.toPx() }
        val previewPx = with(density) { 96.dp.toPx() }
        val nameGap = with(density) { 2.dp.toPx() }
        val sideGap = with(density) { 12.dp.toPx() }
        val mode = remember(pieces, contentPx, previewPx) { cardMediaWidth(pieces, contentPx, previewPx) }
        if (mode == contentPx) {
            SoloMedia(pieces.filterIsInstance<Piece.Media>().single())
        } else {
            val laid = remember(pieces, contentPx, charWidth, lineHeight, previewPx, nameGap, nameHeight, sideGap, widthOf) {
                layout(
                    pieces,
                    Metrics(contentPx, charWidth, lineHeight, previewPx, nameGap, nameHeight, sideGap, widthOf),
                )
            }
            val editor = remember(pieces) { Editor(pieces, null, 0, 0, null) }
            DocumentBody(
                editor = editor,
                layout = laid,
                charWidth = charWidth,
                lineHeightPx = lineHeight,
                ink = ink,
                inkDim = inkFaint,
                textMeasurer = textMeasurer,
                bodyStyle = bodyStyle,
                hintStyle = bodyStyle,
                nameStyle = nameStyle,
                showCursor = false,
                showHint = false,
            )
        }
    }
}
