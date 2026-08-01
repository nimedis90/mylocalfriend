package my.local.friend.android.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MyLocalFriendApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Setup Notification Channel
        NotificationHelper(this).createNotificationChannel()
        
        // Schedule Daily Work
        scheduleDailyUpdate()
    }

    private fun scheduleDailyUpdate() {
        val calendar = java.util.Calendar.getInstance().apply {
            if (get(java.util.Calendar.HOUR_OF_DAY) >= 9) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            set(java.util.Calendar.HOUR_OF_DAY, 9)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val workRequest = PeriodicWorkRequestBuilder<DailyUpdateWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("daily_update")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyLocalUpdate",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
