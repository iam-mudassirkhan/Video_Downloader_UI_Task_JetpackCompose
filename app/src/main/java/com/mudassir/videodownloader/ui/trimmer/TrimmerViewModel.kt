package com.mudassir.videodownloader.ui.trimmer

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mudassir.videodownloader.audio.AudioInfo
import com.mudassir.videodownloader.audio.AudioResult
import com.mudassir.videodownloader.audio.AudioTrimmerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TrimmerUiState(
    val audioInfo: AudioInfo?       = null,
    val waveformData: List<Float>   = emptyList(),
    val isLoadingWaveform: Boolean  = false,
    val isSaving: Boolean           = false,
    val saveSuccess: String?        = null,
    val errorMessage: String?       = null,
)

class TrimmerViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext

    // I  kept MediaPlayer in ViewModel so it will survives recomposition
    private var mediaPlayer: MediaPlayer? = null

    private val _uiState = MutableStateFlow(TrimmerUiState())
    val uiState: StateFlow<TrimmerUiState> = _uiState.asStateFlow()


    fun onAudioPicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWaveform = true, errorMessage = null) }

            when (val infoResult = AudioTrimmerManager.getAudioInfo(context, uri)) {
                is AudioResult.Error   -> {
                    _uiState.update { it.copy(isLoadingWaveform = false, errorMessage = infoResult.message) }
                    return@launch
                }
                is AudioResult.Success -> {
                    // Release old player when a new file is picked
                    mediaPlayer?.release()
                    mediaPlayer = null
                    _uiState.update { it.copy(audioInfo = infoResult.data) }
                }
            }

            when (val waveResult = AudioTrimmerManager.extractWaveform(context, uri)) {
                is AudioResult.Error   -> _uiState.update { it.copy(isLoadingWaveform = false) }
                is AudioResult.Success -> _uiState.update {
                    it.copy(waveformData = waveResult.data, isLoadingWaveform = false)
                }
            }
        }
    }


    fun onSaveClicked(startMs: Long, endMs: Long, trimMode: TrimMode) {
        val info = _uiState.value.audioInfo ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, saveSuccess = null) }

            when (val result = AudioTrimmerManager.trimAndSave(
                context  = context,
                uri      = info.uri,
                startMs  = startMs,
                endMs    = endMs,
                trimMode = trimMode,
                mimeType = info.mimeType
            )) {
                is AudioResult.Error   -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
                is AudioResult.Success -> _uiState.update {
                    it.copy(isSaving = false, saveSuccess = result.data)
                }
            }
        }
    }


    fun togglePlayback(startMs: Long) {
        val info = _uiState.value.audioInfo ?: return

        viewModelScope.launch(Dispatchers.Main) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()

            } else if (mediaPlayer != null) {

                mediaPlayer?.start()

            } else {

                withContext(Dispatchers.IO) {
                    try {
                        val player = MediaPlayer().apply {
                            setDataSource(context, info.uri)

                            setOnPreparedListener { mp ->

                                mp.seekTo(startMs.toInt())
                                mp.start()
                            }

                            setOnCompletionListener {
                                mediaPlayer = null
                            }

                            setOnErrorListener { _, what, extra ->
                                mediaPlayer = null
                                false
                            }


                            prepareAsync()
                        }
                        mediaPlayer = player
                    } catch (e: Exception) {
                        e.message?.let { Log.d("AudioSaveMessage", it) }
                    }
                }
            }
        }
    }


    override fun onCleared() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onCleared()
    }

    fun clearSaveSuccess()  { _uiState.update { it.copy(saveSuccess  = null) } }
    fun clearErrorMessage() { _uiState.update { it.copy(errorMessage = null) } }
}
