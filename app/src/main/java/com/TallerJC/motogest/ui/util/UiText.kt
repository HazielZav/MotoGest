package com.TallerJC.motogest.ui.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import java.text.NumberFormat
import java.util.Locale

/**
 * Representa un texto mostrable en UI sin acoplar los ViewModels a [Context],
 * permitiendo traducir automáticamente mensajes de validación, éxito y error
 * según el idioma activo del sistema.
 */
sealed class UiText {
    data class DynamicString(val value: String) : UiText()

    class StringResource(
        @param:StringRes val resId: Int,
        vararg val args: Any
    ) : UiText() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StringResource) return false
            return resId == other.resId && args.contentEquals(other.args)
        }

        override fun hashCode(): Int {
            return 31 * resId + args.contentHashCode()
        }

        override fun toString(): String {
            return "StringResource(resId=$resId, args=${args.contentToString()})"
        }
    }

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(resId, *args)
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(resId, *args)
    }
}

/**
 * Formateador de moneda: usa MXN (`es-MX`) cuando el idioma activo es español
 * y respeta [Locale.getDefault] cuando se cambia a otro idioma (p. ej. inglés).
 */
@Composable
fun rememberCurrencyFormatter(): NumberFormat {
    val activeLocale = Locale.getDefault()
    return remember(activeLocale) {
        val targetLocale = if (activeLocale.language.equals("es", ignoreCase = true)) {
            Locale.forLanguageTag("es-MX")
        } else {
            activeLocale
        }
        NumberFormat.getCurrencyInstance(targetLocale)
    }
}
