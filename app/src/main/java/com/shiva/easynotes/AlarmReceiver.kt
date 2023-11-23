package com.shiva.easynotes

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("EXTRA_MESSAGE")?:return
        val id = intent.getIntExtra("ALARM_ID", -1)
        if(id==-1) return
        val channelId = "alarm_id"
        context?.let{
            val notificationManager = it.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            val builder = NotificationCompat.Builder(it, channelId)
                .setSmallIcon(android.R.drawable.arrow_up_float)
                .setContentTitle("Easy Notes Alarm")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            notificationManager.notify(id, builder.build())
        }
    }

}