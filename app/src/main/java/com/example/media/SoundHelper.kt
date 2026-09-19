package com.example.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

object SoundHelper {

    private const val TAG = "SoundHelper"
    private var activeMediaPlayer: MediaPlayer? = null

    /**
     * Initializes default audio files (WAV) in internal storage so real sounds always play without requiring external files.
     */
    fun ensureDefaultSounds(context: Context) {
        val audioDir = File(context.filesDir, "default_sounds").apply { mkdirs() }

        val timeAlertFile = File(audioDir, "time_alert.wav")
        if (!timeAlertFile.exists() || timeAlertFile.length() < 100) {
            generateBeepSequence(timeAlertFile, listOf(880.0 to 180, 1174.66 to 250, 1480.0 to 400))
        }

        val azanFile = File(audioDir, "azan_default.wav")
        if (!azanFile.exists() || azanFile.length() < 100) {
            generateAzanMelody(azanFile)
        }

        val alertFile = File(audioDir, "alert_default.wav")
        if (!alertFile.exists() || alertFile.length() < 100) {
            generateBeepSequence(alertFile, listOf(587.33 to 200, 783.99 to 200, 880.0 to 350))
        }

        val salawat1 = File(audioDir, "salawat_default_1.wav")
        if (!salawat1.exists() || salawat1.length() < 100) {
            generateBeepSequence(salawat1, listOf(523.25 to 250, 659.25 to 250, 783.99 to 300, 1046.50 to 500))
        }

        val salawat2 = File(audioDir, "salawat_default_2.wav")
        if (!salawat2.exists() || salawat2.length() < 100) {
            generateBeepSequence(salawat2, listOf(659.25 to 200, 783.99 to 200, 987.77 to 400))
        }
    }

    fun getDefaultTimeAlertPath(context: Context): String {
        ensureDefaultSounds(context)
        return File(File(context.filesDir, "default_sounds"), "time_alert.wav").absolutePath
    }

    fun getDefaultAzanPath(context: Context): String {
        ensureDefaultSounds(context)
        return File(File(context.filesDir, "default_sounds"), "azan_default.wav").absolutePath
    }

    fun getDefaultAlertPath(context: Context): String {
        ensureDefaultSounds(context)
        return File(File(context.filesDir, "default_sounds"), "alert_default.wav").absolutePath
    }

    fun getDefaultSalawatList(context: Context): List<String> {
        ensureDefaultSounds(context)
        val dir = File(context.filesDir, "default_sounds")
        val s1 = File(dir, "salawat_default_1.wav")
        val s2 = File(dir, "salawat_default_2.wav")
        return listOfNotNull(
            if (s1.exists()) s1.absolutePath else null,
            if (s2.exists()) s2.absolutePath else null
        )
    }

    fun playSound(
        context: Context,
        uriString: String?,
        fallbackPath: String? = null,
        isAlarm: Boolean = true,
        onCompletion: (() -> Unit)? = null
    ): MediaPlayer? {
        stopCurrentSound()
        ensureDefaultSounds(context)
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(if (isAlarm) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                var loaded = false
                if (!uriString.isNullOrBlank()) {
                    try {
                        when (uriString) {
                            "builtin_azan_default" -> {
                                setDataSource(getDefaultAzanPath(context))
                                loaded = true
                            }
                            "builtin_time_alert" -> {
                                setDataSource(getDefaultTimeAlertPath(context))
                                loaded = true
                            }
                            "builtin_alert_default" -> {
                                setDataSource(getDefaultAlertPath(context))
                                loaded = true
                            }
                            "builtin_salawat_1" -> {
                                val s1 = File(File(context.filesDir, "default_sounds"), "salawat_default_1.wav")
                                if (s1.exists()) {
                                    setDataSource(s1.absolutePath)
                                    loaded = true
                                }
                            }
                            "builtin_salawat_2" -> {
                                val s2 = File(File(context.filesDir, "default_sounds"), "salawat_default_2.wav")
                                if (s2.exists()) {
                                    setDataSource(s2.absolutePath)
                                    loaded = true
                                }
                            }
                            else -> {
                                if (uriString.startsWith("content://") || uriString.startsWith("android.resource://")) {
                                    setDataSource(context, Uri.parse(uriString))
                                    loaded = true
                                } else {
                                    val f = File(uriString)
                                    if (f.exists() && f.length() > 0) {
                                        setDataSource(f.absolutePath)
                                        loaded = true
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed loading uriString ($uriString): $e")
                    }
                }

                if (!loaded && !fallbackPath.isNullOrBlank()) {
                    try {
                        if (fallbackPath.startsWith("content://") || fallbackPath.startsWith("android.resource://")) {
                            setDataSource(context, Uri.parse(fallbackPath))
                            loaded = true
                        } else {
                            val f = File(fallbackPath)
                            if (f.exists() && f.length() > 0) {
                                setDataSource(f.absolutePath)
                                loaded = true
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed loading fallbackPath: $e")
                    }
                }

                if (!loaded) {
                    val defaultFallback = if (isAlarm) getDefaultAzanPath(context) else getDefaultAlertPath(context)
                    setDataSource(defaultFallback)
                }

                setOnCompletionListener {
                    activeMediaPlayer = null
                    onCompletion?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                    activeMediaPlayer = null
                    onCompletion?.invoke()
                    true
                }
                prepare()
                start()
            }
            activeMediaPlayer = mp
            return mp
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}", e)
            onCompletion?.invoke()
            return null
        }
    }

    fun stopCurrentSound() {
        try {
            activeMediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping sound: ${e.message}")
        } finally {
            activeMediaPlayer = null
        }
    }

    private fun generateBeepSequence(file: File, notes: List<Pair<Double, Int>>) {
        val sampleRate = 22050
        val totalMs = notes.sumOf { it.second } + (notes.size * 50)
        val totalSamples = (sampleRate * totalMs) / 1000
        val pcmData = ShortArray(totalSamples)

        var sampleIdx = 0
        for ((freq, durationMs) in notes) {
            val noteSamples = (sampleRate * durationMs) / 1000
            for (i in 0 until noteSamples) {
                if (sampleIdx >= totalSamples) break
                val t = i.toDouble() / sampleRate
                val envelope = when {
                    i < 200 -> i / 200.0
                    i > noteSamples - 400 -> (noteSamples - i) / 400.0
                    else -> 1.0
                }
                val sample = (sin(2.0 * Math.PI * freq * t) * envelope * 24000).toInt().coerceIn(-32768, 32767)
                pcmData[sampleIdx++] = sample.toShort()
            }
            val pauseSamples = (sampleRate * 50) / 1000
            for (i in 0 until pauseSamples) {
                if (sampleIdx < totalSamples) pcmData[sampleIdx++] = 0
            }
        }
        writeWavFile(file, pcmData, sampleRate)
    }

    private fun generateAzanMelody(file: File) {
        val sampleRate = 22050
        val notes = listOf(
            293.66 to 600,
            329.63 to 500,
            349.23 to 700,
            392.00 to 1100,
            349.23 to 600,
            329.63 to 700,
            293.66 to 1400,
            293.66 to 500,
            349.23 to 600,
            392.00 to 1300,
            440.00 to 800,
            392.00 to 700,
            349.23 to 800,
            293.66 to 1800
        )
        val totalMs = notes.sumOf { it.second } + (notes.size * 100)
        val totalSamples = (sampleRate * totalMs) / 1000
        val pcmData = ShortArray(totalSamples)

        var sampleIdx = 0
        for ((freq, durationMs) in notes) {
            val noteSamples = (sampleRate * durationMs) / 1000
            for (i in 0 until noteSamples) {
                if (sampleIdx >= totalSamples) break
                val t = i.toDouble() / sampleRate
                val envelope = when {
                    i < 400 -> i / 400.0
                    i > noteSamples - 600 -> (noteSamples - i) / 600.0
                    else -> 1.0
                }
                val s1 = sin(2.0 * Math.PI * freq * t)
                val s2 = 0.4 * sin(4.0 * Math.PI * freq * t)
                val s3 = 0.15 * sin(6.0 * Math.PI * freq * t)
                val combined = (s1 + s2 + s3) / 1.55
                val sample = (combined * envelope * 26000).toInt().coerceIn(-32768, 32767)
                pcmData[sampleIdx++] = sample.toShort()
            }
            val pauseSamples = (sampleRate * 80) / 1000
            for (i in 0 until pauseSamples) {
                if (sampleIdx < totalSamples) pcmData[sampleIdx++] = 0
            }
        }
        writeWavFile(file, pcmData, sampleRate)
    }

    private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
        val byteData = ByteArray(pcmData.size * 2)
        ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(pcmData)

        FileOutputStream(file).use { out ->
            val totalAudioLen = byteData.size
            val totalDataLen = totalAudioLen + 36
            val channels = 1
            val byteRate = sampleRate * channels * 2

            val header = ByteArray(44)
            header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
            header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
            header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
            header[20] = 1; header[21] = 0
            header[22] = channels.toByte(); header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = (channels * 2).toByte(); header[33] = 0
            header[34] = 16; header[35] = 0
            header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

            out.write(header)
            out.write(byteData)
        }
    }
}
