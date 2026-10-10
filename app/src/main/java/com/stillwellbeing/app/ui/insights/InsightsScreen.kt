package com.stillwellbeing.app.ui.insights

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stillwellbeing.app.StillApplication
import com.stillwellbeing.app.domain.DayRecord
import com.stillwellbeing.app.domain.PersonalRecords
import com.stillwellbeing.app.ui.components.BarChart
import com.stillwellbeing.app.ui.components.EmptyState
import com.stillwellbeing.app.ui.components.Eyebrow
import com.stillwellbeing.app.ui.components.RecordRow
import com.stillwellbeing.app.ui.components.SectionTitle
import com.stillwellbeing.app.ui.components.TintedCard
import com.stillwellbeing.app.ui.permission.PermissionScreen
import com.stillwellbeing.app.ui.theme.LocalStillPalette
import com.stillwellbeing.app.util.TimeFormat

@Composable
fun InsightsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as StillApplication
    val viewModel: InsightsViewModel = viewModel(
        factory = viewModelFactory { initializer { InsightsViewModel(app.trackingRepository) } }
    )

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
        InsightsUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LocalStillPalette.current.accent)
        }
        InsightsUiState.NeedsPermission -> PermissionScreen(modifier)
        is InsightsUiState.Loaded -> InsightsContent(state.last7Days, state.records, modifier)
        is InsightsUiState.Failed -> InsightsErrorState(state.message, onRetry = viewModel::refresh, modifier = modifier)
    }
}

@Composable
private fun InsightsErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
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
private fun InsightsContent(
    last7Days: List<DayRecord>,
    records: PersonalRecords,
    modifier: Modifier = Modifier
) {
    val palette = LocalStillPalette.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Eyebrow("Notice your natural rhythm")
        Text("Small changes", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
        Text(
            "Your habits, without the judgment.",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted
        )
        Spacer(Modifier.height(16.dp))

        if (last7Days.isEmpty()) {
            Text(
                "Weekly overview",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.dateBg, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            )
            EmptyState(
                title = "Your week is still filling in",
                message = "Come back after a full day of tracking to see your first trend here."
            )
        } else {
            Text(
                "Weekly overview · ${TimeFormat.monthDay(last7Days.first().date)} – ${TimeFormat.monthDay(last7Days.last().date)}",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.dateBg, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            )
            Spacer(Modifier.height(15.dp))

            TintedCard(backgroundColor = palette.card, modifier = Modifier.fillMaxWidth()) {
                Eyebrow("Daily pickups")
                val average = last7Days.map { it.pickups }.average().let { Math.round(it) }
                Text(
                    "$average",
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.ink,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    "average per day",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted
                )
                Spacer(Modifier.height(12.dp))

                val maxPickups = last7Days.maxOf { it.pickups }.coerceAtLeast(1)
                BarChart(
                    values = last7Days.map { it.pickups / maxPickups.toFloat() },
                    barColor = palette.bar,
                    selectedColor = palette.barSelected,
                    gridColor = palette.chartGrid,
                    selectedIndex = last7Days.lastIndex
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    last7Days.forEach { day ->
                        Text(
                            TimeFormat.weekdayInitial(day.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.muted
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("Personal records")
        Spacer(Modifier.height(10.dp))

        if (records.longestBreak == null && records.fewestPickups == null) {
            EmptyState(
                title = "No records yet",
                message = "Records use complete tracking periods only — check back after your first full day."
            )
        } else {
            records.longestBreak?.let { record ->
                RecordRow(
                    label = "Longest phone-free break",
                    value = TimeFormat.duration(record.longestBreakMs),
                    caption = TimeFormat.monthDay(record.date)
                )
                Spacer(Modifier.height(12.dp))
            }
            records.fewestPickups?.let { record ->
                RecordRow(
                    label = "Fewest pickups in a full day",
                    value = "${record.pickups} pickups",
                    caption = TimeFormat.monthDay(record.date)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Records use complete tracking periods only. Missing data never counts as zero.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted
        )
        Spacer(Modifier.height(24.dp))
    }
}
