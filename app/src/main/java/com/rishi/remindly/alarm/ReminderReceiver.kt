package com.rishi.remindly.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rishi.remindly.MainActivity
import com.rishi.remindly.R
import com.rishi.remindly.ui.model.SoundOption

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Reminder"
        val notes = intent.getStringExtra(EXTRA_NOTES).orEmpty()
        val soundKey = intent.getStringExtra(EXTRA_SOUND_KEY) ?: SoundOption.DEFAULT.key
        val reminderId = intent.getIntExtra(EXTRA_ID, 0)

        val channelId = "reminder_$soundKey"
        createChannel(context, channelId, soundKey)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(notes.ifBlank { "Tap to view reminder details." })
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notes.ifBlank { title })
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(reminderId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    private fun createChannel(context: Context, channelId: String, soundKey: String) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) != null) return

        val soundOption = SoundOption.fromKey(soundKey)
        val uri: Uri? = if (soundOption.rawRes != null) {
            Uri.parse("android.resource://${context.packageName}/${soundOption.rawRes}")
        } else null

        val channel = NotificationChannel(
            channelId,
            "Reminder – ${soundOption.label}",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminder notifications with ${soundOption.label} sound"
            enableVibration(true)
            if (uri != null) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(uri, audioAttributes)
            }
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_NOTES = "extra_notes"
        const val EXTRA_SOUND_KEY = "extra_sound_key"
    }
}
