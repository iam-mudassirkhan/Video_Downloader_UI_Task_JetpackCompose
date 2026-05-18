
package com.mudassir.videodownloader.ui.trimmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.BgDark
import com.mudassir.videodownloader.utills.AppUtills
import kotlinx.coroutines.delay


enum class TrimMode { TRIM_SIDES, TRIM_MIDDLE }


class TrimmerState(val totalMs: Long = 30_000L) {

    var startMs  by mutableLongStateOf(3_000L)
    var endMs    by mutableLongStateOf(27_000L)

    var playheadMs by mutableLongStateOf(3_000L)


    var isPlaying by mutableStateOf(false)

    var trimMode by mutableStateOf(TrimMode.TRIM_SIDES)

    var zoomLevel by mutableFloatStateOf(1f)

    val undoStack = mutableStateListOf<Pair<Long, Long>>()
    val redoStack = mutableStateListOf<Pair<Long, Long>>()

    val selectedMs get() = endMs - startMs

    fun pushUndo() {
        undoStack.add(Pair(startMs, endMs))
        redoStack.clear()
    }

    fun undo() {

        if(undoStack.isEmpty()) return

        redoStack.add(
            Pair(startMs,endMs)
        )

        val prev=
            undoStack.removeAt(
                undoStack.lastIndex
            )

        startMs=prev.first
        endMs=prev.second
    }


    fun redo(){

        if(redoStack.isEmpty()) return

        undoStack.add(
            Pair(startMs,endMs)
        )

        val next=
            redoStack.removeAt(
                redoStack.lastIndex
            )

        startMs=next.first
        endMs=next.second
    }


    fun msToFullDisplay(ms: Long): String {
        val s   = ms / 1000
        val m   = s / 60
        val sec = s % 60
        val cs  = (ms % 1000) / 10
        return "%02d:%02d:%02d".format(m, sec, cs)
    }

    fun msToShort(ms: Long): String {
        val s   = ms / 1000
        val m   = s / 60
        val sec = s % 60
        return "%d:%02d".format(m, sec)
    }
}


@Composable
fun TrimmerScreen(onBack: () -> Unit = {}) {

    // to get white status bar on dark backgrounds
    AppUtills.WindowInsetsController()

    val state   = remember { TrimmerState() }
    val haptic  = LocalHapticFeedback.current

    LaunchedEffect(state.isPlaying) {
        while (state.isPlaying) {
            delay(100L)
            val next = state.playheadMs + 100L
            if (next >= state.endMs) {
                state.playheadMs = state.startMs   // loop back to start
                state.isPlaying  = false
            } else {
                state.playheadMs = next
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TrimModeToggle(
            current        = state.trimMode,
            onModeSelected = { state.trimMode = it }
        )

        Spacer(Modifier.height(16.dp))

        WaveformEditor(
            state   = state,
            onDrag  = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
               // .weight(1f)
        )

        Spacer(Modifier.height(12.dp))

        TimeControlRow(state = state)

        Spacer(Modifier.height(14.dp))

        ZoomUndoRow(state = state)

        Spacer(Modifier.height(28.dp))

        PlayPauseButton(
            isPlaying = state.isPlaying,
            onToggle  = {
                if (!state.isPlaying && state.playheadMs >= state.endMs) {
                    state.playheadMs = state.startMs
                }
                state.isPlaying = !state.isPlaying
            }
        )
    }
}


