package com.shiva.easynotes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shiva.easynotes.Models.TaskItem

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNewTask(task: TaskItem)

    @Query("SELECT * FROM tasks ORDER BY id ASC")
    fun getAllTasks(): LiveData<List<TaskItem>>

    @Update
    suspend fun updateTask(task: TaskItem)
}