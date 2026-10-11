package com.stillwellbeing.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A minimal bar chart: each value in [values] is 0f..1f of the chart's
 * height, drawn centred in its own equal-width slot so axis labels placed in
 * the same slots (a Row of weight-1 items) line up exactly under the bars.
 *
 * Interactive when [onSelect] is given: tapping, or dragging a finger across
 * the chart, reports the slot under the finger. Selection state lives with the
 * caller ([selectedIndex]) so a readout elsewhere can follow it.
 *
 * [averageFraction], when given, draws a dashed horizontal line at that
 * height on the same 0f..1f scale as the bars.
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
    barCornerRadius: Dp = 6.dp,
    averageFraction: Float? = null,
    averageColor: Color = selectedColor,
    onSelect: ((Int) -> Unit)? = null
) {
    val currentOnSelect by rememberUpdatedState(onSelect)

    val gestures = if (onSelect != null && values.isNotEmpty()) {
        Modifier
            .pointerInput(values.size) {
                detectTapGestures { offset ->
                    currentOnSelect?.invoke(slotAt(offset.x, size.width.toFloat(), values.size))
                }
            }
            .pointerInput(values.size) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        currentOnSelect?.invoke(slotAt(offset.x, size.width.toFloat(), values.size))
                    },
                    onHorizontalDrag = { change, _ ->
                        currentOnSelect?.invoke(slotAt(change.position.x, size.width.toFloat(), values.size))
                    }
                )
            }
    } else {
        Modifier
    }

    Canvas(modifier = modifier.fillMaxWidth().height(height).then(gestures)) {
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

        val slotWidth = size.width / values.size
        val gapPx = barGap.toPx().coerceAtMost(slotWidth * 0.6f)
        val barWidth = slotWidth - gapPx
        val cornerPx = barCornerRadius.toPx()

        values.forEachIndexed { index, fraction ->
            val barHeight = size.height * fraction.coerceIn(0f, 1f)
            drawRoundRect(
                color = if (index == selectedIndex) selectedColor else barColor,
                topLeft = Offset(index * slotWidth + gapPx / 2, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(cornerPx, cornerPx)
            )
        }

        drawLine(
            color = gridColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )

        averageFraction?.let { fraction ->
            val lineY = size.height * (1f - fraction.coerceIn(0f, 1f))
            drawLine(
                color = averageColor,
                start = Offset(0f, lineY),
                end = Offset(size.width, lineY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx()))
            )
        }
    }
}

/** Which of [count] equal-width slots a touch at [x] (in a [width]-wide chart) falls in. */
private fun slotAt(x: Float, width: Float, count: Int): Int =
    (x / (width / count)).toInt().coerceIn(0, count - 1)
