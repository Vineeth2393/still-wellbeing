package com.stillwellbeing.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A minimal bar chart: each value in [values] is 0f..1f of the chart's
 * height. Drawn on [Canvas] rather than laid out with weighted Boxes so the
 * faint horizontal grid lines can be drawn once behind everything, matching
 * the design's repeating-gradient grid.
 */
@Composable
fun BarChart(
    values: List<Float>,
    barColor: Color,
    selectedColor: Color,
    gridColor: Color,
    selectedIndex: Int?,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
    barGap: Dp = 10.dp,
    barCornerRadius: Dp = 6.dp
) {
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        if (values.isEmpty()) return@Canvas

        // Faint grid lines every ~35dp, matching the mockup's repeating gradient.
        val gridStepPx = 35.dp.toPx()
        var y = size.height
        while (y > 0) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
            y -= gridStepPx
        }

        val gapPx = barGap.toPx()
        val barWidth = (size.width - gapPx * (values.size - 1)) / values.size
        val cornerPx = barCornerRadius.toPx()

        values.forEachIndexed { index, fraction ->
            val barHeight = size.height * fraction.coerceIn(0f, 1f)
            val left = index * (barWidth + gapPx)
            val top = size.height - barHeight
            drawRoundRect(
                color = if (index == selectedIndex) selectedColor else barColor,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = CornerRadius(cornerPx, cornerPx)
            )
        }

        drawLine(
            color = gridColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )
    }
}
