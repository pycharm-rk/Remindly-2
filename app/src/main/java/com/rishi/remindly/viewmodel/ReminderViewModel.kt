package com.rishi.remindly.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rishi.remindly.alarm.ReminderScheduler
import com.rishi.remindly.data.Reminder
import com.rishi.remindly.data.ReminderDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val dao: ReminderDao,
    context: Context
) : ViewModel() {

    private val scheduler = ReminderScheduler(context)

    val reminders = dao.getAllReminders().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun addReminder(
        title: String,
        notes: String,
        triggerAtMillis: Long,
        soundKey: String
    ) {
        viewModelScope.launch {
            val id = dao.insert(
                Reminder(
                    title = title,
                    notes = notes,
                    triggerAtMillis = triggerAtMillis,
                    soundKey = soundKey
                )
            ).toInt()
            scheduler.schedule(
                Reminder(
                    id = id,
                    title = title,
                    notes = notes,
                    triggerAtMillis = triggerAtMillis,
                    soundKey = soundKey
                )
            )
        }
    }

    fun updateReminder(
        reminder: Reminder,
        title: String,
        notes: String,
        triggerAtMillis: Long,
        soundKey: String
    ) {
        viewModelScope.launch {
            scheduler.cancel(reminder)
            val updated = reminder.copy(
                title = title,
                notes = notes,
                triggerAtMillis = triggerAtMillis,
                soundKey = soundKey,
                isCompleted = false
            )
            dao.update(updated)
            scheduler.schedule(updated)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            scheduler.cancel(reminder)
            dao.delete(reminder)
        }
    }

    fun markDone(reminder: Reminder) {
        viewModelScope.launch {
            scheduler.cancel(reminder)
            dao.update(reminder.copy(isCompleted = true))
        }
    }

    fun markUndone(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isCompleted = false)
            dao.update(updated)
            if (updated.triggerAtMillis > System.currentTimeMillis()) {
                scheduler.schedule(updated)
            }
        }
    }
}

class ReminderViewModelFactory(
    private val dao: ReminderDao,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReminderViewModel(dao, context) as T
    }
}
