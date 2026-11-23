package com.TallerJC.motogest.data.repository

import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.WorkItemEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.remote.datasource.RemoteWorkOrderDataSource
import com.TallerJC.motogest.data.remote.dto.ClientDto
import com.TallerJC.motogest.data.remote.dto.MotorcycleDto
import com.TallerJC.motogest.data.remote.dto.WorkOrderDto
import com.TallerJC.motogest.data.remote.mapper.toDto
import com.TallerJC.motogest.data.sync.SyncCooldownStore
import com.TallerJC.motogest.data.sync.SyncScheduler
import com.TallerJC.motogest.data.sync.SyncState
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.fake.FakeWorkOrderDao
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class OperacionesRepositorySyncTest {

    private class FakeRemoteDataSource(
        var online: Boolean = true,
        var uploadDelayMs: Long = 0L
    ) : RemoteWorkOrderDataSource {
        val orders = linkedMapOf<String, WorkOrderDto>()
        val clients = linkedMapOf<String, ClientDto>()
        val motorcycles = linkedMapOf<String, MotorcycleDto>()
        var uploadCalls = 0
        var fetchCalls = 0

        override suspend fun fetchOrders(): Result<List<WorkOrderDto>> {
            fetchCalls++
            return if (online) Result.success(orders.values.toList())
            else Result.failure(IOException("Sin red"))
        }

        override suspend fun uploadOrder(order: WorkOrderDto): Boolean {
            if (uploadDelayMs > 0) delay(uploadDelayMs)
            uploadCalls++
            if (!online) return false
            orders[order.id] = order
            return true
        }

        override suspend fun deleteOrder(orderId: String): Boolean {
            uploadCalls++
            if (!online) return false
            orders.remove(orderId)
            return true
        }

        override suspend fun fetchClients(): Result<List<ClientDto>> {
            fetchCalls++
            return if (online) Result.success(clients.values.toList())
            else Result.failure(IOException("Sin red"))
        }

        override suspend fun uploadClient(client: ClientDto): Boolean {
            uploadCalls++
            if (!online) return false
            clients[client.id] = client
            return true
        }

        override suspend fun fetchMotorcycles(): Result<List<MotorcycleDto>> {
            fetchCalls++
            return if (online) Result.success(motorcycles.values.toList())
            else Result.failure(IOException("Sin red"))
        }

        override suspend fun uploadMotorcycle(moto: MotorcycleDto): Boolean {
            uploadCalls++
            if (!online) return false
            motorcycles[moto.id] = moto
            return true
        }
    }

    private class FakeSyncScheduler : SyncScheduler {
        val estadoFlow = MutableStateFlow(SyncState.IDLE)
        override val estadoSincronizacion: Flow<SyncState> = estadoFlow
        var cambiosProgramados = 0

        override fun programarSincronizacionCambios() {
            cambiosProgramados++
        }

        override fun programarSincronizacionEnPrimerPlano() {}
        override fun programarSincronizacionPeriodica() {}
    }

    private class FakeSyncCooldownStore(
        private var ultimoPullMs: Long = 0L
    ) : SyncCooldownStore {
        override fun obtenerUltimoPullMs(): Long = ultimoPullMs
        override fun guardarUltimoPullMs(timestampMs: Long) {
            ultimoPullMs = timestampMs
        }
    }

    private fun crearDetalleBase(synced: Boolean): Triple<ClientEntity, MotorcycleEntity, WorkOrderEntity> {
        val client = ClientEntity(
            id = "cli-1",
            name = "Carlos Perez",
            phone = "4451112233",
            isSynced = synced
        )
        val moto = MotorcycleEntity(
            id = "moto-1",
            clientId = client.id,
            brand = "Yamaha",
            model = "FZ",
            color = "Negro",
            photoUrl = "",
            isSynced = synced
        )
        val order = WorkOrderEntity(
            id = "ord-1",
            folio = "ORD-100",
            clientId = client.id,
            motorcycleId = moto.id,
            receivedByUserId = "u1",
            status = OrderStatus.RECIBIDA.name,
            serviceType = ServiceType.SERVICIO_GENERAL.name,
            reportedFailure = "Falla",
            intakeDamageNotes = "Rayon tanque",
            intakePhotos = listOf("local://foto1.jpg"),
            deliveryType = DeliveryType.DOMICILIO.name,
            isSynced = synced
        )
        return Triple(client, moto, order)
    }

    @Test
    fun escriturasLocalesProgramanSyncSinLlamarRed() = runBlocking {
        val (client, moto, order) = crearDetalleBase(synced = false)
        val dao = FakeWorkOrderDao()
        val remote = FakeRemoteDataSource(online = true)
        val scheduler = FakeSyncScheduler()
        val repository = OperacionesRepository(dao, remote, scheduler)

        repository.registrarNuevaOrden(client, moto, order)
        repository.cambiarEstadoOrden(order, OrderStatus.EN_PROCESO)
        repository.agregarManoDeObra(order, "Ajuste", 200.0)
        repository.agregarRefaccion(order, "Bujia", 100.0)
        repository.actualizarNotasYVinTaller(order, "Todo bien", "VIN123")
        repository.actualizarCostoEnvio(dao.getOrderById(order.id)!!, 50.0)
        repository.marcarOrdenEntregada(dao.getOrderById(order.id)!!, client, 50.0, DeliveryType.DOMICILIO)

        assertEquals(7, scheduler.cambiosProgramados)
        assertEquals(0, remote.uploadCalls)
        assertEquals(0, remote.fetchCalls)
        assertFalse(dao.getOrderById(order.id)!!.isSynced)
    }

    @Test
    fun mutacionesOfflineMarcanPendienteYLuegoSincronizan() = runBlocking {
        val (client, moto, order) = crearDetalleBase(synced = true)
        val dao = FakeWorkOrderDao(
            initialOrders = listOf(WorkOrderWithDetails(order, client, moto, emptyList(), emptyList())),
            initialClients = listOf(client),
            initialMotorcycles = listOf(moto)
        )
        val remote = FakeRemoteDataSource(online = false)
        val repository = OperacionesRepository(dao, remote)

        repository.cambiarEstadoOrden(order, OrderStatus.EN_PROCESO)
        repository.agregarManoDeObra(order, "Afinacion", 300.0)
        repository.agregarRefaccion(order, "Bujia", 120.0)
        repository.actualizarNotasYVinTaller(order, "Cambiar filtro", "VIN-777")
        repository.marcarOrdenEntregada(dao.getOrderById(order.id)!!, client, 75.0, DeliveryType.DOMICILIO)

        assertFalse(dao.getOrderById(order.id)!!.isSynced)
        assertFalse(dao.getMotorcycleById(moto.id)!!.isSynced)
        assertFalse(dao.getClientById(client.id)!!.isSynced)
        assertFalse(repository.sincronizarConNube())

        remote.online = true
        assertTrue(repository.sincronizarConNube())

        assertTrue(dao.getOrderById(order.id)!!.isSynced)
        assertTrue(dao.getMotorcycleById(moto.id)!!.isSynced)
        assertTrue(dao.getClientById(client.id)!!.isSynced)
        assertEquals(OrderStatus.ENTREGADA.name, remote.orders[order.id]?.status)
        assertEquals(495.0, remote.orders[order.id]?.totalAmount ?: 0.0, 0.001)
        assertEquals("VIN-777", remote.motorcycles[moto.id]?.vin)
        assertEquals(75.0, remote.clients[client.id]?.defaultDeliveryCost ?: 0.0, 0.001)
    }

    @Test
    fun eliminacionOfflineNoResucitaEnPullYSeBorraAlRecuperarRed() = runBlocking {
        val (client, moto, order) = crearDetalleBase(synced = true)
        val dao = FakeWorkOrderDao(
            initialOrders = listOf(WorkOrderWithDetails(order, client, moto, emptyList(), emptyList())),
            initialClients = listOf(client),
            initialMotorcycles = listOf(moto)
        )
        val remote = FakeRemoteDataSource(online = true).apply {
            clients[client.id] = client.toDto()
            motorcycles[moto.id] = moto.toDto()
            orders[order.id] = order.toDto()
        }
        val repository = OperacionesRepository(dao, remote)

        remote.online = false
        repository.eliminarOrden(order.id)

        assertNull(dao.getOrderById(order.id))
        assertEquals(1, dao.getPendingDeletions().size)
        assertNotNull(remote.orders[order.id])

        remote.online = true
        assertTrue(repository.sincronizarConNube())

        assertNull(dao.getOrderById(order.id))
        assertNull(remote.orders[order.id])
        assertTrue(dao.getPendingDeletions().isEmpty())
    }

    @Test
    fun pullNoSobrescribeCambiosLocalesPendientesYPreservaCamposLocales() = runBlocking {
        val (client, moto, order) = crearDetalleBase(synced = false)
        val dao = FakeWorkOrderDao(
            initialOrders = listOf(WorkOrderWithDetails(order, client, moto, emptyList(), emptyList())),
            initialClients = listOf(client),
            initialMotorcycles = listOf(moto)
        )
        val remote = FakeRemoteDataSource(online = true).apply {
            clients[client.id] = client.toDto()
            motorcycles[moto.id] = moto.toDto()
            orders[order.id] = order.copy(status = OrderStatus.LISTA_ENTREGA.name).toDto().copy(
                intakeDamageNotes = null,
                intakePhotos = null
            )
        }
        val failingUploadRemote = object : RemoteWorkOrderDataSource by remote {
            override suspend fun uploadOrder(order: WorkOrderDto): Boolean = false
        }
        val repository = OperacionesRepository(dao, failingUploadRemote)

        val exito = repository.sincronizarConNube()

        assertFalse(exito)
        val localOrder = dao.getOrderById(order.id)!!
        assertFalse(localOrder.isSynced)
        assertEquals(OrderStatus.RECIBIDA.name, localOrder.status)
        assertEquals("Rayon tanque", localOrder.intakeDamageNotes)
        assertEquals(listOf("local://foto1.jpg"), localOrder.intakePhotos)
    }

    @Test
    fun pullActualizaOrdenSincronizadaPreservandoCamposLocalesYDetalles() = runBlocking {
        val (client, moto, order) = crearDetalleBase(synced = true)
        val workItem = WorkItemEntity(
            id = "wi-1",
            orderId = order.id,
            description = "Cambio aceite",
            cost = 250.0
        )
        val dao = FakeWorkOrderDao(
            initialOrders = listOf(WorkOrderWithDetails(order, client, moto, listOf(workItem), emptyList())),
            initialClients = listOf(client),
            initialMotorcycles = listOf(moto)
        )
        val remote = FakeRemoteDataSource(online = true).apply {
            clients[client.id] = client.toDto()
            motorcycles[moto.id] = moto.toDto()
            orders[order.id] = order.copy(status = OrderStatus.LISTA_ENTREGA.name).toDto().copy(
                intakeDamageNotes = null,
                intakePhotos = null
            )
        }
        val repository = OperacionesRepository(dao, remote)

        assertTrue(repository.sincronizarConNube())

        val updated = dao.ordersList.first { it.order.id == order.id }
        assertEquals(OrderStatus.LISTA_ENTREGA.name, updated.order.status)
        assertEquals("Rayon tanque", updated.order.intakeDamageNotes)
        assertEquals(listOf("local://foto1.jpg"), updated.order.intakePhotos)
        assertEquals(1, updated.workItems.size)
    }

    @Test
    fun syncAutomaticoRespetaCooldownParaEvitarDescargasRepetidas() = runBlocking {
        var relojMs = 1_000_000L
        val (client, moto, order) = crearDetalleBase(synced = false)
        val dao = FakeWorkOrderDao(
            initialOrders = listOf(WorkOrderWithDetails(order, client, moto, emptyList(), emptyList())),
            initialClients = listOf(client),
            initialMotorcycles = listOf(moto)
        )
        val remote = FakeRemoteDataSource(online = true)
        val cooldown = FakeSyncCooldownStore(ultimoPullMs = relojMs - 30_000L)
        val repository = OperacionesRepository(
            workOrderDao = dao,
            remoteDataSource = remote,
            syncCooldownStore = cooldown,
            ahoraProvider = { relojMs }
        )

        assertTrue(repository.sincronizarAutomaticamente(forzarPull = false))
        assertEquals(3, remote.uploadCalls)
        assertEquals(0, remote.fetchCalls)

        assertTrue(repository.sincronizarAutomaticamente(forzarPull = false))
        assertEquals(3, remote.uploadCalls)
        assertEquals(0, remote.fetchCalls)

        relojMs += OperacionesRepository.PULL_COOLDOWN_MS
        assertTrue(repository.sincronizarAutomaticamente(forzarPull = false))
        assertEquals(3, remote.fetchCalls)
        assertEquals(relojMs, cooldown.obtenerUltimoPullMs())
    }

    @Test
    fun syncManualFuerzaPullYReprogramaPendientesCuandoFalla() = runBlocking {
        val relojMs = 500_000L
        val (client, moto, order) = crearDetalleBase(synced = false)
        val dao = FakeWorkOrderDao(
            initialOrders = listOf(WorkOrderWithDetails(order, client, moto, emptyList(), emptyList())),
            initialClients = listOf(client),
            initialMotorcycles = listOf(moto)
        )
        val remote = FakeRemoteDataSource(online = false)
        val scheduler = FakeSyncScheduler()
        val cooldown = FakeSyncCooldownStore(ultimoPullMs = relojMs - 5_000L)
        val repository = OperacionesRepository(
            workOrderDao = dao,
            remoteDataSource = remote,
            syncScheduler = scheduler,
            syncCooldownStore = cooldown,
            ahoraProvider = { relojMs }
        )

        assertFalse(repository.sincronizarConNube())
        assertEquals(1, scheduler.cambiosProgramados)
        assertEquals(3, remote.fetchCalls)
        assertEquals(relojMs - 5_000L, cooldown.obtenerUltimoPullMs())

        remote.online = true
        assertTrue(repository.sincronizarConNube())
        assertEquals(6, remote.fetchCalls)
        assertEquals(relojMs, cooldown.obtenerUltimoPullMs())
    }

    @Test
    fun sincronizacionesConcurrentesSeSerializanSinDuplicarSubidas() = runBlocking {
        val (client, moto, order) = crearDetalleBase(synced = true)
        val unsyncedOrder = order.copy(isSynced = false)
        val dao = FakeWorkOrderDao(
            initialOrders = listOf(WorkOrderWithDetails(unsyncedOrder, client, moto, emptyList(), emptyList())),
            initialClients = listOf(client),
            initialMotorcycles = listOf(moto)
        )
        val remote = FakeRemoteDataSource(online = true, uploadDelayMs = 40L)
        val repository = OperacionesRepository(dao, remote)

        val resultados = listOf(
            async { repository.sincronizarAutomaticamente(forzarPull = false) },
            async { repository.sincronizarAutomaticamente(forzarPull = false) }
        ).awaitAll()

        assertTrue(resultados.all { it })
        assertEquals(1, remote.uploadCalls)
    }
}
