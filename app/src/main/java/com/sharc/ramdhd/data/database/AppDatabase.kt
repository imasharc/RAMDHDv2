package com.sharc.ramdhd.data.database
import android.content.Context
import androidx.room.*
import com.sharc.ramdhd.data.dao.NoteDao
import com.sharc.ramdhd.data.dao.RoutineDao
import com.sharc.ramdhd.data.dao.graphTask.GraphTaskDao
import com.sharc.ramdhd.data.model.Note
import com.sharc.ramdhd.data.model.Routine
import com.sharc.ramdhd.data.model.Step
import com.sharc.ramdhd.data.model.graphTask.GraphTask
import com.sharc.ramdhd.data.model.graphTask.GraphStep
import java.time.LocalDateTime
import java.time.ZoneId

@Database(
    entities = [
        Note::class,
        Routine::class,
        Step::class,
        GraphTask::class,
        GraphStep::class
    ],
    version = 6,  // Incremented version for new entities
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun routineDao(): RoutineDao
    abstract fun graphTaskDao(): GraphTaskDao  // Added new DAO

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
                    .fallbackToDestructiveMigration() // This will recreate tables if version changes
                    // For production, you should implement proper migration instead
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

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