package com.mudassir.videodownloader.audio

import android.content.ContentValues
import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.mudassir.videodownloader.ui.trimmer.TrimMode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

sealed class AudioResult<out T> {
    data class Success<T>(val data: T) : AudioResult<T>()
    data class Error(val message: String) : AudioResult<Nothing>()
}

data class AudioInfo(
    val uri: Uri,
    val fileName: String,
    val durationMs: Long,
    val mimeType: String
)

object AudioTrimmerManager {

    suspend fun getAudioInfo(
        context: Context,
        uri: Uri
    ): AudioResult<AudioInfo> = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)

            val durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L

            if (durationMs <= 0L) {
                retriever.release()
                return@withContext AudioResult.Error("Could not read audio duration")
            }

            val mimeType = context.contentResolver.getType(uri) ?: "audio/mpeg"
            val fileName = getFileName(context, uri)
            retriever.release()

            AudioResult.Success(
                AudioInfo(uri = uri, fileName = fileName, durationMs = durationMs, mimeType = mimeType)
            )
        } catch (e: Exception) {
            AudioResult.Error("Failed to read audio: ${e.message}")
        }
    }

    suspend fun extractWaveform(
        context: Context,
        uri: Uri,
        sampleCount: Int = 200
    ): AudioResult<List<Float>> = withContext(Dispatchers.IO) {
        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(context, uri, null)

            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val mime = extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) { audioTrackIndex = i; break }
            }

            if (audioTrackIndex < 0) {
                extractor.release()
                return@withContext AudioResult.Success(generateSyntheticWaveform(sampleCount))
            }

            extractor.selectTrack(audioTrackIndex)

            val buffer     = ByteBuffer.allocate(256 * 1024)
            val rawSamples = mutableListOf<Float>()

            while (true) {
                val bytesRead = extractor.readSampleData(buffer, 0)
                if (bytesRead < 0) break
                var sum = 0.0
                val limit = bytesRead.coerceAtMost(256)
                repeat(limit) { i -> sum += Math.abs(buffer.get(i).toDouble()) }
                rawSamples.add((sum / limit).toFloat())
                extractor.advance()
            }
            extractor.release()

            if (rawSamples.isEmpty()) {
                return@withContext AudioResult.Success(generateSyntheticWaveform(sampleCount))
            }

            val maxVal = rawSamples.maxOrNull()?.takeIf { it > 0f } ?: 1f
            val result = List(sampleCount) { i ->
                val srcIdx = (i.toFloat() / sampleCount * rawSamples.size).toInt()
                    .coerceIn(0, rawSamples.lastIndex)
                (rawSamples[srcIdx] / maxVal).coerceIn(0.05f, 1f)
            }

            AudioResult.Success(result)
        } catch (e: Exception) {
            AudioResult.Success(generateSyntheticWaveform(sampleCount))
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun trimAndSave(
        context: Context,
        uri: Uri,
        startMs: Long,
        endMs: Long,
        trimMode: TrimMode,
        mimeType: String = "audio/mpeg"
    ): AudioResult<String> {

        return try {

            val outputName =
                "trimmed_${System.currentTimeMillis()}.mp3"

            val outFile =
                File(
                    context.cacheDir,
                    outputName
                )

            val mediaItem =
                MediaItem.Builder()
                    .setUri(uri)
                    .setClippingConfiguration(
                        MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(startMs)
                            .setEndPositionMs(endMs)
                            .build()
                    )
                    .build()

            val deferred =
                CompletableDeferred<AudioResult<String>>()

            //  run on main thread
            withContext(Dispatchers.Main) {

                val transformer =
                    Transformer.Builder(context)
                        .addListener(
                            object : Transformer.Listener {

                                override fun onCompleted(
                                    composition: Composition,
                                    exportResult: ExportResult
                                ) {

                                    try {

                                        val savedPath =
                                            saveToDownloadFolder(
                                                context = context,
                                                sourceFile = outFile,
                                                fileName = outputName
                                            )

                                        // remove temp cache file
                                        outFile.delete()

                                        deferred.complete(
                                            AudioResult.Success(
                                                savedPath
                                            )
                                        )

                                    } catch(e: Exception){

                                        deferred.complete(
                                            AudioResult.Error(
                                                e.message ?: "Save failed"
                                            )
                                        )
                                    }
                                }

                                override fun onError(
                                    composition: Composition,
                                    exportResult: ExportResult,
                                    exportException: ExportException
                                ) {

                                    deferred.complete(
                                        AudioResult.Error(
                                            exportException.message
                                                ?: "trim failed"
                                        )
                                    )
                                }
                            }
                        )
                        .build()

                transformer.start(
                    mediaItem,
                    outFile.absolutePath
                )
            }

            deferred.await()

        } catch(e:Exception){

            AudioResult.Error(
                e.message ?: "trim failed"
            )
        }

    }

    private fun saveToDownloadFolder(
        context: Context,
        sourceFile: File,
        fileName: String
    ): String {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            // Android 10+
            val values = ContentValues().apply {

                put(
                    MediaStore.Downloads.DISPLAY_NAME,
                    fileName
                )

                put(
                    MediaStore.Downloads.MIME_TYPE,
                    "audio/mpeg"
                )

                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS +
                            "/VideoDownloader"
                )

                put(
                    MediaStore.Downloads.IS_PENDING,
                    1
                )
            }

            val resolver =
                context.contentResolver

            val uri =
                resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                ) ?: throw Exception("Cannot create file")

            resolver.openOutputStream(uri)?.use { output ->

                sourceFile.inputStream().use {
                    it.copyTo(output)
                }
            }

            values.clear()

            values.put(
                MediaStore.Downloads.IS_PENDING,
                0
            )

            resolver.update(
                uri,
                values,
                null,
                null
            )

            "Download/VideoDownloader/$fileName"

        } else {

            // Android 9 and below

            val dir =
                File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    ),
                    "VideoDownloader"
                )

            if (!dir.exists()) {
                dir.mkdirs()
            }

            val outFile =
                File(
                    dir,
                    fileName
                )

            sourceFile.copyTo(
                outFile,
                overwrite = true
            )

            MediaScannerConnection.scanFile(
                context,
                arrayOf(outFile.absolutePath),
                arrayOf("audio/mpeg"),
                null
            )

            outFile.absolutePath
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "audio_file"
        context.contentResolver.query(
            uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = cursor.getString(idx)
            }
        }
        return name
    }

    private fun generateSyntheticWaveform(count: Int = 200): List<Float> {
        val rng = java.util.Random(42L)
        return List(count) { i ->
            val t        = i / count.toFloat()
            val envelope = kotlin.math.sin(t * Math.PI.toFloat()) * 0.65f + 0.35f
            (rng.nextFloat() * envelope).coerceIn(0.05f, 1f)
        }
    }
}