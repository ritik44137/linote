package com.linote

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.linote.data.DraftStore
import com.linote.data.Note
import com.linote.data.NoteDao
import com.linote.data.linoteDatabase
import com.linote.editor.Editor
import com.linote.editor.finishInsert
import com.linote.editor.idleEditor
import com.linote.editor.sameDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

data class Screen(val editor: Editor, val focused: Boolean)

class NotesViewModel(
    private val dao: NoteDao,
    private val drafts: DraftStore,
) : ViewModel() {
    private val _screen = MutableStateFlow(Screen(drafts.load(), focused = false))
    val screen: StateFlow<Screen> = _screen.asStateFlow()
    private val saving = AtomicBoolean(false)
    private val editorLock = Any()
    private val draftWrites = Mutex()
    private val draftEpoch = AtomicLong()

    val notes: Flow<PagingData<Note>> = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 60, enablePlaceholders = false),
        pagingSourceFactory = dao::pagingSource,
    ).flow.cachedIn(viewModelScope)

    fun edit(now: Long = System.currentTimeMillis(), block: (Editor, Long) -> Editor) {
        val pending = synchronized(editorLock) {
            val next = block(_screen.value.editor, now)
            _screen.value = _screen.value.copy(editor = next)
            draftEpoch.incrementAndGet() to next
        }
        viewModelScope.launch(Dispatchers.IO) { writeDraft(pending.first, pending.second) }
    }

    fun focus() {
        _screen.value = _screen.value.copy(focused = true)
    }

    fun blur() {
        _screen.value = _screen.value.copy(focused = false)
    }

    fun save() {
        val snapshot = _screen.value.editor
        if (snapshot.timestamp == null) return
        if (!saving.compareAndSet(false, true)) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val next = finishInsert(snapshot) { dao.insert(it) }
                if (next == snapshot) return@launch
                val pending = synchronized(editorLock) {
                    if (sameDocument(_screen.value.editor, snapshot)) {
                        _screen.value = Screen(next, focused = true)
                        draftEpoch.incrementAndGet() to next
                    } else {
                        null
                    }
                } ?: return@launch
                writeDraft(pending.first, pending.second)
            } finally {
                saving.set(false)
            }
        }
    }

    // epoch is taken under editorLock so a slower write cannot replace a newer draft
    private suspend fun writeDraft(epoch: Long, editor: Editor) {
        draftWrites.withLock {
            if (epoch == draftEpoch.get()) drafts.save(editor)
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = context.applicationContext
                @Suppress("UNCHECKED_CAST")
                return NotesViewModel(
                    linoteDatabase(app).noteDao(),
                    DraftStore(app.filesDir),
                ) as T
            }
        }
    }
}
