package com.shiva.easynotes

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shiva.easynotes.Models.TaskItem
import kotlinx.coroutines.launch

class TaskViewModel(
    private val application: Application,
    private val repository: TaskRepository,
    private val context: Context
) : AndroidViewModel(application) {

    var savedTasks = repository.allTasks

    fun addNewTask(task: TaskItem){
        viewModelScope.launch{
            repository.addNewTask(task)
        }
    }

    fun updateTask(task: TaskItem){
        viewModelScope.launch{
            repository.updateTask(task)
        }
    }

}