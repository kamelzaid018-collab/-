package com.example.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object MediaStorageHelper {

    private const val TAG = "MediaStorageHelper"

    /**
     * Copies a user-selected Content URI or File into the app's internal permanent storage directory.
     * This guarantees that background services, alarms, and future app launches always have
     * permanent, uninterrupted read access without permission expiration or security exceptions.
     *
     * @return Pair of (savedLocalFilePath, originalDisplayName) or null if failed.
     */
    fun saveUserMediaFile(
        context: Context,
        sourceUri: Uri,
        folderName: String = "user_media",
        filePrefix: String = "custom"
    ): Pair<String, String>? {
        return try {
            val displayName = queryFileName(context, sourceUri) ?: "audio_${System.currentTimeMillis()}.mp3"
            val sanitizedName = sanitizeFilename(displayName)
            val targetDir = File(context.filesDir, folderName).apply { mkdirs() }
            
            val uniqueFileName = "${filePrefix}_${System.currentTimeMillis()}_$sanitizedName"
            val targetFile = File(targetDir, uniqueFileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream for URI: $sourceUri")
                return null
            }

            inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (targetFile.exists() && targetFile.length() > 0) {
                Log.d(TAG, "Successfully copied media file to internal storage: ${targetFile.absolutePath} (size: ${targetFile.length()} bytes)")
                Pair(targetFile.absolutePath, displayName)
            } else {
                Log.e(TAG, "Target file is empty or missing after copy: ${targetFile.absolutePath}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user media file: ${e.message}", e)
            null
        }
    }

    /**
     * Extracts the real display name of a file from a Content URI.
     */
    fun queryFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) {
                            return name
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not resolve DISPLAY_NAME: ${e.message}")
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
    }
}
