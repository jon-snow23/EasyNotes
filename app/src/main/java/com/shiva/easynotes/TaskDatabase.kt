package com.shiva.easynotes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.shiva.easynotes.Models.TaskItem

@Database(entities = [TaskItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun getDao(): TaskDao

    companion object{
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase{
            if(INSTANCE!=null) return INSTANCE!!

            val instance = Room.databaseBuilder(
                context.applicationContext,
                TaskDatabase::class.java,
                "task_database"
            )
                .allowMainThreadQueries()
                .build()

            INSTANCE = instance
            return instance
        }
    }
}