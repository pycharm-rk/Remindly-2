package com.rishi.remindly

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rishi.remindly.data.ReminderDatabase
import com.rishi.remindly.ui.RemindlyApp
import com.rishi.remindly.ui.theme.RemindlyTheme
import com.rishi.remindly.viewmodel.ReminderViewModel
import com.rishi.remindly.viewmodel.ReminderViewModelFactory

class MainActivity : ComponentActivity() {

    private val notificationsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled silently */ }

    private val viewModel: ReminderViewModel by viewModels {
        ReminderViewModelFactory(
            ReminderDatabase.getDatabase(applicationContext).reminderDao(),
            applicationContext
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            RemindlyTheme {
                var showAlarmPermissionPrompt by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    val alarmManager = getSystemService(AlarmManager::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        !alarmManager.canScheduleExactAlarms()
                    ) {
                        showAlarmPermissionPrompt = true
                    }
                }

                RemindlyApp(
                    viewModel = viewModel,
                    showAlarmPermissionPrompt = showAlarmPermissionPrompt,
                    onGrantExactAlarm = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            startActivity(
                                Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:$packageName")
                                )
                            )
                        }
                        showAlarmPermissionPrompt = false
                    }
                )
            }
        }
    }
}
