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
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.fake.FakeWorkOrderDao
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
        var online: Boolean = true
    ) : RemoteWorkOrderDataSource {
        val orders = linkedMapOf<String, WorkOrderDto>()
        val clients = linkedMapOf<String, ClientDto>()
        val motorcycles = linkedMapOf<String, MotorcycleDto>()

        override suspend fun fetchOrders(): Result<List<WorkOrderDto>> {
            return if (online) Result.success(orders.values.toList())
            else Result.failure(IOException("Sin red"))
        }

        override suspend fun uploadOrder(order: WorkOrderDto): Boolean {
            if (!online) return false
            orders[order.id] = order
            return true
        }

        override suspend fun deleteOrder(orderId: String): Boolean {
            if (!online) return false
            orders.remove(orderId)
            return true
        }

        override suspend fun fetchClients(): Result<List<ClientDto>> {
            return if (online) Result.success(clients.values.toList())
            else Result.failure(IOException("Sin red"))
        }

        override suspend fun uploadClient(client: ClientDto): Boolean {
            if (!online) return false
            clients[client.id] = client
            return true
        }

        override suspend fun fetchMotorcycles(): Result<List<MotorcycleDto>> {
            return if (online) Result.success(motorcycles.values.toList())
            else Result.failure(IOException("Sin red"))
        }

        override suspend fun uploadMotorcycle(moto: MotorcycleDto): Boolean {
            if (!online) return false
            motorcycles[moto.id] = moto
            return true
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
}
