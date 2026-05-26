package com.mudassir.videodownloader.ui.trimmer

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mudassir.videodownloader.R
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.BgDark
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.ChipSelected
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.ChipTextOff
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.ChipTextOn
import com.mudassir.videodownloader.ui.trimmer.TrimmerColors.TextWhite
import com.mudassir.videodownloader.utills.AppUtills
import kotlinx.coroutines.delay
import kotlin.math.PI

@Composable
fun TrimmerScreen(onBack: () -> Unit = {}) {

    AppUtills.WindowInsetsController()
    val viewModel: TrimmerViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val state  = remember { TrimmerState() }
    val haptic = LocalHapticFeedback.current


    LaunchedEffect(uiState.audioInfo?.durationMs) {
        uiState.audioInfo?.durationMs?.let { dur ->

            state.totalMs = dur

            state.startMs = 0L
            state.endMs = dur
            state.playheadMs = 0L
        }
    }


    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onAudioPicked(it) }
    }

    // Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) filePicker.launch("audio/*")
    }

    val writePermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){ granted ->

            if(granted){

                viewModel.onSaveClicked(
                    state.startMs,
                    state.endMs,
                    state.trimMode
                )
            }
        }

    LaunchedEffect(state.isPlaying) {
        while (state.isPlaying) {
            delay(100L)
            val next = state.playheadMs + 100L
            if (next >= state.endMs) {
                state.playheadMs = state.startMs
                state.isPlaying  = false
            } else {
                state.playheadMs = next
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show save success
    LaunchedEffect(uiState.saveSuccess) {
        uiState.saveSuccess?.let { path ->
            snackbarHostState.showSnackbar(
                message     = "Saved to $path",
                duration    = SnackbarDuration.Long
            )

            Log.d("AudioSaveMessage", "Saved to $path")
            viewModel.clearSaveSuccess()
        }
    }

    // Show error
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message  = msg,
                duration = SnackbarDuration.Short

            )
            Log.d("AudioSaveMessage", msg)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        containerColor   = BgDark,
        snackbarHost     = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BgDark)
                .padding(top = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {

                TrimModeToggle(
                    current        = state.trimMode,
                    onModeSelected = { state.trimMode = it }
                )


                PickAudioButton(
                    hasFile  = uiState.audioInfo != null,
                    onClick  = {
                        // Request permission then open picker
                        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            Manifest.permission.READ_MEDIA_AUDIO
                        else
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        permissionLauncher.launch(permission)
                    }
                )
            }

            // File name display
            uiState.audioInfo?.let { info ->
                Text(
                    text     = info.fileName,
                    color    = TextWhite.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.isLoadingWaveform) {
                    // Show shimmer while loading
                    WaveformSkeleton(
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    WaveformEditor(
                        state        = state,
                        // Use real waveform
                        waveformData = uiState.waveformData.ifEmpty {
                            remember { generateSyntheticWaveform() }
                        },
                        onDrag       = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier     = Modifier.fillMaxSize()
                    )
                }

                // No audio placeholder when nothing is picked
                if (uiState.audioInfo == null && !uiState.isLoadingWaveform) {
                    NoAudioPlaceholder(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            TimeControlRow(state = state)

            Spacer(Modifier.height(14.dp))

            ZoomUndoRow(state = state)

            Spacer(Modifier.height(16.dp))


            SaveButton(
                enabled   = uiState.audioInfo != null && !uiState.isSaving,
                isSaving  = uiState.isSaving,
                onClick = {

                    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){

                        writePermissionLauncher.launch(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )

                    }else{

                        viewModel.onSaveClicked(
                            state.startMs,
                            state.endMs,
                            state.trimMode
                        )
                    }
                }
            )

            Spacer(Modifier.height(20.dp))


            PlayPauseButton(
                isPlaying = state.isPlaying,
                onToggle  = {
                    state.isPlaying = !state.isPlaying
                    viewModel.togglePlayback(state.startMs)   // ← pass startMs
                }
            )
        }
    }
}


@Composable
fun TrimModeToggle(
    current: TrimMode,
    onModeSelected: (TrimMode) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TrimMode.entries.forEach { mode ->
            val isSelected = current == mode
            val label      = if (mode == TrimMode.TRIM_SIDES) "Trim Sides" else "Trim Middle"

            val bgColor by animateColorAsState(
                targetValue   = if (isSelected) ChipSelected else Color.Transparent,
                animationSpec = tween(200),
                label         = "chip_bg"
            )
            val textColor by animateColorAsState(
                targetValue   = if (isSelected) ChipTextOn else ChipTextOff,
                animationSpec = tween(200),
                label         = "chip_text"
            )
            val borderColor by animateColorAsState(
                targetValue   = if (isSelected) ChipSelected else Color(0xFF444455),
                animationSpec = tween(200),
                label         = "chip_border"
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onModeSelected(mode) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter            = painterResource(
                        if (mode == TrimMode.TRIM_SIDES) R.drawable.ic_trim_sides
                        else R.drawable.ic_trim_middle
                    ),
                    contentDescription = null,
                    tint               = textColor,
                    modifier           = Modifier.size(12.dp)
                )
                Text(
                    text       = label,
                    color      = textColor,
                    fontSize   = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}




fun generateSyntheticWaveform(count: Int = 200): List<Float> {
    val rng = java.util.Random(42L)
    return List(count) { i ->
        val t        = i / count.toFloat()
        val envelope = kotlin.math.sin(t * PI.toFloat()) * 0.65f + 0.35f
        (rng.nextFloat() * envelope).coerceIn(0.05f, 1f)
    }
}