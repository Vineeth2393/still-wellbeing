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
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.stillwellbeing.app.MainActivity
import com.stillwellbeing.app.R
import com.stillwellbeing.app.StillApplication

/**
 * The 4×1 strip: best break, pickups and the "Updated" time side by side.
 * If the user stretches it taller, it switches to the roomier stacked layout
 * shared with the small widget's wide mode.
 */
class StillWideWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(STRIP, TALL))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadWidgetData(context.applicationContext as StillApplication)
        provideContent {
            GlanceTheme {
                WideWidgetContent(data)
            }
        }
    }

    companion object {
        val STRIP = DpSize(250.dp, 40.dp)
        val TALL = DpSize(250.dp, 150.dp)
    }
}

// Explicit Sp sizes on every style — see the note beside TitleStyle in StillWidget.kt.
private val StripTitleStyle = TextStyle(color = ColorProvider(R.color.widget_ink), fontWeight = FontWeight.Medium, fontSize = 15.sp)
private val StripLabelStyle = TextStyle(color = ColorProvider(R.color.widget_muted), fontSize = 11.sp)
private val StripValueStyle = TextStyle(color = ColorProvider(R.color.widget_ink), fontWeight = FontWeight.Medium, fontSize = 18.sp)

@Composable
private fun WideWidgetContent(data: WidgetData) {
    val size = LocalSize.current
    val context = LocalContext.current
    val isStrip = size.height < StillWideWidget.TALL.height
    val modifier = GlanceModifier
        .fillMaxSize()
        .background(ColorProvider(R.color.widget_background))
        .cornerRadius(24.dp)
        .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
        .padding(if (isStrip) 10.dp else 14.dp)

    if (isStrip) StripLayout(data, modifier) else WideWidget(data, modifier)
}

@Composable
private fun StripLayout(data: WidgetData, modifier: GlanceModifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.Vertical.CenterVertically) {
        if (!data.hasUsageAccess) {
            Text("still.", style = StripTitleStyle)
            Spacer(modifier = GlanceModifier.width(12.dp))
            Text("Open Still to turn on tracking", style = StripLabelStyle)
            return@Row
        }

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text("Best break today", style = StripLabelStyle)
            Text(data.bestBreakText, style = StripValueStyle)
        }
        Box(modifier = GlanceModifier.width(1.dp).fillMaxHeight().background(ColorProvider(R.color.widget_line))) {}
        Spacer(modifier = GlanceModifier.width(12.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text("Pickups today", style = StripLabelStyle)
            Text(data.pickupsText, style = StripValueStyle)
        }
        Column(horizontalAlignment = Alignment.Horizontal.End) {
            Text("still.", style = StripTitleStyle)
            Text(data.updatedAtText.removePrefix("Updated "), style = StripLabelStyle)
        }
    }
}
