package com.geekglasses.wordy

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.geekglasses.wordy.service.scheduler.notification.NotificationScheduler
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NotificationSchedulerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testWorkRequestEnqueueing() {
        val workRequest = PeriodicWorkRequestBuilder<NotificationScheduler>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "QuizNotificationWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )

        val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork("QuizNotificationWork").get()

        assertEquals(1, workInfos.size)
        assertEquals(workRequest.id, workInfos[0].id)
        assertEquals("com.geekglasses.wordy.service.notification.NotificationScheduler", workInfos[0].tags.first())
    }
}
