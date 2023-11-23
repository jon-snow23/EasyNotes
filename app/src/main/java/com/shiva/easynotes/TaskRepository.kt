package com.shiva.easynotes

import androidx.lifecycle.LiveData
import com.shiva.easynotes.Models.TaskItem

class TaskRepository(db: TaskDatabase){

    private val dao = db.getDao()

    var allTasks: LiveData<List<TaskItem>> = dao.getAllTasks()

    suspend fun updateTask(task: TaskItem) {
        dao.updateTask(task)
    }

    suspend fun addNewTask(task: TaskItem){
        dao.addNewTask(task)
    }
}