package com.stillwellbeing.app.ui.permission

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stillwellbeing.app.ui.theme.LocalStillPalette

/**
 * Shown instead of the app's normal screens whenever usage access isn't
 * granted. "Usage access" is a special permission Android only lets the user
 * flip on from system Settings — there's no runtime dialog we can trigger —
 * so this screen's whole job is explaining why, then deep-linking there.
 */
@Composable
fun PermissionScreen(modifier: Modifier = Modifier) {
    val palette = LocalStillPalette.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "One thing before we start",
            style = MaterialTheme.typography.headlineMedium,
            color = palette.ink,
            textAlign = TextAlign.Center
        )
        Text(
            "Still counts phone pickups and time in each app using Android's " +
                "built-in usage data — the same source Digital Wellbeing uses. " +
                "It never leaves your device.",
            style = MaterialTheme.typography.bodyLarge,
            color = palette.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 28.dp)
        )
        Text(
            "Android only lets you turn this on from Settings directly — tap below, " +
                "then find Still under \"Usage access\" and switch it on.",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(onClick = {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            runCatching { context.startActivity(intent) }
                .onFailure { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        }) {
            Text("Open usage access settings")
        }
    }
}
