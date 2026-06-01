package com.example.myfridge

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object AlarmScheduler {

    private const val PREFS_NAME = "myfridge_prefs"
    const val KEY_NOTIF_HOUR = "notif_hour"
    const val KEY_NOTIF_MINUTE = "notif_minute"
    const val DEFAULT_HOUR = 17
    const val DEFAULT_MINUTE = 50
    const val KEY_WARN_DAYS = "warn_days"
    const val DEFAULT_WARN_DAYS = 2
    const val KEY_NOTIF_ENABLED = "notifications_enabled"
    private const val WORK_TAG = "daily_expiry_update"

    /**
     * Enqueues (or replaces) the daily expiry-check worker.
     * Pass force = true when the user changes the notification time so the schedule is reset;
     * the default (KEEP) leaves an already-scheduled run untouched.
     */
    fun scheduleDaily(context: Context, force: Boolean = false) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hour = prefs.getInt(KEY_NOTIF_HOUR, DEFAULT_HOUR)
        val minute = prefs.getInt(KEY_NOTIF_MINUTE, DEFAULT_MINUTE)

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<UpdateDaysWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            if (force) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun savePrefs(context: Context, hour: Int, minute: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_NOTIF_HOUR, hour)
            .putInt(KEY_NOTIF_MINUTE, minute)
            .apply()
    }

    fun loadPrefs(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return Pair(
            prefs.getInt(KEY_NOTIF_HOUR, DEFAULT_HOUR),
            prefs.getInt(KEY_NOTIF_MINUTE, DEFAULT_MINUTE)
        )
    }

    fun saveWarnDays(context: Context, days: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_WARN_DAYS, days)
            .apply()
    }

    fun loadWarnDays(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_WARN_DAYS, DEFAULT_WARN_DAYS)

    fun saveNotificationsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NOTIF_ENABLED, enabled)
            .apply()
    }

    fun loadNotificationsEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_NOTIF_ENABLED, true)
}
