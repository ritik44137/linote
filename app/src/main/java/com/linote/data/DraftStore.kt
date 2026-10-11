package com.linote.data

import com.linote.editor.Editor
import com.linote.editor.idleEditor
import com.linote.editor.isIdle
import com.linote.editor.readDraft
import com.linote.editor.writeDraft
import java.io.File

class DraftStore(private val directory: File) {
    private val file = File(directory, "draft.json")

    fun load(): Editor {
        if (!file.exists()) return idleEditor()
        return try {
            readDraft(file.readText()) ?: idleEditor()
        } catch (e: Exception) {
            idleEditor()
        }
    }

    fun save(editor: Editor) {
        val tmp = File(directory, "draft.json.tmp")
        if (editor.isIdle()) {
            file.delete()
            tmp.delete()
            return
        }
        tmp.writeText(writeDraft(editor))
        if (!tmp.renameTo(file)) {
            file.writeText(tmp.readText())
            tmp.delete()
        }
    }
}
