package com.TallerJC.motogest.data.sync

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkManagerSyncSchedulerTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var scheduler: WorkManagerSyncScheduler

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
        scheduler = WorkManagerSyncScheduler(context)
    }

    @Test
    fun programaTrabajosUnicosConRestriccionDeRedConectada() {
        scheduler.programarSincronizacionCambios()
        scheduler.programarSincronizacionEnPrimerPlano()
        scheduler.programarSincronizacionPeriodica()

        val cambios = workManager.getWorkInfosForUniqueWork("motogest_sync_cambios").get()
        val primerPlano = workManager.getWorkInfosForUniqueWork("motogest_sync_primer_plano").get()
        val periodica = workManager.getWorkInfosForUniqueWork("motogest_sync_periodica").get()

        assertEquals(1, cambios.size)
        assertEquals(WorkInfo.State.ENQUEUED, cambios.first().state)
        assertEquals(NetworkType.CONNECTED, cambios.first().constraints.requiredNetworkType)

        assertEquals(1, primerPlano.size)
        assertEquals(NetworkType.CONNECTED, primerPlano.first().constraints.requiredNetworkType)

        assertEquals(1, periodica.size)
        assertEquals(NetworkType.CONNECTED, periodica.first().constraints.requiredNetworkType)
        assertTrue(periodica.first().constraints.requiresBatteryNotLow())
    }
}
