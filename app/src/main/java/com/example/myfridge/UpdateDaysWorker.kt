package com.example.myfridge

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myfridge.data.Item
import com.example.myfridge.data.ItemDatabase
import com.example.myfridge.data.OfflineItemRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

class UpdateDaysWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val channelId = "myfridge_expiry_channel"

    override suspend fun doWork(): Result {
        ensureNotificationChannel()
        val repository = OfflineItemRepository(ItemDatabase.getDatabase(applicationContext).itemDao())
        val threshold = AlarmScheduler.loadWarnDays(applicationContext)
        val items = repository.getFridgeItemsStream().first()
        for (item in items) {
            if (item.expiryDate) {
                val now = Calendar.getInstance().time.time
                val daysPassed = TimeUnit.DAYS.convert(now - item.date, TimeUnit.MILLISECONDS)
                val updated = item.copy(days = item.days - daysPassed.toInt(), date = now)
                repository.updateItem(updated)
                if (updated.days <= threshold) sendExpiryNotification(updated)
            }
        }
        return Result.success()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Expiry Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Alerts for expiring and expired fridge items" }
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendExpiryNotification(item: Item) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val title = when {
            item.days < 0 -> "Expired"
            item.days == 0 -> "Expiring Today"
            else -> "Expiring Soon"
        }
        val body = when {
            item.days < 0 -> "${item.count} × ${item.name} expired ${-item.days} day${if (-item.days == 1) "" else "s"} ago"
            item.days == 0 -> "${item.count} × ${item.name} expires today"
            else -> "${item.count} × ${item.name} expires in ${item.days} day${if (item.days == 1) "" else "s"}"
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(item.id + 1000, notification)
    }
}
