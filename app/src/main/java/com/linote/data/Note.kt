package com.linote.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(indices = [Index("createdAt")])
data class Note(
    @PrimaryKey val uuid: UUID,
    val createdAt: Long,
    val modifiedAt: Long,
    val content: String,
    val title: String?,
    val referencedNoteID: Long?,
) {
    companion object {
        fun create(
            content: String,
            title: String? = null,
            referencedNoteID: Long? = null,
            now: Long = System.currentTimeMillis(),
        ): Note = Note(
            uuid = UuidV7.generate(now),
            createdAt = now,
            modifiedAt = now,
            content = content,
            title = title,
            referencedNoteID = referencedNoteID,
        )
    }
}
