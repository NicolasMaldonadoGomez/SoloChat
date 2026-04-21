package github.com.nicolasmaldonadogomez.solochat.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageUtils {

    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val fileName = "IMG_${UUID.randomUUID()}.jpg"
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            val file = File(imagesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? {
        return try {
            val fileName = "IMG_${UUID.randomUUID()}.jpg"
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            val file = File(imagesDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}