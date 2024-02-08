package com.geekglasses.wordy.service.scheduler.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.geekglasses.wordy.R
import com.geekglasses.wordy.activity.QuizActivity
import com.geekglasses.wordy.db.DataBaseHelper
import com.geekglasses.wordy.service.quiz.QuizDataResolver
import java.util.concurrent.TimeUnit

class NotificationScheduler(context: Context, workerParams: WorkerParameters, private val dbHelper: DataBaseHelper) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        sendNotification()
        scheduleNextNotification()
        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification() {
        val channelId = "quiz_notification_channel"
        val notificationId = 1

        val intent = QuizActivity.createIntent(applicationContext,
            QuizDataResolver.resolveQuizData(dbHelper), 3
        )

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Quiz Time!")
            .setContentText("Please, take a quiz.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Quiz Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, builder.build())
    }

    private fun scheduleNextNotification() {
        val workRequest = OneTimeWorkRequestBuilder<NotificationScheduler>()
            .setInitialDelay(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }
}
