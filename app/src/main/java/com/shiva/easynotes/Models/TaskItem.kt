package com.shiva.easynotes.Models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName="tasks")
data class TaskItem (
    var alarmTime: LocalDateTime?,
    var message: String,
    @PrimaryKey(autoGenerate = false)
    var id: Int
    )