package com.cs388group.refrigeratormanager.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cs388group.refrigeratormanager.data.FoodItemRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Calendar
import kotlin.coroutines.resume

class NotificationsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val groupId = inputData.getString(KEY_GROUP_ID) ?: return Result.failure()
        val thresholdDays = inputData.getInt(KEY_THRESHOLD_DAYS, 2)

        val repository = FoodItemRepository()

        return suspendCancellableCoroutine { continuation ->
            repository.getExpiringFoodItems(
                groupId = groupId,
                thresholdDays = thresholdDays,
                onResult = { items ->
                    try {
                        if (items.isNotEmpty()) {
                            NotificationsHelper.createNotificationChannel(applicationContext)

                            val lines = items.take(5).map { item ->
                                val daysLeft = calculateDaysLeft(item.expirationDate)
                                val whenText = when (daysLeft) {
                                    0 -> "expires today"
                                    1 -> "expires tomorrow"
                                    else -> "expires in $daysLeft days"
                                }

                                "${item.name} in ${item.locationName} $whenText"
                            }

                            NotificationsHelper.showNotification(applicationContext, lines)
                        }

                        if (continuation.isActive) {
                            continuation.resume(Result.success())
                        }
                    } catch (e: Exception) {
                        if (continuation.isActive) {
                            continuation.resume(Result.retry())
                        }
                    }
                },
                onFailure = {
                    if (continuation.isActive) {
                        continuation.resume(Result.retry())
                    }
                }
            )
        }
    }

    private fun calculateDaysLeft(expirationTimestamp: Timestamp): Int {
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val expirationCal = Calendar.getInstance().apply {
            time = expirationTimestamp.toDate()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = expirationCal.timeInMillis - todayCal.timeInMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }

    companion object {
        const val KEY_GROUP_ID = "groupId"
        const val KEY_THRESHOLD_DAYS = "thresholdDays"
    }
}