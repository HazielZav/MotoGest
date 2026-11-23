package com.TallerJC.motogest.data.sync

import kotlinx.coroutines.flow.Flow

interface SyncScheduler {
    val estadoSincronizacion: Flow<SyncState>
    fun programarSincronizacionCambios()
    fun programarSincronizacionEnPrimerPlano()
    fun programarSincronizacionPeriodica()
}
