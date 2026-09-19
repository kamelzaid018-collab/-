package com.example.media

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

object ZipExtractor {

    private const val TAG = "ZipExtractor"

    /**
     * Extracts images (.jpg, .jpeg, .png, .webp) from a selected ZIP URI into a dedicated subfolder.
     * Returns the list of absolute file paths for the extracted images.
     */
    fun extractImagesFromZip(context: Context, zipUri: Uri, subfolderName: String = "azan_images"): List<String> {
        val targetDir = File(context.filesDir, subfolderName).apply {
            if (exists()) deleteRecursively()
            mkdirs()
        }

        val extractedPaths = mutableListOf<String>()
        val imageExtensions = setOf("jpg", "jpeg", "png", "webp")

        try {
            context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        val name = entry.name
                        val ext = name.substringAfterLast('.', "").lowercase()
                        if (!entry.isDirectory && imageExtensions.contains(ext) && !name.contains("__MACOSX")) {
                            val cleanName = File(name).name
                            val outputFile = File(targetDir, "${System.currentTimeMillis()}_$cleanName")
                            FileOutputStream(outputFile).use { out ->
                                zipIn.copyTo(out)
                            }
                            extractedPaths.add(outputFile.absolutePath)
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting images from zip: ${e.message}", e)
        }

        return extractedPaths.sorted()
    }

    /**
     * Extracts audio files (.mp3, .wav, .m4a, .aac, .ogg) from a selected ZIP URI into a dedicated subfolder.
     * Returns the list of absolute file paths for the extracted audio.
     */
    fun extractAudioFromZip(context: Context, zipUri: Uri, subfolderName: String = "salawat_audio"): List<String> {
        val targetDir = File(context.filesDir, subfolderName).apply {
            if (exists()) deleteRecursively()
            mkdirs()
        }

        val extractedPaths = mutableListOf<String>()
        val audioExtensions = setOf("mp3", "wav", "m4a", "aac", "ogg")

        try {
            context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        val name = entry.name
                        val ext = name.substringAfterLast('.', "").lowercase()
                        if (!entry.isDirectory && audioExtensions.contains(ext) && !name.contains("__MACOSX")) {
                            val cleanName = File(name).name
                            val outputFile = File(targetDir, "${System.currentTimeMillis()}_$cleanName")
                            FileOutputStream(outputFile).use { out ->
                                zipIn.copyTo(out)
                            }
                            extractedPaths.add(outputFile.absolutePath)
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting audio from zip: ${e.message}", e)
        }

        return extractedPaths.sorted()
    }
}
