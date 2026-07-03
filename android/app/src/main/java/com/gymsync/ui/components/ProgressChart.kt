package com.gymsync.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymsync.ui.theme.CardShapeSmall
import com.gymsync.ui.theme.SpacingLg
import com.gymsync.ui.theme.SpacingMd

data class ChartDataPoint(
    val label: String,
    val value: Float,
    val date: String = ""
)

@Composable
fun LineChart(
    data: List<ChartDataPoint>,
    title: String,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = remember(data) { data.maxOf { it.value } }
    val minValue = remember(data) { data.minOf { it.value } }
    val range = if (maxValue - minValue == 0f) 1f else maxValue - minValue

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShapeSmall,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(SpacingLg)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(SpacingMd))

            val surfaceColor = MaterialTheme.colorScheme.surface
            val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val width = size.width
                val height = size.height
                val padding = 40f
                val chartWidth = width - padding * 2
                val chartHeight = height - padding * 2

                val textPaint = android.graphics.Paint().apply {
                    color = onSurfaceVariantColor.hashCode()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                val steps = data.size - 1
                if (steps == 0) return@Canvas

                val path = Path()
                data.forEachIndexed { index, point ->
                    val x = padding + (index.toFloat() / steps) * chartWidth
                    val y = padding + chartHeight - ((point.value - minValue) / range) * chartHeight

                    if (index == 0) path.moveTo(x, y)
                    else path.lineTo(x, y)

                    if (index == data.lastIndex) {
                        drawCircle(
                            color = lineColor,
                            radius = 6f,
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = surfaceColor.copy(alpha = 0.8f),
                            radius = 3f,
                            center = Offset(x, y)
                        )
                    }

                    if (index == 0 || index == data.lastIndex) {
                        drawContext.canvas.nativeCanvas.drawText(
                            point.label,
                            x,
                            height - 8f,
                            textPaint
                        )
                    }
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = 3f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                data.forEachIndexed { index, point ->
                    val x = padding + (index.toFloat() / steps) * chartWidth
                    val y = padding + chartHeight - ((point.value - minValue) / range) * chartHeight

                    drawCircle(
                        color = lineColor.copy(alpha = 0.5f),
                        radius = 4f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}
