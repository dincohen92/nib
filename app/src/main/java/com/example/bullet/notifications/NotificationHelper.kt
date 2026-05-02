package com.example.bullet.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.bullet.MainActivity
import com.example.bullet.R

private const val PUSHED_NOTIFICATION_ID = 1001
private const val REMINDER_NOTIFICATION_ID = 1002

fun postPushedTasksNotification(context: Context, count: Int) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notification = NotificationCompat.Builder(context, "nib_pushed")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Tasks pushed to today")
        .setContentText("$count task(s) carried over from yesterday")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(PUSHED_NOTIFICATION_ID, notification)
}

fun postDailyReminderNotification(context: Context, openCount: Int) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val body = if (openCount == 0) "All done for today!" else "$openCount task(s) open today"

    val notification = NotificationCompat.Builder(context, "nib_reminder")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Today's tasks")
        .setContentText(body)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(REMINDER_NOTIFICATION_ID, notification)
}
