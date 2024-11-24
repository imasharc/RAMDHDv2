package com.sharc.ramdhd.data.database

import android.content.Context
import androidx.room.*
import com.sharc.ramdhd.data.dao.*
import com.sharc.ramdhd.data.model.*
import com.sharc.ramdhd.data.model.graphTask.GraphTask
import com.sharc.ramdhd.data.model.graphTask.GraphStep
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
    version = 10,
    exportSchema = false
)
@TypeConverters(AppDatabase.Converters::class)  // Use the inner Converters class
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