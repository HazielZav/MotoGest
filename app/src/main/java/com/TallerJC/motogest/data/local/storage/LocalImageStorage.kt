package com.TallerJC.motogest.data.local.storage

import android.content.Context
import android.net.Uri
import com.TallerJC.motogest.providers.MotoGestFileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Representa un archivo de imagen persistido físicamente en el almacenamiento interno
 * privado de la aplicación (`filesDir/images/`).
 */
data class StoredImageFile(
    val localUri: String,
    val localPath: String,
    val fileName: String,
    val mimeType: String = "image/jpeg",
    val fileSizeBytes: Long = 0L
)

/**
 * Abstracción de almacenamiento interno de imágenes para desacoplar las operaciones
 * del sistema de archivos y facilitar pruebas unitarias.
 */
interface LocalImageStorage {
    fun createCameraImageDestination(prefix: String = "img"): StoredImageFile
    fun finalizeCameraImage(destination: StoredImageFile): StoredImageFile?
    suspend fun copyPickedImageToInternalStorage(sourceUri: Uri, prefix: String = "img"): StoredImageFile?
    fun deleteLocalFile(localPath: String): Boolean
}

/**
 * Implementación Android que persiste las fotos en `context.filesDir/images/`
 * (persistente, a diferencia de `cacheDir`) y expone URIs seguras mediante [MotoGestFileProvider].
 */
class AndroidLocalImageStorage(
    private val context: Context
) : LocalImageStorage {

    override fun createCameraImageDestination(prefix: String): StoredImageFile {
        val dir = MotoGestFileProvider.getImagesDirectory(context)
        val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
        val file = File(dir, fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        val contentUri = MotoGestFileProvider.getUriForImageFile(context, file)
        return StoredImageFile(
            localUri = contentUri.toString(),
            localPath = file.absolutePath,
            fileName = fileName,
            mimeType = "image/jpeg",
            fileSizeBytes = 0L
        )
    }

    override fun finalizeCameraImage(destination: StoredImageFile): StoredImageFile? {
        val file = File(destination.localPath)
        return if (file.exists() && file.length() > 0L) {
            destination.copy(fileSizeBytes = file.length())
        } else {
            runCatching { if (file.exists()) file.delete() }
            null
        }
    }

    override suspend fun copyPickedImageToInternalStorage(
        sourceUri: Uri,
        prefix: String
    ): StoredImageFile? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val resolvedMime = resolver.getType(sourceUri)?.takeIf { it.startsWith("image/") } ?: "image/jpeg"
        val extension = when (resolvedMime.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val dir = MotoGestFileProvider.getImagesDirectory(context)
        val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$extension"
        val destFile = File(dir, fileName)

        try {
            val inputStream = resolver.openInputStream(sourceUri) ?: return@withContext null
            inputStream.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (!destFile.exists() || destFile.length() == 0L) {
                destFile.delete()
                return@withContext null
            }
            val contentUri = MotoGestFileProvider.getUriForImageFile(context, destFile)
            StoredImageFile(
                localUri = contentUri.toString(),
                localPath = destFile.absolutePath,
                fileName = fileName,
                mimeType = resolvedMime,
                fileSizeBytes = destFile.length()
            )
        } catch (_: Exception) {
            runCatching { if (destFile.exists()) destFile.delete() }
            null
        }
    }

    override fun deleteLocalFile(localPath: String): Boolean {
        if (localPath.isBlank()) return false
        return runCatching {
            val file = File(localPath)
            if (file.exists()) file.delete() else false
        }.getOrDefault(false)
    }
}
