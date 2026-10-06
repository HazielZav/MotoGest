package com.TallerJC.motogest.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- MODO OSCURO (El principal de la app) ---
private val DarkColorScheme = darkColorScheme(
    primary = NaranjaVibrante,
    onPrimary = TextoBlanco,
    secondary = NaranjaClaro,
    onSecondary = FondoPrincipal,

    background = FondoPrincipal,
    onBackground = TextoBlanco,

    surface = SuperficieCard,
    onSurface = TextoBlanco,

    surfaceVariant = SuperficieVariante, // Para las cajas de texto y fondos secundarios
    onSurfaceVariant = ArenaMuted, // Textos secundarios o íconos deshabilitados

    error = RojoOscuro,
    onError = TextoBlanco
)

// --- MODO CLARO (De soporte) ---
private val LightColorScheme = lightColorScheme(
    primary = NaranjaOscuro,
    onPrimary = TextoBlanco,
    secondary = MarronMuted,
    onSecondary = TextoBlanco,

    background = TextoBlanco,
    onBackground = FondoPrincipal,

    surface = ArenaMuted, // Fondo de tarjetas claro
    onSurface = FondoPrincipal,

    surfaceVariant = GrisAzuladoSutil,
    onSurfaceVariant = SuperficieVariante,

    error = RojoOscuro,
    onError = TextoBlanco
)

@Composable
fun MotoGestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    // Barra de estado
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}