package com.rishi.remindly.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rishi.remindly.data.Reminder
import com.rishi.remindly.ui.model.SoundOption
import com.rishi.remindly.ui.theme.*
import com.rishi.remindly.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

// ─── Root composable ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindlyApp(
    viewModel: ReminderViewModel,
    showAlarmPermissionPrompt: Boolean,
    onGrantExactAlarm: () -> Unit
) {
    val reminders by viewModel.reminders.collectAsStateCompat()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog    by remember { mutableStateOf(false) }
    var editTarget       by remember { mutableStateOf<Reminder?>(null) }
    var deleteTarget     by remember { mutableStateOf<Reminder?>(null) }
    var selectedTab      by remember { mutableIntStateOf(0) }

    // Alarm permission prompt
    if (showAlarmPermissionPrompt) {
        AlertDialog(
            onDismissRequest = {},
            icon  = { Icon(Icons.Default.Alarm, contentDescription = null, tint = TeamsPrimary) },
            title = { Text("Allow exact reminders") },
            text  = {
                Text("Please grant 'Alarms & reminders' permission so your reminders ring exactly at the scheduled time.")
            },
            confirmButton = {
                Button(onClick = onGrantExactAlarm) { Text("Grant access") }
            }
        )
    }

    // Delete confirmation
    deleteTarget?.let { reminder ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            icon  = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = TeamsError) },
            title = { Text("Delete reminder?") },
            text  = { Text("\"${reminder.title}\" will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteReminder(reminder); deleteTarget = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = TeamsError)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick            = { showAddDialog = true },
                containerColor     = TeamsPrimary,
                contentColor       = Color.White,
                shape              = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.AddAlarm, contentDescription = "Add reminder")
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Top header ──────────────────────────────────────────────
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Remindly",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 22.sp,
                                color      = TeamsPrimary
                            )
                            Text(
                                "Your offline reminder companion",
                                fontSize = 12.sp,
                                color    = TeamsMuted
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        val upcoming = reminders.count { !it.isCompleted }
                        if (upcoming > 0) {
                            Badge(containerColor = TeamsPrimary) {
                                Text("$upcoming", color = Color.White, fontSize = 11.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                        }
                    }
                )

                // ── Tabs ────────────────────────────────────────────────────
                val tabs = listOf("Upcoming", "Completed")
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor   = Color.Transparent,
                    contentColor     = TeamsPrimary,
                    modifier         = Modifier.padding(horizontal = 16.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick  = { selectedTab = index },
                            text     = { Text(title, fontWeight = FontWeight.Medium) }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── List ────────────────────────────────────────────────────
                val upcoming  = reminders.filter { !it.isCompleted }
                    .sortedBy { it.triggerAtMillis }
                val completed = reminders.filter { it.isCompleted }
                    .sortedByDescending { it.triggerAtMillis }

                val displayList = if (selectedTab == 0) upcoming else completed

                if (displayList.isEmpty()) {
                    EmptyState(isCompleted = selectedTab == 1)
                } else {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayList, key = { it.id }) { reminder ->
                            ReminderCard(
                                reminder  = reminder,
                                onDone    = { viewModel.markDone(reminder) },
                                onUndone  = { viewModel.markUndone(reminder) },
                                onDelete  = { deleteTarget = reminder },
                                onEdit    = { editTarget  = reminder }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) } // FAB clearance
                    }
                }
            }
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────
    if (showAddDialog) {
        AddEditReminderDialog(
            existing  = null,
            onDismiss = { showAddDialog = false },
            onSave    = { title, notes, time, soundKey ->
                viewModel.addReminder(title, notes, time, soundKey)
                showAddDialog = false
            }
        )
    }

    editTarget?.let { reminder ->
        AddEditReminderDialog(
            existing  = reminder,
            onDismiss = { editTarget = null },
            onSave    = { title, notes, time, soundKey ->
                viewModel.updateReminder(reminder, title, notes, time, soundKey)
                editTarget = null
            }
        )
    }
}

// ─── Reminder card ────────────────────────────────────────────────────────────

@Composable
private fun ReminderCard(
    reminder : Reminder,
    onDone   : () -> Unit,
    onUndone : () -> Unit,
    onDelete : () -> Unit,
    onEdit   : () -> Unit
) {
    val now      = System.currentTimeMillis()
    val overdue  = !reminder.isCompleted && reminder.triggerAtMillis < now
    val accentColor = when {
        reminder.isCompleted -> TeamsSuccess
        overdue              -> TeamsWarning
        else                 -> TeamsPrimary
    }

    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.Top
        ) {
            // Colour indicator strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text           = reminder.title,
                    style          = MaterialTheme.typography.titleMedium,
                    fontWeight     = FontWeight.SemiBold,
                    maxLines       = 2,
                    overflow       = TextOverflow.Ellipsis,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                    color          = if (reminder.isCompleted) TeamsMuted else MaterialTheme.colorScheme.onSurface
                )

                if (reminder.notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = reminder.notes,
                        color    = TeamsMuted,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint               = accentColor,
                        modifier           = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text      = formatDateTime(reminder.triggerAtMillis),
                        color     = accentColor,
                        fontSize  = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint               = TeamsMuted,
                        modifier           = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text     = SoundOption.fromKey(reminder.soundKey).label,
                        color    = TeamsMuted,
                        fontSize = 12.sp
                    )
                }

                if (overdue) {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        color  = TeamsWarning.copy(alpha = 0.15f),
                        shape  = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "Overdue",
                            color    = TeamsWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!reminder.isCompleted) {
                    IconButton(onClick = onDone, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Mark done", tint = TeamsSuccess)
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TeamsPrimary)
                    }
                } else {
                    IconButton(onClick = onUndone, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Replay, contentDescription = "Restore", tint = TeamsSuccess)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TeamsError)
                }
            }
        }
    }
}

// ─── Empty state ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(isCompleted: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = if (isCompleted) Icons.Default.TaskAlt else Icons.Default.NotificationsNone,
                contentDescription = null,
                tint               = TeamsMuted.copy(alpha = 0.4f),
                modifier           = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text      = if (isCompleted) "No completed reminders yet" else "No upcoming reminders",
                color     = TeamsMuted,
                fontWeight = FontWeight.Medium,
                fontSize  = 16.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text     = if (isCompleted) "Completed reminders appear here" else "Tap + to add your first reminder",
                color    = TeamsMuted.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
    }
}

// ─── Add / Edit dialog ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditReminderDialog(
    existing  : Reminder?,
    onDismiss : () -> Unit,
    onSave    : (String, String, Long, String) -> Unit
) {
    val context = LocalContext.current

    var title         by remember { mutableStateOf(existing?.title ?: "") }
    var notes         by remember { mutableStateOf(existing?.notes ?: "") }
    var selectedTime  by remember {
        mutableLongStateOf(existing?.triggerAtMillis ?: (System.currentTimeMillis() + 60 * 60 * 1000))
    }
    var soundExpanded by remember { mutableStateOf(false) }
    var selectedSound by remember {
        mutableStateOf(SoundOption.fromKey(existing?.soundKey ?: SoundOption.SOFT_BELL.key))
    }
    var titleError    by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }
    LaunchedEffect(selectedTime) { calendar.timeInMillis = selectedTime }

    val openDateTimePicker = {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        selectedTime = calendar.timeInMillis
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (existing == null) "New Reminder" else "Edit Reminder",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Title
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it; titleError = false },
                    label         = { Text("Task title *") },
                    singleLine    = true,
                    isError       = titleError,
                    supportingText = if (titleError) {{ Text("Title is required") }} else null,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp)
                )

                // Notes
                OutlinedTextField(
                    value         = notes,
                    onValueChange = { notes = it },
                    label         = { Text("Notes (optional)") },
                    minLines      = 2,
                    maxLines      = 4,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp)
                )

                // Date & time picker
                Surface(
                    shape        = RoundedCornerShape(12.dp),
                    tonalElevation = 2.dp,
                    modifier     = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment   = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Date & Time", color = TeamsMuted, fontSize = 12.sp)
                            Text(
                                formatDateTime(selectedTime),
                                fontWeight = FontWeight.SemiBold,
                                color      = TeamsPrimary
                            )
                        }
                        TextButton(onClick = openDateTimePicker) {
                            Icon(Icons.Default.EditCalendar, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Change")
                        }
                    }
                }

                // Sound picker
                ExposedDropdownMenuBox(
                    expanded        = soundExpanded,
                    onExpandedChange = { soundExpanded = it }
                ) {
                    OutlinedTextField(
                        modifier      = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly      = true,
                        value         = selectedSound.label,
                        onValueChange = {},
                        label         = { Text("Reminder sound") },
                        leadingIcon   = { Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TeamsPrimary) },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(soundExpanded) },
                        shape         = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded        = soundExpanded,
                        onDismissRequest = { soundExpanded = false }
                    ) {
                        SoundOption.entries.forEach { sound ->
                            DropdownMenuItem(
                                text    = { Text(sound.label) },
                                onClick = { selectedSound = sound; soundExpanded = false },
                                leadingIcon = {
                                    if (sound == selectedSound)
                                        Icon(Icons.Default.Check, contentDescription = null, tint = TeamsPrimary)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isEmpty()) {
                        titleError = true
                    } else {
                        onSave(title.trim(), notes.trim(), selectedTime, selectedSound.key)
                    }
                }
            ) {
                Text(if (existing == null) "Save Reminder" else "Update Reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatDateTime(time: Long): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(time))
