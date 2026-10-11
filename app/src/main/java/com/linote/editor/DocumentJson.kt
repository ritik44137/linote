package com.linote.editor

import java.util.UUID

private val MediaKind.wire: String
    get() = when (this) {
        MediaKind.IMAGE -> "image"
        MediaKind.VIDEO -> "video"
        MediaKind.FILE -> "file"
    }

fun writePieces(pieces: List<Piece>): String = buildString {
    append('[')
    pieces.forEachIndexed { index, piece ->
        if (index > 0) append(',')
        when (piece) {
            is Piece.Text -> append("""{"type":"text","value":${jsonString(piece.value)}}""")
            is Piece.Media -> append(
                """{"type":"media","kind":"${piece.kind.wire}","id":${jsonString(piece.id.toString())},"name":${jsonString(piece.name)},"source":${jsonString(piece.source)}}""",
            )
        }
    }
    append(']')
}

private const val PARSED_NOTES = 128

private val parsedContent = object : LinkedHashMap<String, List<Piece>>(PARSED_NOTES, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<Piece>>) = size > PARSED_NOTES
}

fun readContent(text: String): List<Piece> {
    synchronized(parsedContent) { parsedContent[text] }?.let { return it }
    val pieces = try {
        parsePieces(Cursor(text).parse()) ?: listOf(Piece.Text(text))
    } catch (e: Exception) {
        listOf(Piece.Text(text))
    }
    synchronized(parsedContent) { parsedContent[text] = pieces }
    return pieces
}

fun writeDraft(editor: Editor): String =
    """{"timestamp":${editor.timestamp?.toString() ?: "null"},"pieces":${writePieces(editor.pieces)}}"""

fun readDraft(text: String): Editor? = try {
    val obj = Cursor(text).parse() as? Map<*, *> ?: return null
    if (!obj.containsKey("timestamp") || !obj.containsKey("pieces")) return null
    val timestamp = when (val value = obj["timestamp"]) {
        null -> null
        is Long -> value
        is Int -> value.toLong()
        else -> return null
    }
    val pieces = parsePieces(obj["pieces"]) ?: return null
    val end = Editor(pieces, timestamp, 0, 0, null).flat().length
    Editor(pieces, timestamp, end, end, null)
} catch (e: Exception) {
    null
}

private fun jsonString(value: String): String = buildString {
    append('"')
    for (c in value) when (c) {
        '"' -> append("\\\"")
        '\\' -> append("\\\\")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '\t' -> append("\\t")
        else -> if (c.code < 0x20) append("\\u%04x".format(c.code)) else append(c)
    }
    append('"')
}

private fun parsePieces(value: Any?): List<Piece>? {
    val array = value as? List<*> ?: return null
    val pieces = mutableListOf<Piece>()
    for (element in array) {
        val map = element as? Map<*, *> ?: return null
        when (map["type"]) {
            "text" -> {
                val text = map["value"] as? String ?: return null
                pieces += Piece.Text(text)
            }
            "media" -> {
                val kind = when (map["kind"]) {
                    "image" -> MediaKind.IMAGE
                    "video" -> MediaKind.VIDEO
                    "file" -> MediaKind.FILE
                    else -> return null
                }
                val id = (map["id"] as? String)?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                    ?: return null
                val name = map["name"] as? String ?: return null
                val source = map["source"] as? String ?: return null
                pieces += Piece.Media(id, name, kind, source)
            }
            else -> return null
        }
    }
    return normalize(pieces)
}

private class Cursor(private val s: String) {
    private var i = 0

    fun parse(): Any? {
        skip()
        val value = value()
        skip()
        if (i != s.length) error("trailing")
        return value
    }

    private fun skip() {
        while (i < s.length && s[i].isWhitespace()) i++
    }

    private fun value(): Any? {
        skip()
        if (i >= s.length) error("eof")
        return when (val c = s[i]) {
            '{' -> obj()
            '[' -> arr()
            '"' -> str()
            'n' -> {
                take("null")
                null
            }
            '-', in '0'..'9' -> num()
            else -> error("value $c")
        }
    }

    private fun take(literal: String) {
        if (!s.startsWith(literal, i)) error(literal)
        i += literal.length
    }

    private fun obj(): Map<String, Any?> {
        val map = linkedMapOf<String, Any?>()
        i++
        skip()
        if (i < s.length && s[i] == '}') {
            i++
            return map
        }
        while (true) {
            skip()
            val key = str()
            skip()
            if (i >= s.length || s[i] != ':') error(":")
            i++
            map[key] = value()
            skip()
            if (i >= s.length) error("obj")
            when (s[i]) {
                ',' -> i++
                '}' -> {
                    i++
                    return map
                }
                else -> error("obj")
            }
        }
    }

    private fun arr(): List<Any?> {
        val list = mutableListOf<Any?>()
        i++
        skip()
        if (i < s.length && s[i] == ']') {
            i++
            return list
        }
        while (true) {
            list += value()
            skip()
            if (i >= s.length) error("arr")
            when (s[i]) {
                ',' -> i++
                ']' -> {
                    i++
                    return list
                }
                else -> error("arr")
            }
        }
    }

    private fun str(): String {
        if (i >= s.length || s[i] != '"') error("str")
        i++
        val out = StringBuilder()
        while (i < s.length) {
            when (val c = s[i++]) {
                '"' -> return out.toString()
                '\\' -> out.append(escape())
                else -> out.append(c)
            }
        }
        error("str")
    }

    private fun escape(): Char {
        if (i >= s.length) error("esc")
        return when (val c = s[i++]) {
            '"', '\\', '/' -> c
            'b' -> '\b'
            'f' -> '\u000C'
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            'u' -> {
                if (i + 4 > s.length) error("u")
                val hex = s.substring(i, i + 4)
                i += 4
                hex.toInt(16).toChar()
            }
            else -> error("esc")
        }
    }

    private fun num(): Long {
        val start = i
        if (s[i] == '-') i++
        if (i >= s.length || !s[i].isDigit()) error("num")
        while (i < s.length && s[i].isDigit()) i++
        if (i < s.length && (s[i] == '.' || s[i] == 'e' || s[i] == 'E')) error("int")
        return s.substring(start, i).toLong()
    }
}
