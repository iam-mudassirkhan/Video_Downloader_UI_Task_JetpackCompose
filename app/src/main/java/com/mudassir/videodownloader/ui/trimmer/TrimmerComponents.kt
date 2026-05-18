package com.mudassir.videodownloader.ui.trimmer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.ChipSelected
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.ChipTextOff
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.ChipTextOn
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.ChipUnselected
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.ControlBg
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.HandleBlue
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.OverlayDim
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.PlayheadRed
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.TextWhite
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.WaveformActive
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.WaveformBg
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.WaveformDim
import kotlin.math.PI
import kotlin.math.roundToInt


@Composable
fun TrimModeToggle(
    current: TrimMode,
    onModeSelected: (TrimMode) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

        TrimMode.entries.forEach { mode ->
            val isSelected = current == mode
            val label      = if (mode == TrimMode.TRIM_SIDES) "Trim Sides" else "Trim Middle"


            val bgColor   by animateColorAsState(
                targetValue   = if (isSelected) ChipSelected else ChipUnselected,
                animationSpec = tween(200),
                label         = "chip_bg"
            )
            val textColor by animateColorAsState(
                targetValue   = if (isSelected) ChipTextOn else ChipTextOff,
                animationSpec = tween(200),
                label         = "chip_text"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onModeSelected(mode) }
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = label,
                    color     = textColor,
                    fontSize  = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Composable
fun WaveformEditor(
    state: TrimmerState,
    onDrag: () -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasWidthPx by remember { mutableFloatStateOf(1f) }

    val waveformData = remember {
        val rng = java.util.Random(12345L)
        List(200) { i ->
            val t        = i / 200f
            val envelope = kotlin.math.sin(t * PI.toFloat()) * 0.65f + 0.35f
            (rng.nextFloat() * envelope).coerceIn(0.06f, 1f)
        }
    }


    fun msToFrac(ms: Long) = (ms.toFloat() / state.totalMs).coerceIn(0f, 1f)

    fun pxToMs(px: Float): Long =
        ((px / canvasWidthPx) * state.totalMs).toLong()
            .coerceIn(0L, state.totalMs)

    Box(
        modifier = modifier
            .onGloballyPositioned { canvasWidthPx = it.size.width.toFloat() }
            .background(WaveformBg)
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val rulerH      = 24.dp.toPx()
            val waveTop     = rulerH + 6.dp.toPx()
            val waveH       = h - waveTop - 8.dp.toPx()
            val waveMidY    = waveTop + waveH / 2f

            val startFrac   = msToFrac(state.startMs)
            val endFrac     = msToFrac(state.endMs)
            val startX      = startFrac * w
            val endX        = endFrac   * w
            val playX       = msToFrac(state.playheadMs) * w

            drawTimeRuler(
                width       = w,
                height      = rulerH,
                totalMs     = state.totalMs,
                tickCount   = 10
            )

            drawRect(
                color   = OverlayDim,
                topLeft = Offset(0f, waveTop),
                size    = Size(startX, waveH)
            )

            drawRect(
                color   = OverlayDim,
                topLeft = Offset(endX, waveTop),
                size    = Size(w - endX, waveH)
            )


            val barCount    = waveformData.size
            val totalBarW   = w * state.zoomLevel
            val barW        = totalBarW / barCount
            val gapW        = (barW * 0.3f).coerceAtLeast(1f)
            val drawW       = (barW - gapW).coerceAtLeast(1f)

            waveformData.forEachIndexed { index, amplitude ->
                val barX    = index * barW
                val barMs   = ((index.toFloat() / barCount) * state.totalMs).toLong()
                val inRange = barMs in state.startMs..state.endMs
                val barH =
                    amplitude *
                            (waveH*.65f)
                val color   = if (inRange) WaveformActive else WaveformDim

                drawRoundRect(
                    color        = color,
                    topLeft      = Offset(barX + gapW / 2f, waveMidY - barH / 2f),
                    size         = Size(drawW, barH),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }

            drawLine(
                color       = PlayheadRed,
                start       = Offset(playX, waveTop),
                end         = Offset(playX, waveTop + waveH),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(
                color  = PlayheadRed,
                radius = 5.dp.toPx(),
                center = Offset(playX, waveTop + waveH)
            )

            drawTrimHandle(
                x           = startX,
                top         = waveTop,
                bottom      = waveTop + waveH,
                color       = HandleBlue,
                isLeft      = true,
                strokeWidth = 2.dp.toPx()
            )

            drawTrimHandle(
                x           = endX,
                top         = waveTop,
                bottom      = waveTop + waveH,
                color       = HandleBlue,
                isLeft      = false,
                strokeWidth = 2.dp.toPx()
            )

            if (state.trimMode == TrimMode.TRIM_MIDDLE) {
                val midX = (startX + endX) / 2f
                val midY = waveMidY
                drawCircle(
                    color  = HandleBlue,
                    radius = 14.dp.toPx(),
                    center = Offset(midX, midY)
                )

                val a = 5.dp.toPx()
                drawLine(Color.White, Offset(midX - a / 2f, midY - a), Offset(midX + a / 2f, midY), 2.dp.toPx())
                drawLine(Color.White, Offset(midX + a / 2f, midY), Offset(midX - a / 2f, midY + a), 2.dp.toPx())
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(44.dp)
                .offset {
                    val handleX = (msToFrac(state.startMs) * canvasWidthPx)
                    IntOffset(x = (handleX - 22.dp.toPx()).roundToInt(), y = 0)
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { state.pushUndo() }
                    ) { change, delta ->
                        change.consume()
                        val currentX = msToFrac(state.startMs) * canvasWidthPx
                        val newMs    = pxToMs(currentX + delta.x)
                        state.startMs = newMs.coerceIn(0L, state.endMs - 500L)
                        state.playheadMs = state.startMs
                        onDrag()
                    }
                }
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(44.dp)
                .offset {
                    val handleX = (msToFrac(state.endMs) * canvasWidthPx)
                    IntOffset(x = (handleX - 22.dp.toPx()).roundToInt(), y = 0)
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { state.pushUndo() }
                    ) { change, delta ->
                        change.consume()
                        val currentX = msToFrac(state.endMs) * canvasWidthPx
                        val newMs    = pxToMs(currentX + delta.x)
                        state.endMs  = newMs.coerceIn(state.startMs + 500L, state.totalMs)
                        onDrag()
                    }
                }
        )

        if (state.trimMode == TrimMode.TRIM_MIDDLE) {
            val midFrac = (msToFrac(state.startMs) + msToFrac(state.endMs)) / 2f
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .offset {
                        val midX = midFrac * canvasWidthPx
                        IntOffset(
                            x = (midX - 22.dp.toPx()).roundToInt(),
                            y = 130
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { state.pushUndo() }
                        ) { change, delta ->
                            change.consume()
                            val rangeDuration = state.endMs - state.startMs
                            val midX          = midFrac * canvasWidthPx
                            val newMidMs      = pxToMs(midX + delta.x)
                            val half          = rangeDuration / 2L
                            val newStart      = (newMidMs - half).coerceIn(0L, state.totalMs - rangeDuration)
                            state.startMs     = newStart
                            state.endMs       = newStart + rangeDuration
                            state.playheadMs  = state.startMs
                            onDrag()
                        }
                    }
            )
        }
    }
}



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
