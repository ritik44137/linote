package com.linote.data

import com.linote.editor.commitText
import com.linote.editor.flat
import com.linote.editor.idleEditor
import com.linote.editor.isIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class DraftStoreTest {
    @Test fun save_roundTrips_emptyDeletes_garbageLoadsIdle() {
        val dir = Files.createTempDirectory("draft").toFile()
        val store = DraftStore(dir)
        val editor = idleEditor().commitText("hi", 5)
        store.save(editor)
        assertEquals("hi", store.load().flat())
        assertEquals(5L, store.load().timestamp)
        assertFalse(File(dir, "draft.json.tmp").exists())
        store.save(idleEditor())
        assertFalse(File(dir, "draft.json").exists())
        File(dir, "draft.json").writeText("nope")
        assertTrue(store.load().isIdle())
        store.save(editor)
        assertEquals("hi", store.load().flat())
    }
}
