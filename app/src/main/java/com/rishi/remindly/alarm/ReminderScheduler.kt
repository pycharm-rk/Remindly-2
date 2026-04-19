package com.rishi.remindly.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rishi.remindly.data.Reminder

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(reminder: Reminder) {
        val pendingIntent = createPendingIntent(reminder)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (securityException: SecurityException) {
            // Fallback if exact alarm permission is not granted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancel(reminder: Reminder) {
        alarmManager.cancel(createPendingIntent(reminder))
    }

    private fun createPendingIntent(reminder: Reminder): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_ID, reminder.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, reminder.title)
            putExtra(ReminderReceiver.EXTRA_NOTES, reminder.notes)
            putExtra(ReminderReceiver.EXTRA_SOUND_KEY, reminder.soundKey)
        }
        return PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
