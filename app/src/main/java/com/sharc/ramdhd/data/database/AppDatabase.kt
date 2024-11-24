package com.sharc.ramdhd.data.database

import android.content.Context
import androidx.room.*
import com.sharc.ramdhd.data.dao.*
import com.sharc.ramdhd.data.model.*
import com.sharc.ramdhd.data.model.graphTask.GraphTask
import com.sharc.ramdhd.data.model.graphTask.GraphStep
import com.sharc.ramdhd.data.model.importantPeople.EventType
import com.sharc.ramdhd.data.model.importantPeople.ImportantEvent
import com.sharc.ramdhd.data.model.importantPeople.RecurrenceType
import java.time.LocalDateTime

@Database(
    entities = [
        Note::class,
        Routine::class,
        Step::class,
        GraphTask::class,
        GraphStep::class,
        ImportantEvent::class
    ],
    version = 11,  // Increment version for the new schema
    exportSchema = false
)
@TypeConverters(AppDatabase.Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun routineDao(): RoutineDao
    abstract fun graphTaskDao(): GraphTaskDao
    abstract fun importantEventDao(): ImportantEventDao

    class Converters {
        @TypeConverter
        fun fromTimestamp(value: String?): LocalDateTime? {
            return value?.let { LocalDateTime.parse(it) }
        }

        @TypeConverter
        fun dateToTimestamp(date: LocalDateTime?): String? {
            return date?.toString()
        }

        // Add converters for EventType and RecurrenceType
        @TypeConverter
        fun fromEventType(value: EventType): String = value.name

        @TypeConverter
        fun toEventType(value: String): EventType =
            EventType.valueOf(value)

        @TypeConverter
        fun fromRecurrenceType(value: RecurrenceType): String = value.name

        @TypeConverter
        fun toRecurrenceType(value: String): RecurrenceType =
            RecurrenceType.valueOf(value)
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "RAMDHD.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}