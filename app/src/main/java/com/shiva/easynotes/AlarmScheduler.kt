package com.shiva.easynotes

import com.shiva.easynotes.Models.TaskItem

interface AlarmScheduler {
    fun schedule(taskItem: TaskItem)
    fun cancel(taskItem: TaskItem)
}