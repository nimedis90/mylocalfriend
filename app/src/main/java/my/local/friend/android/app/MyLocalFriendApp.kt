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
        
        // Schedule Triple Daily Updates
        schedulePeriodicUpdate("MorningUpdate", 7, 30)
        schedulePeriodicUpdate("AfternoonUpdate", 13, 0)
        schedulePeriodicUpdate("EveningUpdate", 17, 0)
    }

    private fun schedulePeriodicUpdate(name: String, hour: Int, minute: Int) {
        val calendar = java.util.Calendar.getInstance().apply {
            val now = System.currentTimeMillis()
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            if (timeInMillis <= now) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val workRequest = PeriodicWorkRequestBuilder<DailyUpdateWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("daily_update")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            name,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
