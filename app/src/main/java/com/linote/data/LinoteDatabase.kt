package com.linote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.UUID

@Database(entities = [Note::class], version = 1)
@TypeConverters(UuidConverters::class)
abstract class LinoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}

fun linoteDatabase(context: Context): LinoteDatabase {
    instance?.let { return it }
    return synchronized(lock) {
        instance ?: Room.databaseBuilder(
            context.applicationContext,
            LinoteDatabase::class.java,
            "linote.db",
        ).build().also { instance = it }
    }
}

@Volatile private var instance: LinoteDatabase? = null
private val lock = Any()

class UuidConverters {
    @TypeConverter
    fun fromUuid(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun toUuid(value: String): UUID = UUID.fromString(value)
}
