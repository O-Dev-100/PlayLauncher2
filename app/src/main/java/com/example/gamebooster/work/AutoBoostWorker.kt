package com.example.gamebooster.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.gamebooster.viewmodel.BoosterViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gamebooster.R
import com.example.gamebooster.MainActivity

class AutoBoostWorker @Inject constructor(
    @ApplicationContext context: Context,
    params: WorkerParameters,
    private val viewModel: BoosterViewModel
) : Worker(context, params) {

    override fun doWork(): Result {
        showReminderNotification()
        return Result.success()
    }

    private fun showReminderNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "gamebooster_reminder"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_power)
            .setContentTitle(applicationContext.getString(R.string.reminder_title))
            .setContentText(applicationContext.getString(R.string.reminder_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_power, applicationContext.getString(R.string.reminder_action), pendingIntent)
            .build()
        notificationManager.notify(1001, notification)
    }
}