package com.TallerJC.motogest.ui.taller

import com.TallerJC.motogest.data.local.dao.WorkOrderDao
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.SparePartEntity
import com.TallerJC.motogest.data.local.entity.WorkItemEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.repository.OperacionesRepository
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TallerViewModelTest {

    private lateinit var fakeDao: FakeWorkOrderDao
    private lateinit var repository: OperacionesRepository

    class FakeWorkOrderDao : WorkOrderDao {
        val orders = mutableListOf<WorkOrderEntity>()
        val clients = mutableListOf<ClientEntity>()
        val motorcycles = mutableListOf<MotorcycleEntity>()

        val ordersFlow = MutableStateFlow<List<WorkOrderWithDetails>>(emptyList())

        fun emitCurrent() {
            val list = orders.map { o ->
                val c = clients.firstOrNull { it.id == o.clientId } ?: ClientEntity(id = o.clientId, name = "Mock", phone = "000")
                val m = motorcycles.firstOrNull { it.id == o.motorcycleId } ?: MotorcycleEntity(id = o.motorcycleId, clientId = o.clientId, brand = "B", model = "M", color = "C", photoUrl = "")
                WorkOrderWithDetails(order = o, client = c, motorcycle = m, workItems = emptyList(), spareParts = emptyList())
            }
            ordersFlow.value = list
        }

        override fun getAllOrdersWithDetails(): Flow<List<WorkOrderWithDetails>> = ordersFlow
        override fun getOrdersByStatusWithDetails(status: String): Flow<List<WorkOrderWithDetails>> =
            flowOf(ordersFlow.value.filter { it.order.status == status })

        override suspend fun getOrderById(orderId: String): WorkOrderEntity? = orders.find { it.id == orderId }

        override suspend fun insertOrder(order: WorkOrderEntity) {
            orders.removeAll { it.id == order.id }
            orders.add(order)
            emitCurrent()
        }

        override suspend fun insertOrders(orders: List<WorkOrderEntity>) {
            this.orders.addAll(orders)
            emitCurrent()
        }

        override suspend fun insertClient(client: ClientEntity) {
            clients.removeAll { it.id == client.id }
            clients.add(client)
            emitCurrent()
        }

        override suspend fun insertClients(clients: List<ClientEntity>) {
            this.clients.addAll(clients)
            emitCurrent()
        }

        override suspend fun insertMotorcycle(motorcycle: MotorcycleEntity) {
            motorcycles.removeAll { it.id == motorcycle.id }
            motorcycles.add(motorcycle)
            emitCurrent()
        }

        override suspend fun insertMotorcycles(motorcycles: List<MotorcycleEntity>) {
            this.motorcycles.addAll(motorcycles)
            emitCurrent()
        }

        val workItems = mutableListOf<WorkItemEntity>()
        val spareParts = mutableListOf<SparePartEntity>()

        override suspend fun insertWorkItem(item: WorkItemEntity) { workItems.add(item) }
        override suspend fun insertWorkItems(items: List<WorkItemEntity>) { workItems.addAll(items) }
        override suspend fun insertSparePart(part: SparePartEntity) { spareParts.add(part) }
        override suspend fun insertSpareParts(parts: List<SparePartEntity>) { spareParts.addAll(parts) }
        override suspend fun countOrders(): Int = orders.size
        override suspend fun updateMotorcycleVin(motoId: String, nuevoVin: String) {
            val idx = motorcycles.indexOfFirst { it.id == motoId }
            if (idx != -1) {
                motorcycles[idx] = motorcycles[idx].copy(vin = nuevoVin)
                emitCurrent()
            }
        }

        override fun getAllClients(): Flow<List<ClientEntity>> = MutableStateFlow(clients)
        override suspend fun getClientById(clientId: String): ClientEntity? = clients.find { it.id == clientId }
        override fun getMotorcyclesByClientId(clientId: String): Flow<List<MotorcycleEntity>> = MutableStateFlow(motorcycles.filter { it.clientId == clientId })
        override suspend fun getMotorcycleById(motoId: String): MotorcycleEntity? = motorcycles.find { it.id == motoId }
        override fun getOrdersByMotorcycleId(motorcycleId: String): Flow<List<WorkOrderWithDetails>> = flowOf(ordersFlow.value.filter { it.motorcycle.id == motorcycleId })
        override fun getOrdersByClientId(clientId: String): Flow<List<WorkOrderWithDetails>> = flowOf(ordersFlow.value.filter { it.client.id == clientId })
        override suspend fun getLaborTotal(orderId: String): Double = workItems.filter { it.orderId == orderId }.sumOf { it.cost }
        override suspend fun getPartsTotal(orderId: String): Double = spareParts.filter { it.orderId == orderId }.sumOf { it.cost }
        override suspend fun updateOrderFinancialTotals(orderId: String, labor: Double, parts: Double, updatedAt: Long) {
            val idx = orders.indexOfFirst { it.id == orderId }
            if (idx != -1) {
                orders[idx] = orders[idx].copy(totalLaborCost = labor, totalPartsCost = parts, totalAmount = labor + parts + orders[idx].deliveryCost, updatedAt = updatedAt)
                emitCurrent()
            }
        }
        override suspend fun updateRecommendations(orderId: String, recommendations: String, updatedAt: Long) {
            val idx = orders.indexOfFirst { it.id == orderId }
            if (idx != -1) {
                orders[idx] = orders[idx].copy(recommendations = recommendations, updatedAt = updatedAt)
                emitCurrent()
            }
        }
        override suspend fun updateOrderStatus(orderId: String, status: String, startedAt: Long?, completedAt: Long?, deliveredAt: Long?, updatedAt: Long) {
            val idx = orders.indexOfFirst { it.id == orderId }
            if (idx != -1) {
                orders[idx] = orders[idx].copy(
                    status = status,
                    startedAt = startedAt ?: orders[idx].startedAt,
                    completedAt = completedAt ?: orders[idx].completedAt,
                    deliveredAt = deliveredAt ?: orders[idx].deliveredAt,
                    updatedAt = updatedAt
                )
                emitCurrent()
            }
        }
        override suspend fun updateClientDeliveryCost(clientId: String, cost: Double) {
            val idx = clients.indexOfFirst { it.id == clientId }
            if (idx != -1) {
                clients[idx] = clients[idx].copy(defaultDeliveryCost = cost)
                emitCurrent()
            }
        }
        override suspend fun deleteOrderById(orderId: String) {
            orders.removeAll { it.id == orderId }
            emitCurrent()
        }
        override suspend fun deleteClientById(clientId: String) {
            clients.removeAll { it.id == clientId }
            emitCurrent()
        }
        override suspend fun deleteMotorcycleById(motoId: String) {
            motorcycles.removeAll { it.id == motoId }
            emitCurrent()
        }
        override suspend fun getUnsyncedOrders(): List<WorkOrderEntity> = orders.filter { !it.isSynced }
        override suspend fun markOrderAsSynced(orderId: String) {
            val idx = orders.indexOfFirst { it.id == orderId }
            if (idx != -1) {
                orders[idx] = orders[idx].copy(isSynced = true)
                emitCurrent()
            }
        }
        override suspend fun getUnsyncedClients(): List<ClientEntity> = clients.filter { !it.isSynced }
        override suspend fun markClientAsSynced(clientId: String) {
            val idx = clients.indexOfFirst { it.id == clientId }
            if (idx != -1) {
                clients[idx] = clients[idx].copy(isSynced = true)
                emitCurrent()
            }
        }
        override suspend fun getUnsyncedMotorcycles(): List<MotorcycleEntity> = motorcycles.filter { !it.isSynced }
        override suspend fun markMotorcycleAsSynced(motoId: String) {
            val idx = motorcycles.indexOfFirst { it.id == motoId }
            if (idx != -1) {
                motorcycles[idx] = motorcycles[idx].copy(isSynced = true)
                emitCurrent()
            }
        }
    }

    @Before
    fun setUp() {
        fakeDao = FakeWorkOrderDao()
        repository = OperacionesRepository(fakeDao)
    }

    private fun crearOrdenPrueba(
        id: String,
        folio: String,
        status: OrderStatus,
        deliveryType: DeliveryType = DeliveryType.TALLER
    ): WorkOrderWithDetails {
        val client = ClientEntity(id = "cli_$id", name = "Cliente $id", phone = "4451234567")
        val moto = MotorcycleEntity(id = "moto_$id", clientId = client.id, brand = "Vento", model = "Nitrox", color = "Rojo", photoUrl = "")
        val order = WorkOrderEntity(
            id = id,
            folio = folio,
            clientId = client.id,
            motorcycleId = moto.id,
            receivedByUserId = "u1",
            status = status.name,
            serviceType = ServiceType.SERVICIO_GENERAL.name,
            reportedFailure = "Mantenimiento",
            deliveryType = deliveryType.name,
            totalLaborCost = 300.0,
            totalPartsCost = 200.0,
            totalAmount = 500.0
        )

        fakeDao.clients.add(client)
        fakeDao.motorcycles.add(moto)
        fakeDao.orders.add(order)
        fakeDao.emitCurrent()

        return WorkOrderWithDetails(order, client, moto, emptyList(), emptyList())
    }

    @Test
    fun testTransicionDeEstadosTaller() = runBlocking {
        val detalle = crearOrdenPrueba("1", "ORD-101", OrderStatus.RECIBIDA)
        val viewModel = TallerViewModel(repository, Dispatchers.Unconfined)

        viewModel.cambiarEstadoOrden(detalle, OrderStatus.EN_PROCESO)
        var ordenDb = fakeDao.getOrderById("1")
        assertEquals(OrderStatus.EN_PROCESO.name, ordenDb?.status)
        assertNotNull(ordenDb?.startedAt)

        viewModel.cambiarEstadoOrden(detalle.copy(order = ordenDb!!), OrderStatus.LISTA_ENTREGA)
        ordenDb = fakeDao.getOrderById("1")
        assertEquals(OrderStatus.LISTA_ENTREGA.name, ordenDb?.status)
        assertNotNull(ordenDb?.completedAt)
    }

    @Test
    fun testEntregarOrdenEnTallerDirectamente() = runBlocking {
        val detalle = crearOrdenPrueba("1", "ORD-102", OrderStatus.LISTA_ENTREGA)
        val viewModel = TallerViewModel(repository, Dispatchers.Unconfined)

        viewModel.entregarOrdenEnTaller(detalle)

        val ordenDb = fakeDao.getOrderById("1")
        assertEquals(OrderStatus.ENTREGADA.name, ordenDb?.status)
        assertEquals(DeliveryType.TALLER.name, ordenDb?.deliveryType)
        assertEquals(0.0, ordenDb?.deliveryCost ?: -1.0, 0.001)
        assertNotNull(ordenDb?.deliveredAt)
    }

    @Test
    fun testAgregarTrabajoMecanicoYRefaccionesPreservaCostos() = runBlocking {
        val detalle = crearOrdenPrueba("2", "ORD-103", OrderStatus.EN_PROCESO)
        val viewModel = TallerViewModel(repository, Dispatchers.Unconfined)

        // Agregamos mano de obra
        viewModel.agregarTrabajoMecanico(detalle, "Ajuste de frenos", "150.0")
        // Agregamos refacción
        viewModel.agregarRefaccion(detalle, "Juego de balatas", "250.0")
        // Guardamos recomendaciones técnicas y VIN
        viewModel.guardarRecomendacionesYVin(detalle, "Revisar líquido de frenos pronto", "VIN999888")

        val ordenDb = fakeDao.getOrderById("2")
        assertNotNull(ordenDb)
        // Los costos se calcularon atómicamente y NO se borraron al guardar recomendaciones
        assertEquals(150.0, ordenDb?.totalLaborCost ?: 0.0, 0.001)
        assertEquals(250.0, ordenDb?.totalPartsCost ?: 0.0, 0.001)
        assertEquals(400.0, ordenDb?.totalAmount ?: 0.0, 0.001)
        assertEquals("Revisar líquido de frenos pronto", ordenDb?.recommendations)

        val motoDb = fakeDao.getMotorcycleById(detalle.motorcycle.id)
        assertEquals("VIN999888", motoDb?.vin)
    }
}
