package com.stillwellbeing.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stillwellbeing.app.StillApplication
import com.stillwellbeing.app.domain.Goals
import com.stillwellbeing.app.domain.TodaySummary
import com.stillwellbeing.app.ui.components.AppUsageRow
import com.stillwellbeing.app.ui.components.Eyebrow
import com.stillwellbeing.app.ui.components.EmptyState
import com.stillwellbeing.app.ui.components.GoalRow
import com.stillwellbeing.app.ui.components.SectionTitle
import com.stillwellbeing.app.ui.components.TintedCard
import com.stillwellbeing.app.ui.components.TrackingStatusRow
import com.stillwellbeing.app.ui.permission.PermissionScreen
import com.stillwellbeing.app.ui.theme.BigNumberStyle
import com.stillwellbeing.app.ui.theme.LocalStillPalette
import com.stillwellbeing.app.util.TimeFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(onManageGoals: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as StillApplication
    val viewModel: TodayViewModel = viewModel(
        factory = viewModelFactory {
            initializer { TodayViewModel(app.trackingRepository, app.settingsRepository, app) }
        }
    )

    // Usage stats can change the moment the user returns from granting
    // permission in Settings, or just from using their phone — refresh live.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val uiState by viewModel.uiState.collectAsState()
    when (val state = uiState) {
        TodayUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LocalStillPalette.current.accent)
        }
        TodayUiState.NeedsPermission -> PermissionScreen(modifier)
        is TodayUiState.Loaded -> TodayContent(state, onManageGoals, modifier)
        is TodayUiState.Failed -> ErrorState(state.message, onRetry = viewModel::refresh, modifier = modifier)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalStillPalette.current
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Something went wrong", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted,
            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
        )
        Button(onClick = onRetry) { Text("Try again") }
    }
}

@Composable
private fun TodayContent(
    state: TodayUiState.Loaded,
    onManageGoals: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalStillPalette.current
    val summary = state.summary

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Eyebrow("A little space to breathe")
        Text("Your day", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
        Text(
            DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()).format(summary.date),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted
        )
        Spacer(Modifier.height(16.dp))

        TrackingStatusRow(
            lastUpdatedMs = summary.trackingStatus.lastUpdatedMs,
            trackingStartedAtMs = summary.trackingStatus.trackingStartedAtMs,
            isPartialDay = summary.isPartialDay
        )
        Spacer(Modifier.height(16.dp))

        PickupsHeroCard(summary, state.yesterdayPickups)
        Spacer(Modifier.height(14.dp))

        BreakCard(summary, state.personalBestBreakMs)
        Spacer(Modifier.height(20.dp))

        GoalsSection(state.goals, summary, onManageGoals)
        Spacer(Modifier.height(20.dp))

        AppsSection(summary)
        Spacer(Modifier.height(12.dp))

        Text(
            "A pickup means a phone unlock. Breaks shown here are completed intervals between unlocks.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PickupsHeroCard(summary: TodaySummary, yesterdayPickups: Int?) {
    val palette = LocalStillPalette.current
    TintedCard(backgroundColor = palette.sage, modifier = Modifier.fillMaxWidth()) {
        Eyebrow("Phone pickups")
        Text("${summary.pickups}", style = BigNumberStyle, color = palette.ink)
        yesterdayPickups?.let { yesterday ->
            val diff = yesterday - summary.pickups
            val comparisonText = when {
                diff > 0 -> "$diff fewer than yesterday"
                diff < 0 -> "${-diff} more than yesterday"
                else -> "Same as yesterday"
            }
            Text(
                comparisonText,
                style = MaterialTheme.typography.bodySmall,
                color = palette.chipFg,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .background(palette.chipBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun BreakCard(summary: TodaySummary, personalBestBreakMs: Long?) {
    val palette = LocalStillPalette.current
    TintedCard(backgroundColor = palette.yellow, modifier = Modifier.fillMaxWidth()) {
        Eyebrow("Longest phone-free break today")
        val longest = summary.longestBreakToday
        if (longest != null) {
            Text(
                TimeFormat.duration(longest.durationMs),
                style = MaterialTheme.typography.headlineMedium,
                color = palette.ink,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "${TimeFormat.clockTime(longest.startMs)} – ${TimeFormat.clockTime(longest.endMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted
            )
        } else {
            Text(
                "Not enough data yet today",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        val footerParts = buildList {
            summary.lastCompletedBreak?.let { add("Last break: ${TimeFormat.duration(it.durationMs)}") }
            personalBestBreakMs?.takeIf { it > 0 }?.let { add("Personal best: ${TimeFormat.duration(it)}") }
        }
        if (footerParts.isNotEmpty()) {
            Text(
                footerParts.joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = palette.onAccentSurface,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun GoalsSection(goals: Goals, summary: TodaySummary, onManageGoals: () -> Unit) {
    val palette = LocalStillPalette.current
    val hasAnyGoal = goals.pickupGoal != null || goals.screenTimeGoalMinutes != null || goals.breakGoalMinutes != null

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SectionTitle("Gentle daily goals")
        TextButton(onClick = onManageGoals) { Text("Manage") }
    }

    if (!hasAnyGoal) {
        Text(
            "No goals set yet — these are optional targets, never limits.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted
        )
        return
    }

    TintedCard(backgroundColor = palette.card, modifier = Modifier.fillMaxWidth()) {
        goals.pickupGoal?.let { goal ->
            GoalRow(
                label = "Pickups",
                valueText = "${summary.pickups} / $goal",
                progress = summary.pickups / goal.toFloat(),
                fillColor = palette.accentFill
            )
            Spacer(Modifier.height(15.dp))
        }
        goals.screenTimeGoalMinutes?.let { goalMinutes ->
            GoalRow(
                label = "Screen time",
                valueText = "${TimeFormat.duration(summary.totalScreenTimeMs)} / ${TimeFormat.duration(goalMinutes * 60_000L)}",
                progress = summary.totalScreenTimeMs / (goalMinutes * 60_000f),
                fillColor = palette.blueFill
            )
            Spacer(Modifier.height(15.dp))
        }
        goals.breakGoalMinutes?.let { goalMinutes ->
            val achievedMs = goalMinutes * 60_000L
            val longestMs = summary.longestBreakToday?.durationMs ?: 0L
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Phone-free break", style = MaterialTheme.typography.bodyMedium, color = palette.ink)
                Text(
                    if (longestMs >= achievedMs) "${TimeFormat.duration(achievedMs)} target reached ✓"
                    else "${TimeFormat.duration(longestMs)} / ${TimeFormat.duration(achievedMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted
                )
            }
        }
    }
}

@Composable
private fun AppsSection(summary: TodaySummary) {
    val palette = LocalStillPalette.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        SectionTitle("Time in your apps")
        Text(
            "${TimeFormat.duration(summary.totalScreenTimeMs)} total",
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted
        )
    }
    Spacer(Modifier.height(8.dp))

    if (summary.topApps.isEmpty()) {
        EmptyState(
            title = "No app usage yet today",
            message = "Come back after using a few apps to see them here."
        )
        return
    }

    val maxTime = summary.topApps.maxOf { it.totalTimeMs }.coerceAtLeast(1L)
    summary.topApps.forEach { item ->
        AppUsageRow(
            packageName = item.packageName,
            label = item.label,
            timeText = TimeFormat.duration(item.totalTimeMs),
            fraction = item.totalTimeMs / maxTime.toFloat(),
            opens = item.opens
        )
        Spacer(Modifier.height(6.dp))
    }
    if (summary.otherAppsTimeMs > 0) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Other apps", style = MaterialTheme.typography.bodySmall, color = palette.muted)
            Text(
                TimeFormat.duration(summary.otherAppsTimeMs),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted
            )
        }
    }
}
