package com.TallerJC.motogest.providers

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.TallerJC.motogest.R
import java.io.File

/**
 * FileProvider de MotoGest siguiendo la convención de FileProvider propio del curso.
 * Expone exclusivamente el subdirectorio persistente `filesDir/images/` para delegar
 * la captura fotográfica a la cámara nativa del sistema (`ActivityResultContracts.TakePicture`).
 */
class MotoGestFileProvider : FileProvider(R.xml.file_paths) {

    companion object {
        const val IMAGES_SUBDIR = "images"

        fun getAuthority(context: Context): String = "${context.packageName}.fileprovider"

        fun getImagesDirectory(context: Context): File {
            val dir = File(context.filesDir, IMAGES_SUBDIR)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

        fun getUriForImageFile(context: Context, imageFile: File): Uri {
            return getUriForFile(context, getAuthority(context), imageFile)
        }
    }
}
