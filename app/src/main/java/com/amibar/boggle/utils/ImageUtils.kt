package com.amibar.boggle.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Utility functions for image processing and data conversion.
 * Provides helper methods for converting between [Bitmap], Base64 strings, and [Uri]s,
 * as well as custom Data Binding adapters for [ImageView].
 */

/** Tag used for logging. */
private const val TAG = "ImageUtils"

/**
 * Converts a content [Uri] to a Base64 encoded JPEG string.
 *
 * @param uri The image [Uri] to convert.
 * @param context Application [Context] for accessing [android.content.ContentResolver].
 * @return A Base64 string representation of the image, or null if conversion fails.
 * @throws IOException If the input stream cannot be opened or read.
 */
@Throws(IOException::class)
suspend fun uriToBase64(uri: Uri, context: Context): String? {
    return withContext(Dispatchers.IO){
        try {
            val bitmap = uriToBitmap(uri, context)
            bitmapToBase64(bitmap!!)
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not found: $uri", e)
            null
        }
    }
}

suspend fun uriToBitmap(uri: Uri, context: Context): Bitmap? {
    return withContext(Dispatchers.IO){
        try {
            ImageDecoder.createSource(context.contentResolver, uri).let {
                ImageDecoder.decodeBitmap(it)
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not found: $uri", e)
            null
        }
    }
}

/**
 * Converts a [Bitmap] object into a Base64 encoded JPEG string.
 * Uses 70% quality compression to balance size and visual fidelity.
 *
 * @param bitmap The [Bitmap] to encode.
 * @return Base64 encoded string, or null on error.
 */
fun bitmapToBase64(bitmap: Bitmap): String? {
    return try {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.DEFAULT)
    } catch (e: Exception) {
        Log.e(TAG, "Error converting image to Base64", e)
        null
    }
}

/**
 * Decodes a Base64 encoded string back into a [Bitmap] object.
 *
 * @param base64 The encoded image string.
 * @return The decoded [Bitmap], or null if input is null or invalid.
 */
fun base64ToBitmap(base64: String?): Bitmap? {
    if (base64 == null) return null
    return try {
        val decodedArray = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedArray, 0, decodedArray.size)
    } catch (e: Exception) {
        Log.e(TAG, "Error decoding Base64 to Bitmap", e)
        null
    }
}
