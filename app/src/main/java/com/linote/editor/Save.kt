package com.linote.editor

import com.linote.data.Note

fun noteFrom(editor: Editor): Note? {
    val timestamp = editor.timestamp ?: return null
    return Note.create(content = writePieces(editor.pieces), now = timestamp)
}

fun finishInsert(editor: Editor, insert: (Note) -> Unit): Editor {
    val note = noteFrom(editor) ?: return editor
    return try {
        insert(note)
        idleEditor()
    } catch (e: Exception) {
        editor
    }
}

fun sameDocument(a: Editor, b: Editor): Boolean =
    a.pieces == b.pieces && a.timestamp == b.timestamp
