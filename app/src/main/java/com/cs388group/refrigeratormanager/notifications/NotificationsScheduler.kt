package com.cs388group.refrigeratormanager.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationsScheduler {

    private const val UNIQUE_WORK_NAME = "daily_expiration_notifications"

    fun scheduleDailyExpirationCheck(
        context: Context,
        groupId: String,
        thresholdDays: Int = 2
    ) {
        val inputData = Data.Builder()
            .putString(NotificationsWorker.KEY_GROUP_ID, groupId)
            .putInt(NotificationsWorker.KEY_THRESHOLD_DAYS, thresholdDays)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<NotificationsWorker>(
            1, TimeUnit.DAYS
        )
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}