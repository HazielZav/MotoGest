package com.TallerJC.motogest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.TallerJC.motogest.R

/**
 * Miniatura reutilizable de la foto de perfil de una motocicleta.
 * Muestra la imagen local mediante Coil 3 cuando existe una URI válida y
 * conserva un ícono discreto de respaldo cuando la unidad aún no tiene fotografía.
 */
@Composable
fun MotoThumbnail(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    cornerRadius: Dp = 10.dp,
    contentDescription: String? = stringResource(R.string.recepcion_desc_miniatura_moto)
) {
    val tieneFotoReal = !photoUrl.isNullOrBlank() && !photoUrl.startsWith("local://")

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.TwoWheeler,
            contentDescription = if (tieneFotoReal) null else contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(size * 0.52f)
        )
        if (tieneFotoReal) {
            AsyncImage(
                model = photoUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
