package com.shiva.easynotes

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.shiva.easynotes.Models.TaskItem
import java.time.ZoneId

class AlarmSchedulerImpl(
    private val context: Context
) :AlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ScheduleExactAlarm")
    override fun schedule(taskItem: TaskItem) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", taskItem.message)
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            taskItem.alarmTime!!.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L,
            PendingIntent.getBroadcast(
                context,
                taskItem.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    override fun cancel(taskItem: TaskItem) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                taskItem.id,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}