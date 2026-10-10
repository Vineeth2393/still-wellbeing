package com.stillwellbeing.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stillwellbeing.app.ui.theme.LocalStillPalette
import com.stillwellbeing.app.util.TimeFormat

/**
 * "Tracking active · Updated 12:41 PM", with an optional line underneath for
 * a partial-tracking day. Never claims a full day of data when there isn't one.
 */
@Composable
fun TrackingStatusRow(
    lastUpdatedMs: Long,
    trackingStartedAtMs: Long?,
    isPartialDay: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalStillPalette.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(6.dp).background(palette.accent, CircleShape)
            )
            Text(
                "Tracking active · Updated ${TimeFormat.clockTime(lastUpdatedMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted
            )
        }
        if (isPartialDay && trackingStartedAtMs != null) {
            Text(
                "Tracking started at ${TimeFormat.clockTime(trackingStartedAtMs)} — today's results cover only part of the day.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/** A quiet "nothing to show yet" block — never a bare zero standing in for missing data. */
@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    val palette = LocalStillPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = palette.ink,
            textAlign = TextAlign.Center
        )
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
