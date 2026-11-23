package com.TallerJC.motogest.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.TallerJC.motogest.MotoGestApplication

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? MotoGestApplication ?: return Result.failure()
        val forzarPull = inputData.getBoolean(KEY_FORZAR_PULL, false)
        val exito = app.container.operacionesRepository.sincronizarAutomaticamente(forzarPull)
        if (exito) return Result.success()
        return if (runAttemptCount >= MAX_REINTENTOS) Result.failure() else Result.retry()
    }

    companion object {
        const val KEY_FORZAR_PULL = "forzar_pull"
        private const val MAX_REINTENTOS = 5
    }
}
