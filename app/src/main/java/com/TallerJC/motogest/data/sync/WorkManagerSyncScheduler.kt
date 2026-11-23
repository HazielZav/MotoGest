package com.TallerJC.motogest.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.concurrent.TimeUnit

class WorkManagerSyncScheduler(context: Context) : SyncScheduler {

    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(context.applicationContext)
    }

    override val estadoSincronizacion: Flow<SyncState>
        get() = combine(
            workManager.getWorkInfosForUniqueWorkFlow(WORK_CAMBIOS),
            workManager.getWorkInfosForUniqueWorkFlow(WORK_PRIMER_PLANO),
            workManager.getWorkInfosForUniqueWorkFlow(WORK_PERIODICA)
        ) { cambios, primerPlano, periodica ->
            resolverEstado(cambios, primerPlano, periodica)
        }

    // Agrupa ediciones consecutivas con un pequeno retraso antes de subir cambios
    override fun programarSincronizacionCambios() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf(SyncWorker.KEY_FORZAR_PULL to false))
            .setConstraints(crearConstraints(requerirBateriaOk = false))
            .setInitialDelay(DEBOUNCE_SEGUNDOS, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            WORK_CAMBIOS,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun programarSincronizacionEnPrimerPlano() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf(SyncWorker.KEY_FORZAR_PULL to false))
            .setConstraints(crearConstraints(requerirBateriaOk = false))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            WORK_PRIMER_PLANO,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override fun programarSincronizacionPeriodica() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            INTERVALO_PERIODICO_MINUTOS,
            TimeUnit.MINUTES
        )
            .setInputData(workDataOf(SyncWorker.KEY_FORZAR_PULL to true))
            .setConstraints(crearConstraints(requerirBateriaOk = true))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_PERIODICA,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun crearConstraints(requerirBateriaOk: Boolean): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(requerirBateriaOk)
            .build()
    }

    private fun resolverEstado(
        cambios: List<WorkInfo>,
        primerPlano: List<WorkInfo>,
        periodica: List<WorkInfo>
    ): SyncState {
        val todos = cambios + primerPlano + periodica
        if (todos.any { it.state == WorkInfo.State.RUNNING }) return SyncState.SYNCING

        val estadoCambios = cambios.firstOrNull()?.state
        return when (estadoCambios) {
            WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> SyncState.PENDING
            WorkInfo.State.FAILED -> SyncState.ERROR
            else -> SyncState.IDLE
        }
    }

    private companion object {
        const val WORK_CAMBIOS = "motogest_sync_cambios"
        const val WORK_PRIMER_PLANO = "motogest_sync_primer_plano"
        const val WORK_PERIODICA = "motogest_sync_periodica"
        const val DEBOUNCE_SEGUNDOS = 8L
        const val INTERVALO_PERIODICO_MINUTOS = 30L
    }
}
