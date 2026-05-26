package com.mudassir.videodownloader.ui.trimmer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


enum class TrimMode { TRIM_SIDES, TRIM_MIDDLE }

class TrimmerState(initialTotalMs: Long = 30_000L) {
    var totalMs by mutableLongStateOf(initialTotalMs)
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

