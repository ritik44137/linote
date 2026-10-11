package com.linote.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID

@Dao
interface NoteDao {
    // shortcut: offset pages shift one row when a newer note is inserted, switch to a createdAt+uuid cursor if that jump shows up
    @Query("SELECT * FROM Note ORDER BY createdAt DESC, uuid DESC")
    fun pagingSource(): PagingSource<Int, Note>

    @Insert
    fun insert(note: Note)

    // Debug seeder in app/src/debug is the only caller. Remove these with that source set.
    @Insert
    suspend fun insertAll(notes: List<Note>)

    @Query("SELECT uuid FROM Note WHERE uuid IN (:uuids)")
    suspend fun existingUuids(uuids: List<UUID>): List<UUID>

    @Query("DELETE FROM Note WHERE uuid IN (:uuids)")
    suspend fun deleteByUuids(uuids: List<UUID>)
}
