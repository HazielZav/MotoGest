package com.TallerJC.motogest.data.local.session

import android.content.Context

interface SessionManager {
    fun guardarSesion(userId: String)
    fun obtenerUsuarioId(): String?
    fun limpiarSesion()
}

class SharedPreferencesSessionManager(context: Context) : SessionManager {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun guardarSesion(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    override fun obtenerUsuarioId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    override fun limpiarSesion() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    private companion object {
        const val PREFS_NAME = "motogest_session"
        const val KEY_USER_ID = "logged_user_id"
    }
}
