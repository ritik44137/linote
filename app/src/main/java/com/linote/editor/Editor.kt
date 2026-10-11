package com.linote.editor

import java.util.UUID

enum class MediaKind { IMAGE, VIDEO, FILE }

sealed interface Piece {
    data class Text(val value: String) : Piece
    data class Media(
        val id: UUID,
        val name: String,
        val kind: MediaKind,
        val source: String,
    ) : Piece
}

data class Editor(
    val pieces: List<Piece>,
    val timestamp: Long?,
    val anchor: Int,
    val caret: Int,
    val composition: Pair<Int, Int>?,
)

fun idleEditor() = Editor(listOf(Piece.Text("")), null, 0, 0, null)

private sealed interface Atom {
    data class Ch(val c: Char) : Atom
    data class Med(val piece: Piece.Media) : Atom
}

private fun explode(pieces: List<Piece>): List<Atom> = buildList {
    for (piece in pieces) when (piece) {
        is Piece.Text -> piece.value.forEach { add(Atom.Ch(it)) }
        is Piece.Media -> add(Atom.Med(piece))
    }
}

fun normalize(pieces: List<Piece>): List<Piece> {
    if (isNormalized(pieces)) return pieces
    return regroup(explode(pieces))
}

private fun isNormalized(pieces: List<Piece>): Boolean {
    if (pieces.size == 1 && pieces[0] is Piece.Text && (pieces[0] as Piece.Text).value.isEmpty()) return true
    if (pieces.isEmpty()) return false
    var text = false
    for (piece in pieces) when (piece) {
        is Piece.Text -> {
            if (text || piece.value.isEmpty()) return false
            text = true
        }
        is Piece.Media -> text = false
    }
    return true
}

private fun regroup(atoms: List<Atom>): List<Piece> {
    val out = mutableListOf<Piece>()
    val buf = StringBuilder()
    fun flush() {
        if (buf.isNotEmpty()) {
            out += Piece.Text(buf.toString())
            buf.clear()
        }
    }
    for (atom in atoms) when (atom) {
        is Atom.Ch -> buf.append(atom.c)
        is Atom.Med -> {
            flush()
            out += atom.piece
        }
    }
    flush()
    return out.ifEmpty { listOf(Piece.Text("")) }
}

fun Editor.flat(): String {
    val plain = pieces.singleOrNull() as? Piece.Text
    if (plain != null) return plain.value
    return buildString {
        for (piece in pieces) when (piece) {
            is Piece.Text -> append(piece.value)
            is Piece.Media -> append('\uFFFC')
        }
    }
}

fun Editor.isIdle(): Boolean = timestamp == null && pieces == listOf(Piece.Text(""))

private fun Piece.stamps(): Boolean = when (this) {
    is Piece.Media -> true
    is Piece.Text -> value.any { it != ' ' && it != '\n' && it != '\r' }
}

private fun Editor.withTimestamp(now: Long): Editor {
    val on = pieces.any { it.stamps() }
    val ts = when {
        on && timestamp == null -> now
        !on -> null
        else -> timestamp
    }
    return copy(timestamp = ts)
}

private fun Editor.splice(
    start: Int,
    end: Int,
    insert: List<Atom>,
    now: Long,
    composition: Pair<Int, Int>?,
): Editor {
    val plain = pieces.singleOrNull() as? Piece.Text
    if (plain != null && insert.all { it is Atom.Ch }) {
        val from = minOf(start, end).coerceIn(0, plain.value.length)
        val to = maxOf(start, end).coerceIn(from, plain.value.length)
        val next = buildString(plain.value.length - (to - from) + insert.size) {
            append(plain.value, 0, from)
            for (atom in insert) append((atom as Atom.Ch).c)
            append(plain.value, to, plain.value.length)
        }
        val caret = from + insert.size
        val grouped = if (next.isEmpty()) listOf(Piece.Text("")) else listOf(Piece.Text(next))
        return Editor(grouped, timestamp, caret, caret, composition).withTimestamp(now)
    }
    val atoms = explode(pieces).toMutableList()
    val from = minOf(start, end).coerceIn(0, atoms.size)
    val to = maxOf(start, end).coerceIn(from, atoms.size)
    repeat(to - from) { atoms.removeAt(from) }
    atoms.addAll(from, insert)
    val caret = from + insert.size
    return Editor(regroup(atoms), timestamp, caret, caret, composition).withTimestamp(now)
}

fun Editor.commitText(text: String, now: Long): Editor {
    // shortcut: pasted U+FFFC stays a text character; it would alias a media piece in the IME string. Upgrade when the IME buffer can carry piece ids.
    val (from, to) = composition ?: (minOf(anchor, caret) to maxOf(anchor, caret))
    return splice(from, to, text.map { Atom.Ch(it) }, now, null)
}

fun Editor.insertMedia(piece: Piece.Media, now: Long): Editor {
    val editor = finishComposing()
    val from = minOf(editor.anchor, editor.caret)
    val to = maxOf(editor.anchor, editor.caret)
    return editor.splice(from, to, listOf(Atom.Med(piece)), now, null)
}

fun Editor.finishComposing(): Editor = if (composition == null) this else copy(composition = null)

fun Editor.backspace(now: Long): Editor {
    val editor = finishComposing()
    val from = minOf(editor.anchor, editor.caret)
    val to = maxOf(editor.anchor, editor.caret)
    return when {
        from != to -> editor.splice(from, to, emptyList(), now, null)
        from == 0 -> editor
        else -> editor.splice(from - 1, from, emptyList(), now, null)
    }
}

fun Editor.deleteForward(now: Long): Editor {
    val editor = finishComposing()
    val from = minOf(editor.anchor, editor.caret)
    val to = maxOf(editor.anchor, editor.caret)
    return when {
        from != to -> editor.splice(from, to, emptyList(), now, null)
        from >= editor.flat().length -> editor
        else -> editor.splice(from, from + 1, emptyList(), now, null)
    }
}

fun Editor.setComposing(text: String, now: Long): Editor {
    val (from, to) = composition ?: (minOf(anchor, caret) to maxOf(anchor, caret))
    if (pieces.singleOrNull() is Piece.Text) {
        return splice(from, to, text.map { Atom.Ch(it) }, now, from to from + text.length)
    }
    val atoms = explode(pieces)
    val overlapsMedia = (from until to).any { it < atoms.size && atoms[it] is Atom.Med }
    if (overlapsMedia) {
        return finishComposing().splice(from, to, text.map { Atom.Ch(it) }, now, null)
    }
    return splice(from, to, text.map { Atom.Ch(it) }, now, from to from + text.length)
}

fun Editor.setCaret(index: Int): Editor {
    val i = index.coerceIn(0, flat().length)
    return finishComposing().copy(anchor = i, caret = i)
}

fun Editor.selectRange(anchor: Int, caret: Int): Editor {
    val n = flat().length
    return finishComposing().copy(
        anchor = anchor.coerceIn(0, n),
        caret = caret.coerceIn(0, n),
    )
}

fun Editor.selectWord(flatIndex: Int): Editor {
    val atoms = explode(pieces)
    if (flatIndex !in atoms.indices) return setCaret(flatIndex)
    return when (val atom = atoms[flatIndex]) {
        is Atom.Med -> finishComposing().copy(anchor = flatIndex, caret = flatIndex + 1)
        is Atom.Ch -> {
            if (atom.c.isWhitespace()) return setCaret(flatIndex)
            var start = flatIndex
            var end = flatIndex + 1
            while (start > 0 && atoms[start - 1] is Atom.Ch && !(atoms[start - 1] as Atom.Ch).c.isWhitespace()) {
                start--
            }
            while (end < atoms.size && atoms[end] is Atom.Ch && !(atoms[end] as Atom.Ch).c.isWhitespace()) {
                end++
            }
            finishComposing().copy(anchor = start, caret = end)
        }
    }
}

fun showsArrow(editor: Editor): Boolean =
    editor.pieces.any { it is Piece.Media || (it is Piece.Text && it.value.isNotEmpty()) }

fun showsHint(editor: Editor): Boolean = !showsArrow(editor)
