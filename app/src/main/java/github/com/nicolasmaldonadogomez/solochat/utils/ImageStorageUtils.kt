package github.com.nicolasmaldonadogomez.solochat.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

object ImageStorageUtils {

    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val hash = calculateHash(bytes)
            val fileName = "IMG_$hash.jpg"
            
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            val file = File(imagesDir, fileName)
            if (!file.exists()) {
                FileOutputStream(file).use { output ->
                    output.write(bytes)
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
            // Para el hash de un bitmap, lo convertimos a bytes primero
            val stream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val bytes = stream.toByteArray()
            
            val hash = calculateHash(bytes)
            val fileName = "IMG_$hash.jpg"
            
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            val file = File(imagesDir, fileName)
            if (!file.exists()) {
                FileOutputStream(file).use { output ->
                    output.write(bytes)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteImageIfOrphaned(context: Context, imagePath: String?, isUsedCallback: (String) -> Boolean) {
        if (imagePath == null) return
        val file = File(imagePath)
        if (file.exists() && !isUsedCallback(imagePath)) {
            file.delete()
        }
    }

    private fun calculateHash(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}