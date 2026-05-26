package com.mudassir.videodownloader.ui.trimmer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudassir.videodownloader.R
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.ControlBg
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.HandleBlue
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.OverlayDim
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.PlayheadRed
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.TextWhite
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.WaveformActive
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.WaveformBg
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.WaveformDim
import kotlin.math.roundToInt


@Composable
fun TimeControlRow(state: TrimmerState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        TimeAdjuster(
            label  = state.msToShort(state.startMs),
            onMinus = {
                state.pushUndo()
                state.startMs = (state.startMs - 1_000L).coerceAtLeast(0L)
            },
            onPlus  = {
                state.pushUndo()
                state.startMs = (state.startMs + 1_000L).coerceAtMost(state.endMs - 1_000L)
            }
        )

        Text(
            text       = state.msToFullDisplay(state.selectedMs),
            color      = TextWhite,
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold
        )

        TimeAdjuster(
            label   = state.msToShort(state.endMs),
            onMinus = {
                state.pushUndo()
                state.endMs = (state.endMs - 1_000L).coerceAtLeast(state.startMs + 1_000L)
            },
            onPlus  = {
                state.pushUndo()
                state.endMs = (state.endMs + 1_000L).coerceAtMost(state.totalMs)
            }
        )
    }
}

@Composable
fun TimeAdjuster(
    label: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ControlBg)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Minus button
        SmallCircleButton(label = "−", onClick = onMinus)

        // Time label
        Text(
            text      = label,
            color     = TextWhite,
            fontSize  = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier  = Modifier.widthIn(min = 34.dp),
            textAlign = TextAlign.Center
        )

        // Plus button
        SmallCircleButton(label = "+", onClick = onPlus)
    }
}

@Composable
fun SmallCircleButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Color(0xFF3A3A4A))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            color      = TextWhite,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 16.sp
        )
    }
}


@Composable
fun ZoomUndoRow(
    state: TrimmerState
){

    Row(
        modifier=Modifier
            .fillMaxWidth()
            .padding(horizontal=28.dp),

        horizontalArrangement=Arrangement.SpaceBetween
    ) {

        Row(
            horizontalArrangement=Arrangement.spacedBy(10.dp)
        ){

            SquareIconButton(
                icon= R.drawable.zoom_out,
                enabled=state.zoomLevel>1f
            ){
                state.zoomLevel=
                    (state.zoomLevel-.5f)
                        .coerceAtLeast(1f)
            }


            SquareIconButton(
                icon=R.drawable.zoom_in,
                enabled=state.zoomLevel<4f
            ){
                state.zoomLevel=
                    (state.zoomLevel+.5f)
                        .coerceAtMost(4f)
            }

        }


        Row(
            horizontalArrangement=Arrangement.spacedBy(10.dp)
        ){

            SquareIconButton(
                icon= R.drawable.ic_undo,
                enabled=state.undoStack.isNotEmpty()
            ){
                state.undo()
            }


            SquareIconButton(
                icon=R.drawable.ic_redo,
                enabled=state.redoStack.isNotEmpty()
            ){
                state.redo()
            }

        }

    }

}

@Composable
fun SquareIconButton(
    icon:Int,
    enabled:Boolean=true,
    onClick:()->Unit
){
    Box(
        modifier=Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ControlBg)
            .alpha(if(enabled)1f else .35f)
            .clickable(
                enabled=enabled,
                indication=null,
                interactionSource= remember {
                    MutableInteractionSource()
                }
            ){
                onClick()
            },
        contentAlignment=Alignment.Center
    ){

        Icon(
            painter=painterResource(icon),
            contentDescription=null,
            tint=Color.White,
            modifier=Modifier.size(20.dp)
        )

    }
}


@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onToggle: () -> Unit
) {

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue   = 1f,
        targetValue    = if (isPlaying) 1.07f else 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_pulse"
    )

    Box(
        modifier = Modifier
            .size(70.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(

            painter=
                painterResource(

                    if(isPlaying)
                        R.drawable.ic_pause
                    else
                        R.drawable.ic_play

                ),

            contentDescription=null,

            tint=Color.Black,

            modifier=Modifier.size(34.dp)

        )
    }
}

@Composable
fun PickAudioButton(
    hasFile: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (hasFile) Color(0xFF2A4A2A) else Color(0xFF2A2A3A))
            .border(
                width = 1.dp,
                color = if (hasFile) Color(0xFF4CAF50) else Color(0xFF444455),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            painter = painterResource(
                id = R.drawable.ic_pick_audio
            ),
            contentDescription = "Pick audio",
            tint               = if (hasFile) Color(0xFF4CAF50) else TextWhite,
            modifier           = Modifier.size(14.dp)
        )
        Text(
            text      = if (hasFile) "Change" else "Pick Audio",
            color     = if (hasFile) Color(0xFF4CAF50) else TextWhite,
            fontSize  = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun SaveButton(
    enabled: Boolean,
    isSaving: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(46.dp),
        shape  = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = Color(0xFF4FC3F7),
            disabledContainerColor = Color(0xFF2A2A3A)
        )
    ) {
        if (isSaving) {
            // Spinner while saving
            CircularProgressIndicator(
                color     = Color.White,
                modifier  = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
            Text("Saving...", color = Color.White, fontWeight = FontWeight.SemiBold)
        } else {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_save_to_device
                ),
                contentDescription = null,
                tint               = if (enabled) Color.Black else Color(0xFF666666),
                modifier           = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = "Save Trimmed Audio",
                color      = if (enabled) Color.Black else Color(0xFF666666),
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp
            )
        }
    }
}

@Composable
fun WaveformEditor(
    state: TrimmerState,
    waveformData: List<Float>,
    onDrag: () -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasWidthPx by remember { mutableFloatStateOf(1f) }

    fun msToFrac(ms: Long) = (ms.toFloat() / state.totalMs).coerceIn(0f, 1f)
    fun pxToMs(px: Float): Long =
        ((px / canvasWidthPx) * state.totalMs).toLong().coerceIn(0L, state.totalMs)

    Box(
        modifier = modifier
            .onGloballyPositioned { canvasWidthPx = it.size.width.toFloat() }
            .background(WaveformBg)
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w       = size.width
            val h       = size.height
            val rulerH  = 24.dp.toPx()
            val waveTop = rulerH + 6.dp.toPx()
            val waveH   = h - waveTop - 8.dp.toPx()
            val waveMidY = waveTop + waveH / 2f

            val startX = msToFrac(state.startMs) * w
            val endX   = msToFrac(state.endMs)   * w
            val playX  = msToFrac(state.playheadMs) * w

            // Ruler ticks
            drawTimeRuler(width = w, height = rulerH, totalMs = state.totalMs, tickCount = 10)

            // Dim overlays
            drawRect(color = OverlayDim, topLeft = Offset(0f, waveTop), size = Size(startX, waveH))
            drawRect(color = OverlayDim, topLeft = Offset(endX, waveTop), size = Size(w - endX, waveH))

            // Waveform bars uses REAL data passed in
            val barCount = waveformData.size
            if (barCount > 0) {
                val totalBarW = w * state.zoomLevel
                val barW      = totalBarW / barCount
                val gapW      = (barW * 0.3f).coerceAtLeast(1f)
                val drawW     = (barW - gapW).coerceAtLeast(1f)

                waveformData.forEachIndexed { index, amplitude ->
                    val barX    = index * barW
                    val barMs   = ((index.toFloat() / barCount) * state.totalMs).toLong()
                    val inRange = barMs in state.startMs..state.endMs
                    val barH    = amplitude * (waveH * 0.85f)
                    val color   = if (inRange) WaveformActive else WaveformDim

                    drawRoundRect(
                        color        = color,
                        topLeft      = Offset(barX + gapW / 2f, waveMidY - barH / 2f),
                        size         = Size(drawW, barH),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )
                }
            }


            drawLine(color = PlayheadRed, start = Offset(playX, waveTop), end = Offset(playX, waveTop + waveH), strokeWidth = 2.dp.toPx())
            drawCircle(color = PlayheadRed, radius = 5.dp.toPx(), center = Offset(playX, waveTop))
            drawCircle(color = PlayheadRed, radius = 5.dp.toPx(), center = Offset(playX, waveTop + waveH))

            // Trim handles
            drawTrimHandle(x = startX, top = waveTop, bottom = waveTop + waveH, color = HandleBlue, isLeft = true,  strokeWidth = 2.dp.toPx())
            drawTrimHandle(x = endX,   top = waveTop, bottom = waveTop + waveH, color = HandleBlue, isLeft = false, strokeWidth = 2.dp.toPx())

            // Middle handle
            if (state.trimMode == TrimMode.TRIM_MIDDLE) {
                val midX = (startX + endX) / 2f
                drawCircle(color = HandleBlue, radius = 14.dp.toPx(), center = Offset(midX, waveMidY))
                val a = 5.dp.toPx()
                drawLine(Color.White, Offset(midX - a/2f, waveMidY - a), Offset(midX + a/2f, waveMidY), 2.dp.toPx())
                drawLine(Color.White, Offset(midX + a/2f, waveMidY), Offset(midX - a/2f, waveMidY + a), 2.dp.toPx())
            }
        }

        // Left handle drag zone
        Box(
            modifier = Modifier
                .fillMaxHeight().width(44.dp)
                .offset {
                    IntOffset(x = (msToFrac(state.startMs) * canvasWidthPx - 22.dp.toPx()).roundToInt(), y = 0)
                }
                .pointerInput(Unit) {
                    detectDragGestures(onDragStart = { state.pushUndo() }) { change, delta ->
                        change.consume()
                        val newMs = pxToMs(msToFrac(state.startMs) * canvasWidthPx + delta.x)
                        state.startMs    = newMs.coerceIn(0L, state.endMs - 500L)
                        state.playheadMs = state.startMs
                        onDrag()
                    }
                }
        )

        // Right handle drag zone
        Box(
            modifier = Modifier
                .fillMaxHeight().width(44.dp)
                .offset {
                    IntOffset(x = (msToFrac(state.endMs) * canvasWidthPx - 22.dp.toPx()).roundToInt(), y = 0)
                }
                .pointerInput(Unit) {
                    detectDragGestures(onDragStart = { state.pushUndo() }) { change, delta ->
                        change.consume()
                        val newMs = pxToMs(msToFrac(state.endMs) * canvasWidthPx + delta.x)
                        state.endMs = newMs.coerceIn(state.startMs + 500L, state.totalMs)
                        onDrag()
                    }
                }
        )

        // Middle handle drag zone
        if (state.trimMode == TrimMode.TRIM_MIDDLE) {
            val midFrac = (msToFrac(state.startMs) + msToFrac(state.endMs)) / 2f
            Box(
                modifier = Modifier.size(44.dp)
                    .offset {
                        IntOffset(x = (midFrac * canvasWidthPx - 22.dp.toPx()).roundToInt(), y = 130)
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(onDragStart = { state.pushUndo() }) { change, delta ->
                            change.consume()
                            val rangeDuration = state.endMs - state.startMs
                            val newMidMs      = pxToMs(midFrac * canvasWidthPx + delta.x)
                            val newStart      = (newMidMs - rangeDuration / 2L).coerceIn(0L, state.totalMs - rangeDuration)
                            state.startMs     = newStart
                            state.endMs       = newStart + rangeDuration
                            state.playheadMs  = newStart
                            onDrag()
                        }
                    }
            )
        }
    }
}


@Composable
fun WaveformSkeleton(modifier: Modifier = Modifier) {
    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val offset by shimmer.animateFloat(
        initialValue  = -300f,
        targetValue   = 1200f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label         = "shimmer_offset"
    )

    Box(
        modifier = modifier.background(WaveformBg),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w    = size.width
            val h    = size.height
            val midY = h / 2f

            val shimmerBrush = Brush.linearGradient(
                colors = listOf(WaveformDim, WaveformDim.copy(alpha = 0.3f), WaveformDim),
                start  = Offset(offset, 0f),
                end    = Offset(offset + 400f, 0f)
            )

            val barCount = 60
            val barW     = w / barCount
            repeat(barCount) { i ->
                val barH = ((i % 5 + 1) / 5f * h * 0.4f)
                drawRoundRect(
                    brush        = shimmerBrush,
                    topLeft      = Offset(i * barW + 2f, midY - barH / 2f),
                    size         = Size(barW - 4f, barH),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
        }

        Text(
            text     = "Loading waveform...",
            color    = TextWhite.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun NoAudioPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(
                id = R.drawable.ic_tap_to_select
            ),
            contentDescription = null,
            tint               = TextWhite.copy(alpha = 0.25f),
            modifier           = Modifier.size(56.dp)
        )
        Text(
            text      = "Tap \"Pick Audio\" to select\nan audio file from your device",
            color     = TextWhite.copy(alpha = 0.4f),
            fontSize  = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}