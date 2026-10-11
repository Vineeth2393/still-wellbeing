package com.stillwellbeing.app.ui.insights

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
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
private fun ChartReadout(day: DayRecord) {
    val palette = LocalStillPalette.current
    Column {
        Text(
            TimeFormat.dayLabel(day.date) + if (day.isComplete) "" else " · partial day",
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            ReadoutStat("pickups", "${day.pickups}")
            ReadoutStat("best break", if (day.longestBreakMs > 0) TimeFormat.duration(day.longestBreakMs) else "—")
            ReadoutStat("screen time", if (day.totalScreenTimeMs > 0) TimeFormat.duration(day.totalScreenTimeMs) else "—")
        }
    }
}

@Composable
private fun ReadoutStat(label: String, value: String) {
    val palette = LocalStillPalette.current
    Column {
        Text(value, style = MaterialTheme.typography.titleLarge, color = palette.ink)
        Text(label, style = MaterialTheme.typography.bodySmall, color = palette.muted)
    }
}

/** A dashed swatch matching the chart's average line, plus what it means. */
@Composable
private fun AverageLegend(average: Double, color: Color) {
    val palette = LocalStillPalette.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(Modifier.width(24.dp).height(2.dp)) {
            drawLine(
                color = color,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
            )
        }
        Text(
            "7-day average · ${Math.round(average)} pickups",
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted
        )
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

                var selectedIndex by remember(last7Days) { mutableIntStateOf(last7Days.lastIndex) }
                ChartReadout(last7Days[selectedIndex.coerceIn(0, last7Days.lastIndex)])
                Spacer(Modifier.height(14.dp))

                val maxPickups = last7Days.maxOf { it.pickups }.coerceAtLeast(1)
                val averagePickups = last7Days.map { it.pickups }.average()
                BarChart(
                    values = last7Days.map { it.pickups / maxPickups.toFloat() },
                    barColor = palette.bar,
                    selectedColor = palette.barSelected,
                    gridColor = palette.chartGrid,
                    selectedIndex = selectedIndex,
                    averageFraction = (averagePickups / maxPickups).toFloat(),
                    averageColor = palette.muted,
                    onSelect = { selectedIndex = it }
                )
                // One weight-1 slot per day, matching the chart's own slots, so each
                // letter sits exactly under its bar.
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    last7Days.forEachIndexed { index, day ->
                        Text(
                            TimeFormat.weekdayInitial(day.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (index == selectedIndex) palette.ink else palette.muted,
                            fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                AverageLegend(averagePickups, palette.muted)
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
