package com.stillwellbeing.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stillwellbeing.app.ui.theme.EyebrowStyle
import com.stillwellbeing.app.ui.theme.LocalStillPalette

/** The rounded, tinted container used for the hero, break, and weekly cards. */
@Composable
fun TintedCard(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.ui.unit.Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(24.dp))
            .padding(contentPadding),
        content = content
    )
}

/** Small uppercase label above a section or card, e.g. "PHONE PICKUPS". */
@Composable
fun Eyebrow(text: String, color: Color = LocalStillPalette.current.muted, modifier: Modifier = Modifier) {
    Text(text = text.uppercase(), style = EyebrowStyle, color = color, modifier = modifier)
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = LocalStillPalette.current.ink,
        modifier = modifier
    )
}
