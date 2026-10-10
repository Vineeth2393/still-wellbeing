package com.stillwellbeing.app.ui.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stillwellbeing.app.StillApplication
import com.stillwellbeing.app.data.settings.UserPreferences
import com.stillwellbeing.app.domain.RecordWindow
import com.stillwellbeing.app.domain.ThemeChoice
import com.stillwellbeing.app.ui.components.Eyebrow
import com.stillwellbeing.app.ui.components.SectionTitle
import com.stillwellbeing.app.ui.components.TintedCard
import com.stillwellbeing.app.ui.theme.LocalStillPalette
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as StillApplication
    val viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SettingsViewModel(app.settingsRepository, app.trackingRepository) }
        }
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val palette = LocalStillPalette.current
    val prefs by viewModel.preferences.collectAsState()
    val hasUsageAccess by viewModel.hasUsageAccess.collectAsState()
    val historyCleared by viewModel.historyCleared.collectAsState()

    var editingGoal by remember { mutableStateOf<GoalKind?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Eyebrow("Your pace, your preferences")
        Text("Make it yours", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
        Text("A quiet space for simple choices.", style = MaterialTheme.typography.bodyMedium, color = palette.muted)
        Spacer(Modifier.height(20.dp))

        TintedCard(backgroundColor = palette.sage, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (hasUsageAccess) "● Tracking is active" else "○ Usage access not granted",
                style = MaterialTheme.typography.titleMedium,
                color = palette.ink
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (hasUsageAccess) "Usage access enabled"
                else "Still needs usage access to count pickups and app time. Open the Today tab to grant it.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted
            )
        }

        SettingsSection(title = "Daily goals") {
            SettingRow(
                title = "Pickup target",
                value = prefs.goals.pickupGoal?.let { "$it / day" } ?: "Not set",
                onClick = { editingGoal = GoalKind.Pickups }
            )
            SettingRow(
                title = "Screen-time target",
                value = prefs.goals.screenTimeGoalMinutes?.let { formatMinutesAsHours(it) } ?: "Not set",
                onClick = { editingGoal = GoalKind.ScreenTime }
            )
            SettingRow(
                title = "Phone-free break target",
                value = prefs.goals.breakGoalMinutes?.let { formatMinutesAsHours(it) } ?: "Not set",
                onClick = { editingGoal = GoalKind.Break },
                showDivider = false
            )
        }
        Text(
            "Optional goals — not app blocking or enforced limits.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
            modifier = Modifier.padding(top = 8.dp)
        )

        SettingsSection(title = "Breaks & tracking") {
            Column(modifier = Modifier.padding(15.dp)) {
                Text("Record window", style = MaterialTheme.typography.bodyMedium, color = palette.ink)
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    RecordWindow.entries.forEachIndexed { index, window ->
                        SegmentedButton(
                            selected = prefs.recordWindow == window,
                            onClick = { viewModel.setRecordWindow(window) },
                            shape = SegmentedButtonDefaults.itemShape(index, RecordWindow.entries.size)
                        ) {
                            Text(if (window == RecordWindow.ALL_DAY) "All day" else "Waking hours")
                        }
                    }
                }
                if (prefs.recordWindow == RecordWindow.WAKING_HOURS) {
                    Spacer(Modifier.height(10.dp))
                    WakingHoursPicker(
                        startMinute = prefs.wakingStartMinute,
                        endMinute = prefs.wakingEndMinute,
                        onChange = viewModel::setWakingHours
                    )
                    Text(
                        "Waking hours are what you choose here — not detected sleep.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.muted,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        SettingsSection(title = "Today screen") {
            Column(modifier = Modifier.padding(15.dp)) {
                Text("Apps shown in \"Time in your apps\"", style = MaterialTheme.typography.bodyMedium, color = palette.ink)
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    UserPreferences.APP_LIST_SIZE_OPTIONS.forEachIndexed { index, size ->
                        SegmentedButton(
                            selected = prefs.appListSize == size,
                            onClick = { viewModel.setAppListSize(size) },
                            shape = SegmentedButtonDefaults.itemShape(index, UserPreferences.APP_LIST_SIZE_OPTIONS.size)
                        ) {
                            Text(if (size == UserPreferences.ALL_APPS) "All" else "$size")
                        }
                    }
                }
                Text(
                    "Apps beyond this number are added up under \"Other apps\".",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        SettingsSection(title = "Appearance") {
            Column(modifier = Modifier.padding(15.dp)) {
                Text("Theme", style = MaterialTheme.typography.bodyMedium, color = palette.ink)
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ThemeChoice.entries.forEachIndexed { index, choice ->
                        SegmentedButton(
                            selected = prefs.themeChoice == choice,
                            onClick = { viewModel.setThemeChoice(choice) },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemeChoice.entries.size)
                        ) {
                            Text(
                                when (choice) {
                                    ThemeChoice.SYSTEM -> "System"
                                    ThemeChoice.LIGHT -> "Light"
                                    ThemeChoice.DARK -> "Dark"
                                }
                            )
                        }
                    }
                }
            }
        }

        SettingsSection(title = "Your data stays yours") {
            Column(modifier = Modifier.padding(15.dp)) {
                Text(
                    "Everything Still records lives only on this device — no account, no cloud, no ads.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (historyCleared) "History deleted" else "Delete history")
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    editingGoal?.let { kind ->
        GoalEditDialog(
            kind = kind,
            currentValue = when (kind) {
                GoalKind.Pickups -> prefs.goals.pickupGoal
                GoalKind.ScreenTime -> prefs.goals.screenTimeGoalMinutes
                GoalKind.Break -> prefs.goals.breakGoalMinutes
            },
            onConfirm = { value ->
                when (kind) {
                    GoalKind.Pickups -> viewModel.setPickupGoal(value)
                    GoalKind.ScreenTime -> viewModel.setScreenTimeGoalMinutes(value)
                    GoalKind.Break -> viewModel.setBreakGoalMinutes(value)
                }
                editingGoal = null
            },
            onDismiss = { editingGoal = null }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete history?") },
            text = {
                Text(
                    "This removes every day Still has recorded on this device — pickups, breaks, and " +
                        "records. It can't be undone. Android's own usage data is unaffected."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHistory()
                    showDeleteConfirm = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

private enum class GoalKind { Pickups, ScreenTime, Break }

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    val palette = LocalStillPalette.current
    Spacer(Modifier.height(20.dp))
    SectionTitle(title)
    Spacer(Modifier.height(10.dp))
    TintedCard(backgroundColor = palette.card, modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
        content()
    }
}

@Composable
private fun SettingRow(title: String, value: String, onClick: () -> Unit, showDivider: Boolean = true) {
    val palette = LocalStillPalette.current
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = palette.ink)
            Text(value, style = MaterialTheme.typography.bodySmall, color = palette.muted)
        }
        if (showDivider) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.lineSoft)
            )
        }
    }
}

@Composable
private fun WakingHoursPicker(startMinute: Int, endMinute: Int, onChange: (Int, Int) -> Unit) {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = {
            val start = LocalTime.ofSecondOfDay((startMinute * 60L).coerceIn(0, 86399))
            TimePickerDialog(
                context,
                { _, hour, minute -> onChange(hour * 60 + minute, endMinute) },
                start.hour, start.minute, false
            ).show()
        }) { Text("From ${formatMinuteOfDay(startMinute)}") }

        OutlinedButton(onClick = {
            val end = LocalTime.ofSecondOfDay((endMinute * 60L).coerceIn(0, 86399))
            TimePickerDialog(
                context,
                { _, hour, minute -> onChange(startMinute, hour * 60 + minute) },
                end.hour, end.minute, false
            ).show()
        }) { Text("To ${formatMinuteOfDay(endMinute)}") }
    }
}

@Composable
private fun GoalEditDialog(
    kind: GoalKind,
    currentValue: Int?,
    onConfirm: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentValue?.toString() ?: "") }
    val title = when (kind) {
        GoalKind.Pickups -> "Pickup target"
        GoalKind.ScreenTime -> "Screen-time target (minutes)"
        GoalKind.Break -> "Phone-free break target (minutes)"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { input -> if (input.all(Char::isDigit)) text = input },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.toIntOrNull()?.takeIf { it > 0 }) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(null) }) { Text("Clear goal") }
        }
    )
}

private val hourFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

private fun formatMinuteOfDay(minute: Int): String =
    hourFormatter.format(LocalTime.ofSecondOfDay((minute * 60L).coerceIn(0, 86399)))

private fun formatMinutesAsHours(minutes: Int): String {
    val hours = minutes / 60
    val remaining = minutes % 60
    return when {
        hours <= 0 -> "${minutes}m"
        remaining == 0 -> "${hours}h"
        else -> "${hours}h ${remaining}m"
    }
}
