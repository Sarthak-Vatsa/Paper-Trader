package com.papertrader.app.presentation.watchlist

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp

data class CandleData(
    val date: String,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Long
)

@Composable
fun CandlestickChart(
    data: List<CandleData>,
    modifier: Modifier = Modifier,
    bullColor: Color = Color(0xFF00E676), // Neon Green
    bearColor: Color = Color(0xFFFF5252)  // Neon Red
) {
    if (data.isEmpty()) return

    val maxPrice = data.maxOf { it.high }
    val minPrice = data.minOf { it.low }
    val maxVolume = data.maxOf { it.volume }.coerceAtLeast(1)

    Column(modifier = modifier) {
        // --- 1. Top Section: Candlestick Chart (70% height) ---
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .weight(0.7f)
        ) {
            val width = size.width
            val height = size.height

            val dataSize = data.size
            val candleWidth = width / dataSize.coerceAtLeast(1)
            val padding = candleWidth * 0.2f // Space between candles
            
            // Draw baseline dashes
            val dashedPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            val midY = height / 2
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(0f, midY),
                end = Offset(width, midY),
                strokeWidth = 2f,
                pathEffect = dashedPathEffect
            )

            data.forEachIndexed { index, candle ->
                val x = index * candleWidth + (candleWidth / 2)
                
                // Map price to screen Y coordinates (inverted because Y=0 is top)
                val priceRange = maxPrice - minPrice
                // Add tiny padding to range to avoid divide by zero if max==min
                val range = if (priceRange == 0f) 1f else priceRange
                
                val yOpen = height - ((candle.open - minPrice) / range * height)
                val yClose = height - ((candle.close - minPrice) / range * height)
                val yHigh = height - ((candle.high - minPrice) / range * height)
                val yLow = height - ((candle.low - minPrice) / range * height)

                val color = if (candle.close >= candle.open) bullColor else bearColor

                // Draw Wick
                drawLine(
                    color = color,
                    start = Offset(x, yHigh),
                    end = Offset(x, yLow),
                    strokeWidth = 2f
                )

                // Draw Body
                val bodyTop = minOf(yOpen, yClose)
                val bodyBottom = maxOf(yOpen, yClose)
                // Ensure there's at least 1px body even if open==close
                val rectHeight = maxOf(bodyBottom - bodyTop, 2f)

                drawRect(
                    color = color,
                    topLeft = Offset(x - (candleWidth / 2) + padding, bodyTop),
                    size = Size(candleWidth - (padding * 2), rectHeight)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 2. Bottom Section: Volume Bars (30% height) ---
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .weight(0.3f)
        ) {
            val width = size.width
            val height = size.height
            val dataSize = data.size
            val candleWidth = width / dataSize.coerceAtLeast(1)
            val padding = candleWidth * 0.2f

            data.forEachIndexed { index, candle ->
                val color = if (candle.close >= candle.open) bullColor else bearColor
                
                // Map volume to height
                val barHeight = (candle.volume.toFloat() / maxVolume) * height
                val yTop = height - barHeight
                val xLeft = index * candleWidth + padding

                drawRect(
                    color = color.copy(alpha = 0.8f),
                    topLeft = Offset(xLeft, yTop),
                    size = Size(candleWidth - (padding * 2), barHeight)
                )
            }
        }
    }
}
