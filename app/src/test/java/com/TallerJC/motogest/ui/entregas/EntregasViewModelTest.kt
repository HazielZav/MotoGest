package com.TallerJC.motogest.ui.entregas

import com.TallerJC.motogest.data.local.dao.WorkOrderDao
import com.TallerJC.motogest.data.local.entity.*
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

class EntregasViewModelTest {

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

        override fun getOrdersByStatusWithDetails(status: String): Flow<List<WorkOrderWithDetails>> {
            return flowOf(ordersFlow.value.filter { it.order.status == status })
        }

        override suspend fun getOrderById(orderId: String): WorkOrderEntity? {
            return orders.find { it.id == orderId }
        }

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

        override suspend fun insertMotorcycle(motorcycle: MotorcycleEntity) {
            motorcycles.removeAll { it.id == motorcycle.id }
            motorcycles.add(motorcycle)
            emitCurrent()
        }

        override suspend fun insertWorkItem(item: WorkItemEntity) {}
        override suspend fun insertSparePart(part: SparePartEntity) {}
        override suspend fun updateMotorcycleVin(motoId: String, nuevoVin: String) {}
    }

    @Before
    fun setUp() {
        fakeDao = FakeWorkOrderDao()
        repository = OperacionesRepository(fakeDao)
    }

    private fun crearOrdenDomicilio(
        id: String,
        folio: String,
        status: OrderStatus,
        deliveryCost: Double = 0.0,
        deliveryType: DeliveryType = DeliveryType.DOMICILIO
    ): WorkOrderWithDetails {
        val client = ClientEntity(
            id = "cli_$id",
            name = "Cliente $id",
            phone = "4451234567",
            address = "Calle Prueba $id",
            defaultDeliveryCost = 0.0
        )
        val moto = MotorcycleEntity(
            id = "moto_$id",
            clientId = client.id,
            brand = "Italika",
            model = "DM200",
            color = "Amarillo",
            photoUrl = ""
        )
        val order = WorkOrderEntity(
            id = id,
            folio = folio,
            clientId = client.id,
            motorcycleId = moto.id,
            receivedByUserId = "u1",
            status = status.name,
            serviceType = ServiceType.SERVICIO_GENERAL.name,
            reportedFailure = "Afinación",
            deliveryType = deliveryType.name,
            deliveryCost = deliveryCost,
            totalLaborCost = 200.0,
            totalPartsCost = 100.0,
            totalAmount = 300.0 + deliveryCost
        )

        fakeDao.clients.add(client)
        fakeDao.motorcycles.add(moto)
        fakeDao.orders.add(order)
        fakeDao.emitCurrent()

        return WorkOrderWithDetails(
            order = order,
            client = client,
            motorcycle = moto,
            workItems = emptyList(),
            spareParts = emptyList()
        )
    }

    @Test
    fun testFiltroOrdenesSoloDomicilioYEstadosPermitidos() = runBlocking {
        // Orden 1: Domicilio en LISTA_ENTREGA -> DEBE APARECER
        crearOrdenDomicilio("1", "ORD-01", OrderStatus.LISTA_ENTREGA, 50.0)
        // Orden 2: Domicilio en EN_CAMINO -> DEBE APARECER
        crearOrdenDomicilio("2", "ORD-02", OrderStatus.EN_CAMINO, 40.0)
        // Orden 3: Domicilio en RECIBIDA -> NO DEBE APARECER
        crearOrdenDomicilio("3", "ORD-03", OrderStatus.RECIBIDA, 50.0)
        // Orden 4: Recoger en TALLER en LISTA_ENTREGA -> NO DEBE APARECER
        crearOrdenDomicilio("4", "ORD-04", OrderStatus.LISTA_ENTREGA, 0.0, DeliveryType.TALLER)
        // Orden 5: Domicilio en ENTREGADA -> NO DEBE APARECER
        crearOrdenDomicilio("5", "ORD-05", OrderStatus.ENTREGADA, 50.0)

        val viewModel = EntregasViewModel(repository, Dispatchers.Unconfined)

        val ordenes = viewModel.estadoUi.value.ordenesParaEntrega
        assertEquals(2, ordenes.size)
        assertTrue(ordenes.any { it.order.folio == "ORD-01" })
        assertTrue(ordenes.any { it.order.folio == "ORD-02" })
    }

    @Test
    fun testTransicionAEnCamino() = runBlocking {
        val orden = crearOrdenDomicilio("1", "ORD-01", OrderStatus.LISTA_ENTREGA, 50.0)
        val viewModel = EntregasViewModel(repository, Dispatchers.Unconfined)

        viewModel.marcarEnCamino(orden)

        val ordenDb = fakeDao.getOrderById("1")
        assertNotNull(ordenDb)
        assertEquals(OrderStatus.EN_CAMINO.name, ordenDb?.status)
    }

    @Test
    fun testAlertaPreventivaCostoCeroAlGuardar() = runBlocking {
        val orden = crearOrdenDomicilio("1", "ORD-01", OrderStatus.LISTA_ENTREGA, 50.0)
        val viewModel = EntregasViewModel(repository, Dispatchers.Unconfined)

        viewModel.abrirDialogoEdicionCosto(orden)
        viewModel.actualizarCostoEnvioTexto("0")
        viewModel.guardarCostoEnvio()

        // Debe desplegar alerta preventiva
        assertTrue(viewModel.estadoUi.value.mostrarAlertaCostoCero)
        assertEquals(AccionCostoCero.GUARDAR_COSTO, viewModel.estadoUi.value.accionPendienteCostoCero)

        // Al confirmar costo cero, se guarda $0.00
        viewModel.confirmarCostoCero()

        assertFalse(viewModel.estadoUi.value.mostrarAlertaCostoCero)
        val ordenDb = fakeDao.getOrderById("1")
        assertEquals(0.0, ordenDb?.deliveryCost ?: -1.0, 0.001)
    }

    @Test
    fun testAlertaPreventivaCostoCeroAlIntentarConfirmarEntrega() = runBlocking {
        val orden = crearOrdenDomicilio("1", "ORD-01", OrderStatus.EN_CAMINO, 0.0)
        val viewModel = EntregasViewModel(repository, Dispatchers.Unconfined)

        viewModel.solicitarConfirmacionEntrega(orden)

        // Como el costo es $0.00, debe disparar la alerta preventiva
        assertTrue(viewModel.estadoUi.value.mostrarAlertaCostoCero)
        assertEquals(AccionCostoCero.MARCAR_ENTREGADA, viewModel.estadoUi.value.accionPendienteCostoCero)

        // Al confirmar entrega con costo cero
        viewModel.confirmarCostoCero()

        val ordenDb = fakeDao.getOrderById("1")
        assertEquals(OrderStatus.ENTREGADA.name, ordenDb?.status)
        assertNotNull(ordenDb?.deliveredAt)

        val clienteDb = fakeDao.clients.find { it.id == "cli_1" }
        assertEquals(0.0, clienteDb?.defaultDeliveryCost ?: -1.0, 0.001)
    }

    @Test
    fun testEntregaExitosaActualizaFichaCliente() = runBlocking {
        val orden = crearOrdenDomicilio("1", "ORD-01", OrderStatus.EN_CAMINO, 65.0)
        val viewModel = EntregasViewModel(repository, Dispatchers.Unconfined)

        viewModel.solicitarConfirmacionEntrega(orden)
        // No debe mostrar alerta de costo cero porque costo > 0
        assertFalse(viewModel.estadoUi.value.mostrarAlertaCostoCero)
        assertTrue(viewModel.estadoUi.value.mostrarDialogoConfirmarEntrega)

        viewModel.confirmarEntregaDirecta()

        val ordenDb = fakeDao.getOrderById("1")
        assertEquals(OrderStatus.ENTREGADA.name, ordenDb?.status)
        assertNotNull(ordenDb?.deliveredAt)

        val clienteDb = fakeDao.clients.find { it.id == "cli_1" }
        assertEquals(65.0, clienteDb?.defaultDeliveryCost ?: -1.0, 0.001)
    }
}
