package com.stillwellbeing.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

/** Refreshes every Still widget (mini, small and wide) in one call. */
suspend fun updateAllStillWidgets(context: Context) {
    StillWidget().updateAll(context)
    StillWideWidget().updateAll(context)
    StillMiniWidget().updateAll(context)
}
