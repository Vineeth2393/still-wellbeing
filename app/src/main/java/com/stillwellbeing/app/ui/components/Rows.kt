package com.stillwellbeing.app.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.stillwellbeing.app.ui.theme.LocalStillPalette

/** A single optional-goal row: label, "24 / 40"-style count, and a progress track. */
@Composable
fun GoalRow(label: String, valueText: String, progress: Float, fillColor: Color, modifier: Modifier = Modifier) {
    val palette = LocalStillPalette.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = palette.ink)
            Text(valueText, style = MaterialTheme.typography.bodySmall, color = palette.muted)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = fillColor,
            trackColor = palette.trackBg
        )
    }
}

/** One row in the "time in your apps" list: real launcher icon, name, time, and a relative bar. */
@Composable
fun AppUsageRow(
    packageName: String,
    label: String,
    timeText: String,
    fraction: Float,
    opens: Int,
    modifier: Modifier = Modifier
) {
    val palette = LocalStillPalette.current
    val context = LocalContext.current
    val icon: Drawable? = remember(packageName) {
        runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(palette.cardAlt, RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                val bitmap = remember(icon) { icon.toBitmap(width = 96, height = 96).asImageBitmap() }
                Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(28.dp))
            } else {
                Text(label.take(1), color = palette.ink, style = MaterialTheme.typography.titleMedium)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(timeText, style = MaterialTheme.typography.bodySmall, color = palette.muted)
            }
            if (opens > 0) {
                Text(
                    if (opens == 1) "1 open" else "$opens opens",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted
                )
            }
            LinearProgressIndicator(
                progress = { fraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = palette.accentFill,
                trackColor = palette.trackBg,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}

/** A personal-record card: a big value, its label, and a one-line caption. */
@Composable
fun RecordRow(
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalStillPalette.current
    TintedCard(backgroundColor = palette.card, modifier = modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = palette.muted)
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            color = palette.ink,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Text(caption, style = MaterialTheme.typography.bodySmall, color = palette.muted)
    }
}
