package com.mudassir.videodownloader.ui.trimmer


import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.RulerTick

fun DrawScope.drawTimeRuler(
    width: Float,
    height: Float,
    totalMs: Long,
    tickCount: Int
) {
    val stepMs = totalMs / tickCount

    for (i in 0..tickCount) {
        val ms  = i * stepMs
        val x   = (ms.toFloat() / totalMs) * width

        drawLine(
            color       = RulerTick,
            start       = Offset(x, 0f),
            end         = Offset(x, height * 0.55f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

fun DrawScope.drawTrimHandle(
    x: Float,
    top: Float,
    bottom: Float,
    color: androidx.compose.ui.graphics.Color,
    isLeft: Boolean,
    strokeWidth: Float
) {
    val midY     = (top + bottom) / 2f
    val dotR     = 5.dp.toPx()
    val circleR  = 14.dp.toPx()
    val arrowLen = 5.dp.toPx()

    drawLine(color = color, start = Offset(x, top), end = Offset(x, bottom), strokeWidth = strokeWidth)

    drawCircle(color = color, radius = dotR, center = Offset(x, top))

    drawCircle(color = color, radius = dotR, center = Offset(x, bottom))

    drawCircle(color = color, radius = circleR, center = Offset(x, midY))


    val dir = if (isLeft) -1f else 1f
    drawLine(
        color       = androidx.compose.ui.graphics.Color.White,
        start       = Offset(x + dir * arrowLen / 2f, midY - arrowLen),
        end         = Offset(x - dir * arrowLen / 2f, midY),
        strokeWidth = 2.dp.toPx()
    )
    drawLine(
        color       = androidx.compose.ui.graphics.Color.White,
        start       = Offset(x - dir * arrowLen / 2f, midY),
        end         = Offset(x + dir * arrowLen / 2f, midY + arrowLen),
        strokeWidth = 2.dp.toPx()
    )
}