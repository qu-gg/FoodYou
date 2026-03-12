package com.maksimowiczm.foodyou.app.ui.home.weighttracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maksimowiczm.foodyou.weighttracker.domain.entity.WeightEntry
import kotlin.math.roundToInt

@Composable
internal fun WeightLineChart(
    entries: List<WeightEntry>,
    goalWeight: Double?,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 160.dp,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    goalColor: Color = MaterialTheme.colorScheme.tertiary,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    labelStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    ),
    goalLabelStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.tertiary,
    ),
    dotColor: Color = MaterialTheme.colorScheme.primary,
    unitLabel: String = "",
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxWidth().height(chartHeight)) {
        if (entries.isEmpty()) return@Canvas

        val weights = entries.map { it.weightKg }
        val allValues = if (goalWeight != null) weights + goalWeight else weights
        val minWeight = allValues.min()
        val maxWeight = allValues.max()

        // Add padding to min/max for visual breathing room
        val range = (maxWeight - minWeight).coerceAtLeast(1.0)
        val paddedMin = minWeight - range * 0.1
        val paddedMax = maxWeight + range * 0.1
        val paddedRange = paddedMax - paddedMin

        val leftPadding = 48f
        val rightPadding = 16f
        val topPadding = 16f
        val bottomPadding = 24f

        val chartWidth = size.width - leftPadding - rightPadding
        val chartHeight = size.height - topPadding - bottomPadding

        // Draw horizontal grid lines and labels
        val gridLineCount = 4
        for (i in 0..gridLineCount) {
            val fraction = i.toFloat() / gridLineCount
            val y = topPadding + chartHeight * fraction
            val weight = paddedMax - paddedRange * fraction

            drawLine(
                color = gridColor,
                start = Offset(leftPadding, y),
                end = Offset(size.width - rightPadding, y),
                strokeWidth = 1f,
            )

            val label = "${weight.roundToInt()}"
            val result = textMeasurer.measure(label, labelStyle)
            drawText(
                textLayoutResult = result,
                topLeft = Offset(
                    x = leftPadding - result.size.width - 8f,
                    y = y - result.size.height / 2f,
                ),
            )
        }

        // Draw goal weight line
        if (goalWeight != null) {
            val goalY =
                topPadding + chartHeight * (1f - ((goalWeight - paddedMin) / paddedRange).toFloat())

            drawLine(
                color = goalColor,
                start = Offset(leftPadding, goalY),
                end = Offset(size.width - rightPadding, goalY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
            )

            val goalLabel = "Goal: ${goalWeight.roundToInt()}$unitLabel"
            val goalResult = textMeasurer.measure(goalLabel, goalLabelStyle)
            drawText(
                textLayoutResult = goalResult,
                topLeft = Offset(
                    x = size.width - rightPadding - goalResult.size.width,
                    y = goalY - goalResult.size.height - 4f,
                ),
            )
        }

        if (entries.size == 1) {
            // Single point — draw a dot at center
            val x = leftPadding + chartWidth / 2f
            val y =
                topPadding + chartHeight * (1f - ((weights[0] - paddedMin) / paddedRange).toFloat())
            drawCircle(color = dotColor, radius = 6f, center = Offset(x, y))
            return@Canvas
        }

        // Map entries to coordinates
        val firstEpochDay = entries.first().date.toEpochDays()
        val lastEpochDay = entries.last().date.toEpochDays()
        val dayRange = (lastEpochDay - firstEpochDay).coerceAtLeast(1)

        val points = entries.map { entry ->
            val xFraction = (entry.date.toEpochDays() - firstEpochDay).toFloat() / dayRange
            val yFraction = ((entry.weightKg - paddedMin) / paddedRange).toFloat()
            Offset(
                x = leftPadding + chartWidth * xFraction,
                y = topPadding + chartHeight * (1f - yFraction),
            )
        }

        // Draw line
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        // Draw dots
        for (point in points) {
            drawCircle(
                color = dotColor,
                radius = 4f,
                center = point,
            )
        }
    }
}
