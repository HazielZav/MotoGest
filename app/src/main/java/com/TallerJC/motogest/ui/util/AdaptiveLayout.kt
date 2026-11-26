package com.TallerJC.motogest.ui.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tipo de navegación principal de Material 3 según [WindowWidthSizeClass].
 */
enum class MotoGestNavigationType {
    BOTTOM_NAVIGATION,
    NAVIGATION_RAIL,
    PERMANENT_NAVIGATION_DRAWER
}

/**
 * Distribución de contenido principal según el ancho disponible.
 */
enum class MotoGestContentType {
    SINGLE_PANE,
    DUAL_PANE
}

/**
 * Estado adaptativo global de la aplicación, accesible desde cualquier pantalla
 * (incluyendo futuras pantallas como Login o Roles) mediante [LocalAdaptiveState].
 */
data class MotoGestAdaptiveState(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass = WindowHeightSizeClass.Medium,
    val navigationType: MotoGestNavigationType,
    val contentType: MotoGestContentType
) {
    val isCompact: Boolean get() = widthSizeClass == WindowWidthSizeClass.Compact
    val isMedium: Boolean get() = widthSizeClass == WindowWidthSizeClass.Medium
    val isExpanded: Boolean get() = widthSizeClass == WindowWidthSizeClass.Expanded
    val isDualPane: Boolean get() = contentType == MotoGestContentType.DUAL_PANE

    /**
     * Verdadero exclusivamente cuando la altura disponible es reducida (< 480 dp),
     * es decir, teléfonos en orientación horizontal (landscape).
     * Permite compactar encabezados, pestañas y filtros sin alterar celular vertical ni tabletas.
     */
    val isCompactHeight: Boolean get() = heightSizeClass == WindowHeightSizeClass.Compact

    companion object {
        fun fromWidthSizeClass(
            widthSizeClass: WindowWidthSizeClass,
            heightSizeClass: WindowHeightSizeClass = WindowHeightSizeClass.Medium
        ): MotoGestAdaptiveState {
            return when (widthSizeClass) {
                WindowWidthSizeClass.Compact -> MotoGestAdaptiveState(
                    widthSizeClass = widthSizeClass,
                    heightSizeClass = heightSizeClass,
                    navigationType = MotoGestNavigationType.BOTTOM_NAVIGATION,
                    contentType = MotoGestContentType.SINGLE_PANE
                )
                WindowWidthSizeClass.Medium -> MotoGestAdaptiveState(
                    widthSizeClass = widthSizeClass,
                    heightSizeClass = heightSizeClass,
                    navigationType = MotoGestNavigationType.NAVIGATION_RAIL,
                    contentType = MotoGestContentType.DUAL_PANE
                )
                WindowWidthSizeClass.Expanded -> MotoGestAdaptiveState(
                    widthSizeClass = widthSizeClass,
                    heightSizeClass = heightSizeClass,
                    navigationType = MotoGestNavigationType.PERMANENT_NAVIGATION_DRAWER,
                    contentType = MotoGestContentType.DUAL_PANE
                )
                else -> MotoGestAdaptiveState(
                    widthSizeClass = WindowWidthSizeClass.Compact,
                    heightSizeClass = heightSizeClass,
                    navigationType = MotoGestNavigationType.BOTTOM_NAVIGATION,
                    contentType = MotoGestContentType.SINGLE_PANE
                )
            }
        }
    }
}

val LocalAdaptiveState = compositionLocalOf {
    MotoGestAdaptiveState.fromWidthSizeClass(WindowWidthSizeClass.Compact)
}

/**
 * Encabezado principal homologado para todas las pantallas de MotoGest (Opción A: fondo limpio `background`).
 * - En celular vertical y tableta (`!isCompactHeight`) mantiene la altura estándar de Material 3 con título y subtítulo.
 * - En celular horizontal (`isCompactHeight`) se compacta a una sola fila de 44.dp para liberar espacio vertical.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotoGestTopBar(
    title: String,
    subtitle: String? = null,
    showBottomDivider: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val adaptive = LocalAdaptiveState.current
    Column(modifier = Modifier.fillMaxWidth()) {
        if (adaptive.isCompactHeight) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(start = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions
                    )
                }
            }
        } else {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    if (subtitle != null) {
                        Column {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = subtitle,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
        if (showBottomDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        }
    }
}

/**
 * Contenedor reutilizable para formularios (Recepción, futuro Login, etc.).
 * Centra el contenido horizontalmente y limita su ancho máximo en tablets o modo horizontal
 * para evitar campos de texto estirados al 100%.
 */
@Composable
fun AdaptiveFormContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 680.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth),
            content = content
        )
    }
}

/**
 * Panel derecho vacío reutilizable para pantallas List-Detail en modo `Medium` / `Expanded`.
 */
@Composable
fun AdaptiveEmptyDetailPane(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 420.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
