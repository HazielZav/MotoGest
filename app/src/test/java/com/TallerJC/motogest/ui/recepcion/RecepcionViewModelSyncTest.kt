package com.TallerJC.motogest.ui.recepcion

import com.TallerJC.motogest.data.remote.datasource.RemoteWorkOrderDataSource
import com.TallerJC.motogest.data.remote.dto.ClientDto
import com.TallerJC.motogest.data.remote.dto.MotorcycleDto
import com.TallerJC.motogest.data.remote.dto.WorkOrderDto
import com.TallerJC.motogest.data.repository.OperacionesRepository
import com.TallerJC.motogest.data.sync.SyncScheduler
import com.TallerJC.motogest.data.sync.SyncState
import com.TallerJC.motogest.fake.FakeWorkOrderDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RecepcionViewModelSyncTest {

    private class FakeSyncScheduler : SyncScheduler {
        val estadoFlow = MutableStateFlow(SyncState.IDLE)
        override val estadoSincronizacion: Flow<SyncState> = estadoFlow

        override fun programarSincronizacionCambios() {}
        override fun programarSincronizacionEnPrimerPlano() {}
        override fun programarSincronizacionPeriodica() {}
    }

    private class FakeRemote(var online: Boolean = true) : RemoteWorkOrderDataSource {
        override suspend fun fetchOrders(): Result<List<WorkOrderDto>> =
            if (online) Result.success(emptyList()) else Result.failure(IOException("Sin red"))

        override suspend fun uploadOrder(order: WorkOrderDto): Boolean = online
        override suspend fun deleteOrder(orderId: String): Boolean = online

        override suspend fun fetchClients(): Result<List<ClientDto>> =
            if (online) Result.success(emptyList()) else Result.failure(IOException("Sin red"))

        override suspend fun uploadClient(client: ClientDto): Boolean = online

        override suspend fun fetchMotorcycles(): Result<List<MotorcycleDto>> =
            if (online) Result.success(emptyList()) else Result.failure(IOException("Sin red"))

        override suspend fun uploadMotorcycle(moto: MotorcycleDto): Boolean = online
    }

    @Test
    fun estadoSincronizacionEnSegundoPlanoSeReflejaEnUiState() = runBlocking {
        val scheduler = FakeSyncScheduler()
        val repository = OperacionesRepository(FakeWorkOrderDao(), FakeRemote(), scheduler)
        val viewModel = RecepcionViewModel(repository, Dispatchers.Unconfined)

        assertEquals(SyncState.IDLE, viewModel.estadoUi.value.estadoSincronizacion)
        assertFalse(viewModel.estadoUi.value.sincronizandoNube)

        scheduler.estadoFlow.value = SyncState.PENDING
        assertEquals(SyncState.PENDING, viewModel.estadoUi.value.estadoSincronizacion)
        assertFalse(viewModel.estadoUi.value.sincronizandoNube)

        scheduler.estadoFlow.value = SyncState.SYNCING
        assertEquals(SyncState.SYNCING, viewModel.estadoUi.value.estadoSincronizacion)
        assertTrue(viewModel.estadoUi.value.sincronizandoNube)

        scheduler.estadoFlow.value = SyncState.IDLE
        assertEquals(SyncState.IDLE, viewModel.estadoUi.value.estadoSincronizacion)
        assertFalse(viewModel.estadoUi.value.sincronizandoNube)
    }

    @Test
    fun sincronizarManualMuestraResultadoExitoYError() = runBlocking {
        val remote = FakeRemote(online = false)
        val repository = OperacionesRepository(FakeWorkOrderDao(), remote, FakeSyncScheduler())
        val viewModel = RecepcionViewModel(repository, Dispatchers.Unconfined)

        viewModel.sincronizarConNube()
        assertFalse(viewModel.estadoUi.value.sincronizandoNube)
        assertTrue(viewModel.estadoUi.value.mensajeNube!!.contains("Verifique su conexión"))

        remote.online = true
        viewModel.sincronizarConNube()
        assertFalse(viewModel.estadoUi.value.sincronizandoNube)
        assertTrue(viewModel.estadoUi.value.mensajeNube!!.contains("completada"))
    }
}
