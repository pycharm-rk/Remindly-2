package com.rishi.remindly.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rishi.remindly.data.ReminderDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = ReminderDatabase.getDatabase(context).reminderDao()
                val scheduler = ReminderScheduler(context)
                val now = System.currentTimeMillis()
                dao.getAllNow()
                    .filter { !it.isCompleted && it.triggerAtMillis > now }
                    .forEach { scheduler.schedule(it) }
            }
        }
    }
}
