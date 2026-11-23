package com.TallerJC.motogest.data.sync

import android.content.Context
import androidx.core.content.edit

interface SyncCooldownStore {
    fun obtenerUltimoPullMs(): Long
    fun guardarUltimoPullMs(timestampMs: Long)
}

class SharedPreferencesSyncCooldownStore(context: Context) : SyncCooldownStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun obtenerUltimoPullMs(): Long = prefs.getLong(KEY_LAST_PULL_MS, 0L)

    override fun guardarUltimoPullMs(timestampMs: Long) {
        prefs.edit { putLong(KEY_LAST_PULL_MS, timestampMs) }
    }

    private companion object {
        const val PREFS_NAME = "motogest_sync_prefs"
        const val KEY_LAST_PULL_MS = "last_pull_ms"
    }
}
