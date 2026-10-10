package com.stillwellbeing.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.stillwellbeing.app.MainActivity
import com.stillwellbeing.app.R
import com.stillwellbeing.app.StillApplication
import com.stillwellbeing.app.util.TimeFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Shows two figures that don't require opening the app: today's best
 * completed phone-free break, and today's pickup count — both as of the
 * last refresh, never a live-ticking timer (see the "Updated at…" footer;
 * Android can delay widget refreshes by design).
 */
class StillWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SMALL_SQUARE, WIDE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as StillApplication
        val data = loadWidgetData(app)

        provideContent {
            GlanceTheme {
                WidgetContent(data)
            }
        }
    }

    companion object {
        val SMALL_SQUARE = DpSize(110.dp, 110.dp)
        val WIDE = DpSize(250.dp, 110.dp)
    }
}

internal data class WidgetData(
    val hasUsageAccess: Boolean,
    val pickupsText: String,
    val bestBreakText: String,
    val updatedAtText: String
)

internal suspend fun loadWidgetData(app: StillApplication): WidgetData {
    if (!app.trackingRepository.hasUsageAccess()) {
        return WidgetData(hasUsageAccess = false, pickupsText = "–", bestBreakText = "–", updatedAtText = "")
    }
    val summary = app.trackingRepository.getTodaySummary()
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val updatedAt = Instant.ofEpochMilli(summary.trackingStatus.lastUpdatedMs).atZone(ZoneId.systemDefault())
    return WidgetData(
        hasUsageAccess = true,
        pickupsText = "${summary.pickups}",
        bestBreakText = summary.longestBreakToday?.let { TimeFormat.duration(it.durationMs) } ?: "—",
        updatedAtText = "Updated ${formatter.format(updatedAt)}"
    )
}

// Glance requires an explicit Sp value for every TextStyle — TextUnit.Unspecified
// (including the value TextStyle defaults to when fontSize is left out) throws
// at render time ("Only Sp is currently supported for font sizes"), which fails
// the whole widget with a generic "Can't show content". These four cover every
// text role used below so that mistake can't recur one Text() at a time.
internal val TitleStyle = TextStyle(color = ColorProvider(R.color.widget_ink), fontWeight = FontWeight.Medium, fontSize = 16.sp)
internal val LabelStyle = TextStyle(color = ColorProvider(R.color.widget_muted), fontSize = 12.sp)
internal val ValueStyle = TextStyle(color = ColorProvider(R.color.widget_ink), fontWeight = FontWeight.Medium, fontSize = 20.sp)
internal val FooterStyle = TextStyle(color = ColorProvider(R.color.widget_muted), fontSize = 11.sp)

@Composable
private fun WidgetContent(data: WidgetData) {
    val size = LocalSize.current
    val context = LocalContext.current
    val modifier = GlanceModifier
        .fillMaxSize()
        .background(ColorProvider(R.color.widget_background))
        .cornerRadius(24.dp)
        .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
        .padding(14.dp)

    if (size.width >= StillWidget.WIDE.width) {
        WideWidget(data, modifier)
    } else {
        SmallWidget(data, modifier)
    }
}

@Composable
private fun SmallWidget(data: WidgetData, modifier: GlanceModifier) {
    Column(modifier = modifier) {
        Text("still.", style = TitleStyle)
        Spacer(modifier = GlanceModifier.height(10.dp))

        if (!data.hasUsageAccess) {
            Text("Open Still to turn on tracking", style = LabelStyle)
            return@Column
        }

        Text("Best break today", style = LabelStyle)
        Text(data.bestBreakText, style = ValueStyle)
        Spacer(modifier = GlanceModifier.height(8.dp))
        Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(ColorProvider(R.color.widget_line))) {}
        Spacer(modifier = GlanceModifier.height(8.dp))
        Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.Horizontal.Start) {
            Text("Pickups", style = LabelStyle)
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(data.pickupsText, style = ValueStyle)
        }
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(data.updatedAtText, style = FooterStyle)
    }
}

@Composable
internal fun WideWidget(data: WidgetData, modifier: GlanceModifier) {
    Column(modifier = modifier) {
        Text("A little more breathing room.", style = TitleStyle)
        Spacer(modifier = GlanceModifier.height(10.dp))

        if (!data.hasUsageAccess) {
            Text("Open Still to turn on tracking", style = LabelStyle)
            return@Column
        }

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text("Best break today", style = LabelStyle)
                Text(data.bestBreakText, style = ValueStyle)
            }
            Box(modifier = GlanceModifier.width(1.dp).height(44.dp).background(ColorProvider(R.color.widget_line))) {}
            Spacer(modifier = GlanceModifier.width(14.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text("Pickups today", style = LabelStyle)
                Text(data.pickupsText, style = ValueStyle)
            }
        }
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(data.updatedAtText, style = FooterStyle)
    }
}
