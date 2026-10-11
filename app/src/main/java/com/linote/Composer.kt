package com.linote

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.text.InputType
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import kotlin.math.abs
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.PlatformTextInputModifierNode
import androidx.compose.ui.platform.establishTextInputSession
import com.linote.editor.DocLayout
import com.linote.editor.Editor
import com.linote.editor.Metrics
import com.linote.editor.backspace
import com.linote.editor.caretPosition
import com.linote.editor.commitText
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.linote.editor.deleteForward
import com.linote.editor.finishComposing
import com.linote.editor.flat
import com.linote.editor.hit
import com.linote.editor.layout
import com.linote.editor.moveVertical
import com.linote.editor.selectRange
import com.linote.editor.selectWord
import com.linote.editor.setCaret
import com.linote.editor.setComposing
import com.linote.editor.showsArrow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.max
import kotlin.math.min

@Composable
fun Composer(
    editor: Editor,
    focused: Boolean,
    onEdit: (((Editor, Long) -> Editor)) -> Unit,
    onFocus: () -> Unit,
    onSave: () -> Unit,
    onHeight: (Dp) -> Unit,
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val ink = colorResource(R.color.ink)
    val inkHot = colorResource(R.color.ink_hot)
    val inkDim = colorResource(R.color.ink_dim)
    val inkFaint = colorResource(R.color.ink_faint)
    val accent = colorResource(R.color.accent)
    val dark = isSystemInDarkTheme()
    val bar = if (dark) Color(0xFF201F1B) else colorResource(R.color.paper_3)
    val chrome = if (dark) Color(0xFFC4B798) else accent
    val hintColor = if (dark) Color(0xFFA9A295) else inkDim
    val bodyStyle = TextStyle(color = inkHot, fontFamily = FontFamily.Serif, fontSize = 16.sp)
    val hintStyle = TextStyle(color = hintColor, fontFamily = FontFamily.SansSerif, fontSize = 16.sp)
    val nameStyle = TextStyle(color = inkFaint, fontFamily = FontFamily.SansSerif, fontSize = 11.sp)
    val widthOf = remember(textMeasurer, bodyStyle) { stringWidth(textMeasurer, bodyStyle) }
    val measured = remember(textMeasurer, bodyStyle) { textMeasurer.measure("0", bodyStyle) }
    val nameMeasured = remember(textMeasurer, nameStyle) { textMeasurer.measure("0", nameStyle) }
    val charWidth = measured.size.width.toFloat().coerceAtLeast(1f)
    val lineHeightPx = measured.size.height.toFloat().coerceAtLeast(1f)
    var boxWidthPx by remember { mutableFloatStateOf(0f) }
    val side = 16.dp
    val cardPad = 16.dp // note card inner padding
    val glyph = 20.dp
    val screenPad = 16.dp
    val floatGap = 8.dp
    val toolbar = 52.dp
    val clipStart = floatGap + (toolbar - AttachButton) / 2 + (AttachButton - glyph) / 2 - screenPad
    val chromeInsets = composerChrome(side, glyph, AttachButton, cardPad, clipStart)
    val barStart = chromeInsets.barStart
    val textStart = chromeInsets.textStart
    val textEnd = chromeInsets.textEnd
    val columnWidthPx = (boxWidthPx - with(density) { (barStart + textStart + textEnd).toPx() }).coerceAtLeast(charWidth)
    val blockPx = with(density) { 96.dp.toPx() }
    val nameGapPx = with(density) { 2.dp.toPx() }
    val sideGapPx = with(density) { 12.dp.toPx() }
    val nameHeight = nameMeasured.size.height.toFloat().coerceAtLeast(1f)
    val laid = remember(
        editor.pieces,
        columnWidthPx,
        charWidth,
        lineHeightPx,
        blockPx,
        nameGapPx,
        nameHeight,
        sideGapPx,
        widthOf,
    ) {
        layout(
            editor.pieces,
            Metrics(columnWidthPx, charWidth, lineHeightPx, blockPx, nameGapPx, nameHeight, sideGapPx, widthOf),
        )
    }
    val textPad = 18.dp
    val oldestPad = 4.dp
    val toolbarPx = with(density) { toolbar.toPx() }
    val maxHeightPx = toolbarPx + with(density) { (textPad * 2).toPx() } + lineHeightPx * 5f
    val naturalPx = toolbarPx + (laid.height - lineHeightPx).coerceAtLeast(0f)
    val composerHeightPx = min(naturalPx, maxHeightPx)
    val composerHeight = with(density) { composerHeightPx.toDp() }
    val shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    val scroll = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val inputState = remember { ComposerInputState() }
    val view = LocalView.current
    inputState.editor = editor
    inputState.edit = onEdit
    inputState.view = view
    inputState.focused = focused
    var xGoal by remember { mutableFloatStateOf(0f) }
    val overflows = naturalPx > maxHeightPx + 1f
    val bottomPadPx = ((toolbarPx - lineHeightPx) / 2f).coerceAtLeast(0f)
    val oldestPadPx = if (overflows) with(density) { oldestPad.toPx() } else 0f
    val bodyTop = if (overflows) oldestPadPx else (composerHeightPx - bottomPadPx - laid.height).coerceAtLeast(0f)
    LaunchedEffect(composerHeight, bottomInset) { onHeight(composerHeight + bottomInset + floatGap) }
    LaunchedEffect(focused) {
        if (focused) focusRequester.requestFocus() else focusManager.clearFocus()
    }
    LaunchedEffect(editor.caret, editor.anchor, editor.composition, focused, laid, overflows) {
        if (!focused) return@LaunchedEffect
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val comp = editor.composition
        imm.updateSelection(view, editor.anchor, editor.caret, comp?.first ?: -1, comp?.second ?: -1)
        if (!overflows) {
            if (scroll.value != 0) scroll.scrollTo(0)
            return@LaunchedEffect
        }
        val (_, caretY) = caretPosition(laid, editor.caret, charWidth)
        val desired = composerHeightPx - bottomPadPx - lineHeightPx
        val maxScroll = (oldestPadPx + laid.height + bottomPadPx - composerHeightPx).coerceAtLeast(0f)
        scroll.scrollTo((caretY + oldestPadPx - desired).coerceIn(0f, maxScroll).toInt())
    }

    fun focusNow() {
        focusRequester.requestFocus()
        showImeNow(view)
        onFocus()
    }

    fun editCaret(index: Int) {
        xGoal = caretPosition(laid, index, charWidth).first
        onEdit { current, _ -> current.setCaret(index) }
        focusNow()
    }

    Box(
        modifier
            .padding(start = screenPad, end = 16.dp, bottom = floatGap)
            .onSizeChanged { boxWidthPx = it.width.toFloat() }
            .height(composerHeight + bottomInset)
            .padding(bottom = bottomInset),
    ) {
        Box(
            Modifier
                .padding(start = barStart)
                .fillMaxSize()
                .clip(shape)
                .background(bar),
        ) {
            val fieldModifier = Modifier
                .fillMaxWidth()
                .padding(start = textStart, end = textEnd)
                .then(if (overflows) Modifier.verticalScroll(scroll) else Modifier)
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.Enter, Key.NumPadEnter -> {
                            onEdit { current, now -> current.commitText("\n", now) }
                            true
                        }
                        Key.Backspace -> {
                            onEdit { current, now -> current.backspace(now) }
                            true
                        }
                        Key.Delete -> {
                            onEdit { current, now -> current.deleteForward(now) }
                            true
                        }
                        Key.DirectionLeft -> {
                            val next = editor.caret - 1
                            xGoal = caretPosition(laid, next, charWidth).first
                            onEdit { current, _ -> current.setCaret(current.caret - 1) }
                            true
                        }
                        Key.DirectionRight -> {
                            val next = editor.caret + 1
                            xGoal = caretPosition(laid, next, charWidth).first
                            onEdit { current, _ -> current.setCaret(current.caret + 1) }
                            true
                        }
                        Key.DirectionUp -> {
                            onEdit { current, _ ->
                                current.setCaret(moveVertical(laid, current.caret, -1, xGoal, charWidth))
                            }
                            true
                        }
                        Key.DirectionDown -> {
                            onEdit { current, _ ->
                                current.setCaret(moveVertical(laid, current.caret, 1, xGoal, charWidth))
                            }
                            true
                        }
                        else -> false
                    }
                }
                .focusable()
                .then(ComposerInputElement(inputState, focused))
                .pointerInput(laid, charWidth, bodyTop, overflows) {
                    val slop = viewConfiguration.touchSlop
                    val longPress = viewConfiguration.longPressTimeoutMillis
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val start = down.position
                        val hitIndex = hit(laid, start.x, start.y - bodyTop, charWidth)
                        var last = down
                        val releasedInside = withTimeoutOrNull(longPress) {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val change = event.changes.first()
                                last = change
                                if (!change.pressed) return@withTimeoutOrNull true
                                if ((change.position - start).getDistance() > slop) return@withTimeoutOrNull false
                            }
                            @Suppress("UNREACHABLE_CODE")
                            false
                        }
                        when (releasedInside) {
                            true -> editCaret(hitIndex)
                            false -> {
                                val delta = last.position - start
                                val scrolling = overflows && last.pressed && abs(delta.y) > abs(delta.x)
                                if (!last.pressed) {
                                    editCaret(hitIndex)
                                } else if (!scrolling) {
                                    focusNow()
                                    onEdit { current, _ -> current.selectRange(hitIndex, hitIndex) }
                                    dragSelection(laid, bodyTop, charWidth, onEdit)
                                }
                            }
                            null -> {
                                focusNow()
                                onEdit { current, _ -> current.selectWord(hitIndex) }
                                dragSelection(laid, bodyTop, charWidth, onEdit)
                            }
                        }
                    }
                }
            val bottomPad = with(density) { bottomPadPx.toDp() }
            val arrow = showsArrow(editor)
            val canSave = editor.timestamp != null
            val save = { if (canSave) onSave() }
            if (overflows) {
                Column(fieldModifier) {
                    Spacer(Modifier.height(oldestPad))
                    DocumentBody(
                        editor, laid, charWidth, lineHeightPx, ink, inkDim,
                        textMeasurer, bodyStyle, hintStyle, nameStyle, true,
                    )
                    Spacer(Modifier.height(bottomPad))
                }
            } else {
                Column(
                    fieldModifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    DocumentBody(
                        editor, laid, charWidth, lineHeightPx, ink, inkDim,
                        textMeasurer, bodyStyle, hintStyle, nameStyle, true,
                    )
                    Spacer(Modifier.height(bottomPad))
                }
            }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = chromeInsets.circleEnd, bottom = (toolbar - AttachButton) / 2)
                    .size(AttachButton)
                    .then(if (canSave) Modifier.background(chrome, CircleShape) else Modifier)
                    .then(if (arrow) Modifier.semantics { contentDescription = "Save" } else Modifier)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = save,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                ArrowUpIcon(tint = if (canSave) bar else chrome, size = glyph)
            }
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF9E9E9E)),
            )
        }
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = clipStart, bottom = (toolbar - AttachButton) / 2),
        ) {
            ComposerHit(onClick = {}, alignment = Alignment.CenterStart) {
                PaperclipIcon(tint = chrome, size = glyph)
            }
        }
    }
}

internal data class ComposerChrome(val barStart: Dp, val textStart: Dp, val textEnd: Dp, val circleEnd: Dp)

internal fun composerChrome(side: Dp, glyph: Dp, circle: Dp, cardPad: Dp, clipStart: Dp): ComposerChrome {
    val circleEnd = side - (circle - glyph) / 2
    val caret = circleEnd + circle + 3.dp
    return ComposerChrome(caret - (side - clipStart), cardPad, caret, circleEnd)
}

private val AttachButton = 40.dp

@Composable
private fun ComposerHit(
    onClick: () -> Unit,
    alignment: Alignment = Alignment.Center,
    icon: @Composable () -> Unit,
) {
    Box(
        Modifier
            .size(AttachButton)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = alignment,
    ) { icon() }
}

private suspend fun AwaitPointerEventScope.dragSelection(
    laid: DocLayout,
    bodyTop: Float,
    charWidth: Float,
    onEdit: (((Editor, Long) -> Editor)) -> Unit,
) {
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.first()
        if (!change.pressed) break
        change.consume()
        val index = hit(laid, change.position.x, change.position.y - bodyTop, charWidth)
        onEdit { current, _ -> current.selectRange(current.anchor, index) }
    }
}

private class ComposerInputState {
    var editor: Editor = com.linote.editor.idleEditor()
    var edit: (((Editor, Long) -> Editor)) -> Unit = {}
    var view: View? = null
    var focused: Boolean = false
}

private class ComposerInputElement(
    val state: ComposerInputState,
    val focused: Boolean,
) : ModifierNodeElement<ComposerInputNode>() {
    override fun create() = ComposerInputNode(state)
    override fun update(node: ComposerInputNode) {
        node.state = state
        node.ensureSession()
    }
    override fun equals(other: Any?) =
        other is ComposerInputElement && other.state === state && other.focused == focused
    override fun hashCode() = 31 * state.hashCode() + focused.hashCode()
}

private fun showImeNow(view: View) {
    val activity = activityOf(view)
    if (activity != null) {
        WindowCompat.getInsetsController(activity.window, view).show(WindowInsetsCompat.Type.ime())
        return
    }
    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, 0)
}

private fun activityOf(view: View): Activity? {
    var context = view.context
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

private class ComposerInputNode(
    var state: ComposerInputState,
) : Modifier.Node(), PlatformTextInputModifierNode {
    private var job: Job? = null

    override fun onAttach() {
        ensureSession()
    }

    override fun onDetach() {
        job?.cancel()
        job = null
    }

    fun ensureSession() {
        if (!isAttached) return
        if (!state.focused) {
            job?.cancel()
            job = null
            state.view?.let { view ->
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            return
        }
        if (job?.isActive == true) return
        job = coroutineScope.launch {
            establishTextInputSession {
                val sessionView = view
                state.view = sessionView
                showImeNow(sessionView)
                startInputMethod { outAttrs ->
                    outAttrs.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                    outAttrs.initialSelStart = state.editor.anchor
                    outAttrs.initialSelEnd = state.editor.caret
                    DocumentInputConnection(state, sessionView)
                }
            }
        }
    }
}

private class DocumentInputConnection(
    private val state: ComposerInputState,
    view: View,
) : BaseInputConnection(view, true) {
    private fun apply(block: (Editor, Long) -> Editor) {
        state.edit { editor, now -> block(editor, now).also { state.editor = it } }
        val sessionView = state.view ?: return
        val editor = state.editor
        val imm = sessionView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val comp = editor.composition
        imm.updateSelection(
            sessionView,
            editor.anchor,
            editor.caret,
            comp?.first ?: -1,
            comp?.second ?: -1,
        )
    }

    override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
        apply { editor, now -> editor.commitText(text?.toString().orEmpty(), now) }
        return true
    }

    override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
        apply { editor, now -> editor.setComposing(text?.toString().orEmpty(), now) }
        return true
    }

    override fun finishComposingText(): Boolean {
        apply { editor, _ -> editor.finishComposing() }
        return true
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        apply { editor, now ->
            if (editor.anchor != editor.caret) {
                editor.backspace(now)
            } else {
                val cursor = editor.caret.coerceIn(0, editor.flat().length)
                val from = (cursor - beforeLength).coerceAtLeast(0)
                val to = (cursor + afterLength).coerceAtMost(editor.flat().length)
                editor.selectRange(from, to).backspace(now)
            }
        }
        return true
    }

    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean =
        deleteSurroundingText(beforeLength, afterLength)

    override fun setSelection(start: Int, end: Int): Boolean {
        apply { editor, _ -> editor.selectRange(start, end) }
        return true
    }

    override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence {
        val editor = state.editor
        val flat = editor.flat()
        val cursor = editor.caret.coerceIn(0, flat.length)
        return flat.substring((cursor - n).coerceAtLeast(0), cursor)
    }

    override fun getTextAfterCursor(n: Int, flags: Int): CharSequence {
        val editor = state.editor
        val flat = editor.flat()
        val cursor = editor.caret.coerceIn(0, flat.length)
        return flat.substring(cursor, (cursor + n).coerceAtMost(flat.length))
    }

    override fun getSelectedText(flags: Int): CharSequence? {
        val editor = state.editor
        val start = min(editor.anchor, editor.caret)
        val end = max(editor.anchor, editor.caret)
        if (start == end) return null
        return editor.flat().substring(start, end)
    }

    override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText {
        val editor = state.editor
        return ExtractedText().apply {
            text = editor.flat()
            startOffset = 0
            partialStartOffset = -1
            partialEndOffset = -1
            selectionStart = editor.anchor
            selectionEnd = editor.caret
        }
    }
}
