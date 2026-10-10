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

/**
 * The 2×1 tile: best break and pickups side by side. A "last updated" line is
 * added underneath only when the launcher gives it enough height, so a short
 * cell never clips it.
 */
class StillMiniWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SHORT, ROOMY))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadWidgetData(context.applicationContext as StillApplication)
        provideContent {
            GlanceTheme {
                MiniContent(data)
            }
        }
    }

    companion object {
        val SHORT = DpSize(110.dp, 40.dp)
        val ROOMY = DpSize(110.dp, 90.dp)
    }
}

// Explicit Sp sizes on every style — see the note beside TitleStyle in StillWidget.kt.
private val MiniLabelStyle = TextStyle(color = ColorProvider(R.color.widget_muted), fontSize = 11.sp)
private val MiniValueStyle = TextStyle(color = ColorProvider(R.color.widget_ink), fontWeight = FontWeight.Medium, fontSize = 18.sp)
private val MiniFooterStyle = TextStyle(color = ColorProvider(R.color.widget_muted), fontSize = 10.sp)

@Composable
private fun MiniContent(data: WidgetData) {
    val context = LocalContext.current
    val showFooter = LocalSize.current.height >= StillMiniWidget.ROOMY.height && data.hasUsageAccess
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.widget_background))
            .cornerRadius(22.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            .padding(10.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        if (!data.hasUsageAccess) {
            Text("still.", style = MiniValueStyle)
            Text("Open Still to turn on tracking", style = MiniLabelStyle)
            return@Column
        }

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text("Best break", style = MiniLabelStyle)
                Text(data.bestBreakText, style = MiniValueStyle)
            }
            Box(modifier = GlanceModifier.width(1.dp).height(38.dp).background(ColorProvider(R.color.widget_line))) {}
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text("Pickups", style = MiniLabelStyle)
                Text(data.pickupsText, style = MiniValueStyle)
            }
        }
        if (showFooter) {
            Spacer(modifier = GlanceModifier.height(6.dp))
            Text(data.updatedAtText, style = MiniFooterStyle)
        }
    }
}
