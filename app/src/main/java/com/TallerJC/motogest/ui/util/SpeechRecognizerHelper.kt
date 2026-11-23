package com.TallerJC.motogest.ui.util

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent

/**
 * Utilidad ligera para el dictado por voz nativo mediante [RecognizerIntent.ACTION_RECOGNIZE_SPEECH].
 * Delega la captura y transcripción al motor de voz del sistema (sin SDKs externos ni permiso RECORD_AUDIO).
 */
object SpeechRecognizerHelper {

    private const val SPANISH_LOCALE_TAG = "es-MX"

    fun createSpanishSpeechIntent(prompt: String): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, SPANISH_LOCALE_TAG)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, SPANISH_LOCALE_TAG)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
        }
    }

    /**
     * Extrae la mejor transcripción cuando el resultado es [Activity.RESULT_OK].
     * Si el usuario cancela o no hay resultados, devuelve `null` silenciosamente.
     */
    fun extractTranscribedText(resultCode: Int, data: Intent?): String? {
        if (resultCode != Activity.RESULT_OK || data == null) return null
        val matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        return matches?.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
    }

    /**
     * Combina el texto dictado con el texto ya presente en el campo sin sobrescribirlo.
     */
    fun combineWithExistingText(existing: String, dictated: String?): String {
        val cleanDictated = dictated?.trim().orEmpty()
        if (cleanDictated.isEmpty()) return existing
        return if (existing.isBlank()) {
            cleanDictated
        } else {
            "${existing.trimEnd()} $cleanDictated"
        }
    }
}
