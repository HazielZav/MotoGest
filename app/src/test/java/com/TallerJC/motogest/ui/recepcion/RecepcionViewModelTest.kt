package com.TallerJC.motogest.ui.recepcion

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

class RecepcionViewModelTest {

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

        override suspend fun insertWorkItem(item: WorkItemEntity) {}
        override suspend fun insertWorkItems(items: List<WorkItemEntity>) {}
        override suspend fun insertSparePart(part: SparePartEntity) {}
        override suspend fun insertSpareParts(parts: List<SparePartEntity>) {}
        override suspend fun countOrders(): Int = orders.size
        override suspend fun updateMotorcycleVin(motoId: String, nuevoVin: String) {}

        override fun getAllClients(): Flow<List<ClientEntity>> = MutableStateFlow(clients)
        override suspend fun getClientById(clientId: String): ClientEntity? = clients.find { it.id == clientId }
        override fun getMotorcyclesByClientId(clientId: String): Flow<List<MotorcycleEntity>> = MutableStateFlow(motorcycles.filter { it.clientId == clientId })
        override suspend fun getMotorcycleById(motoId: String): MotorcycleEntity? = motorcycles.find { it.id == motoId }
        override fun getOrdersByMotorcycleId(motorcycleId: String): Flow<List<WorkOrderWithDetails>> = flowOf(ordersFlow.value.filter { it.motorcycle.id == motorcycleId })
        override fun getOrdersByClientId(clientId: String): Flow<List<WorkOrderWithDetails>> = flowOf(ordersFlow.value.filter { it.client.id == clientId })
        override suspend fun getLaborTotal(orderId: String): Double = orders.find { it.id == orderId }?.totalLaborCost ?: 0.0
        override suspend fun getPartsTotal(orderId: String): Double = orders.find { it.id == orderId }?.totalPartsCost ?: 0.0
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
        nombreCliente: String,
        marcaMoto: String,
        modeloMoto: String,
        status: OrderStatus,
        deliveryType: DeliveryType
    ): WorkOrderWithDetails {
        val client = ClientEntity(id = "cli_$id", name = nombreCliente, phone = "4451234567")
        val moto = MotorcycleEntity(id = "moto_$id", clientId = client.id, brand = marcaMoto, model = modeloMoto, color = "Negro", photoUrl = "")
        val order = WorkOrderEntity(
            id = id,
            folio = folio,
            clientId = client.id,
            motorcycleId = moto.id,
            receivedByUserId = "u1",
            status = status.name,
            serviceType = ServiceType.SERVICIO_GENERAL.name,
            reportedFailure = "Falla prueba",
            deliveryType = deliveryType.name,
            totalLaborCost = 250.0,
            totalPartsCost = 150.0,
            totalAmount = 400.0
        )

        fakeDao.clients.add(client)
        fakeDao.motorcycles.add(moto)
        fakeDao.orders.add(order)
        fakeDao.emitCurrent()

        return WorkOrderWithDetails(order, client, moto, emptyList(), emptyList())
    }

    @Test
    fun testObservarOrdenesListasParaEntregaEnTaller() = runBlocking {
        crearOrdenPrueba("1", "ORD-01", "Pedro Gómez", "Italika", "FT150", OrderStatus.RECIBIDA, DeliveryType.TALLER)
        crearOrdenPrueba("2", "ORD-02", "Ana Martínez", "Yamaha", "R15", OrderStatus.LISTA_ENTREGA, DeliveryType.TALLER)
        crearOrdenPrueba("3", "ORD-03", "Luis Soto", "Honda", "XR150", OrderStatus.LISTA_ENTREGA, DeliveryType.DOMICILIO)
        crearOrdenPrueba("4", "ORD-04", "Sofía Cruz", "Bajaj", "Pulsar", OrderStatus.ENTREGADA, DeliveryType.TALLER)

        val viewModel = RecepcionViewModel(repository, Dispatchers.Unconfined)

        val listas = viewModel.estadoUi.value.ordenesListasParaEntrega
        assertEquals(2, listas.size)
        assertTrue(listas.any { it.order.folio == "ORD-02" })
        assertTrue(listas.any { it.order.folio == "ORD-03" })
    }

    @Test
    fun testCambioDePestanaYBusqueda() = runBlocking {
        crearOrdenPrueba("1", "ORD-01", "Juan Carlos", "Italika", "DM200", OrderStatus.LISTA_ENTREGA, DeliveryType.TALLER)
        crearOrdenPrueba("2", "ORD-02", "María Elena", "Yamaha", "FZ", OrderStatus.LISTA_ENTREGA, DeliveryType.DOMICILIO)

        val viewModel = RecepcionViewModel(repository, Dispatchers.Unconfined)

        assertEquals(0, viewModel.estadoUi.value.pestanaSeleccionada)
        viewModel.cambiarPestana(1)
        assertEquals(1, viewModel.estadoUi.value.pestanaSeleccionada)

        // Buscar por cliente
        viewModel.actualizarBusquedaEntrega("María")
        assertEquals(1, viewModel.estadoUi.value.ordenesFiltradasEntrega.size)
        assertEquals("María Elena", viewModel.estadoUi.value.ordenesFiltradasEntrega.first().client.name)

        // Buscar por folio
        viewModel.actualizarBusquedaEntrega("ORD-01")
        assertEquals(1, viewModel.estadoUi.value.ordenesFiltradasEntrega.size)
        assertEquals("Juan Carlos", viewModel.estadoUi.value.ordenesFiltradasEntrega.first().client.name)

        // Filtro por modalidad TALLER
        viewModel.actualizarBusquedaEntrega("")
        viewModel.cambiarFiltroModalidadEntrega(DeliveryType.TALLER)
        assertEquals(1, viewModel.estadoUi.value.ordenesFiltradasEntrega.size)
        assertEquals("ORD-01", viewModel.estadoUi.value.ordenesFiltradasEntrega.first().order.folio)
    }

    @Test
    fun testConfirmarEntregaEnTaller() = runBlocking {
        val orden = crearOrdenPrueba("1", "ORD-01", "Roberto Ruiz", "Hero", "Hunk", OrderStatus.LISTA_ENTREGA, DeliveryType.TALLER)
        val viewModel = RecepcionViewModel(repository, Dispatchers.Unconfined)

        viewModel.solicitarEntregaEnTaller(orden)
        assertTrue(viewModel.estadoUi.value.mostrarDialogoConfirmarEntrega)
        assertNotNull(viewModel.estadoUi.value.ordenAEntregar)

        viewModel.confirmarEntregaEnTaller()

        assertFalse(viewModel.estadoUi.value.mostrarDialogoConfirmarEntrega)
        assertNull(viewModel.estadoUi.value.ordenAEntregar)
        assertNotNull(viewModel.estadoUi.value.mensajeEntregaExitosa)

        val ordenDb = fakeDao.getOrderById("1")
        assertEquals(OrderStatus.ENTREGADA.name, ordenDb?.status)
        assertEquals(DeliveryType.TALLER.name, ordenDb?.deliveryType)
        assertEquals(0.0, ordenDb?.deliveryCost ?: -1.0, 0.001)
        assertNotNull(ordenDb?.deliveredAt)
    }

    @Test
    fun testSugerenciaYSeleccionClienteExistenteConMoto() = runBlocking {
        val clienteExistente = ClientEntity(
            id = "c_existente_1",
            name = "Pedro Pascal",
            phone = "4451234567",
            address = "Av. Principal 123"
        )
        val motoExistente = MotorcycleEntity(
            id = "m_existente_1",
            clientId = clienteExistente.id,
            brand = "Yamaha",
            model = "YZF-R3",
            color = "Azul",
            photoUrl = "local://yamaha.jpg",
            plate = "YAM-99"
        )
        fakeDao.insertClient(clienteExistente)
        fakeDao.insertMotorcycle(motoExistente)

        val viewModel = RecepcionViewModel(repository, Dispatchers.Unconfined)

        // Al escribir 3 letras del nombre, se sugieren clientes coincidentes
        viewModel.actualizarNombreCliente("Ped")
        assertTrue(viewModel.estadoUi.value.mostrarSugerenciasClientes)
        assertEquals(1, viewModel.estadoUi.value.clientesSugeridos.size)
        assertEquals("Pedro Pascal", viewModel.estadoUi.value.clientesSugeridos.first().name)

        // Seleccionamos el cliente sugerido
        viewModel.seleccionarCliente(clienteExistente)
        assertEquals(clienteExistente, viewModel.estadoUi.value.clienteSeleccionado)
        assertEquals("Pedro Pascal", viewModel.estadoUi.value.nombreCliente)
        assertEquals("4451234567", viewModel.estadoUi.value.telefonoCliente)
        assertEquals("Av. Principal 123", viewModel.estadoUi.value.direccionCliente)
        assertFalse(viewModel.estadoUi.value.mostrarSugerenciasClientes)
        assertEquals(1, viewModel.estadoUi.value.motosClienteSeleccionado.size)

        // Seleccionamos la motocicleta registrada del cliente
        viewModel.seleccionarMoto(motoExistente)
        assertEquals(motoExistente, viewModel.estadoUi.value.motoSeleccionada)
        assertEquals("Yamaha", viewModel.estadoUi.value.marcaMoto)
        assertEquals("YZF-R3", viewModel.estadoUi.value.modeloMoto)
        assertEquals("Azul", viewModel.estadoUi.value.colorMoto)
        assertEquals("YAM-99", viewModel.estadoUi.value.placaOpcional)

        // Completamos motivo y guardamos la orden
        viewModel.actualizarFallaReportada("Mantenimiento de 5000 km")
        viewModel.guardarOrden()

        assertTrue(viewModel.estadoUi.value.ordenGuardadaConExito)
        val ordenesCreadas = fakeDao.orders.filter { it.clientId == clienteExistente.id }
        assertEquals(1, ordenesCreadas.size)
        // La orden preserva el ID del cliente y de la moto existentes sin duplicarlos
        assertEquals(clienteExistente.id, ordenesCreadas.first().clientId)
        assertEquals(motoExistente.id, ordenesCreadas.first().motorcycleId)
    }

    @Test
    fun testRegistroNuevoClienteYValidacionTelefono() = runBlocking {
        val viewModel = RecepcionViewModel(repository, Dispatchers.Unconfined)

        viewModel.abrirDialogoNuevoCliente()
        assertTrue(viewModel.estadoUi.value.mostrarDialogoNuevoCliente)

        // Intento guardar sin nombre ni teléfono de 10 dígitos
        viewModel.actualizarNuevoClienteNombre("")
        viewModel.actualizarNuevoClienteTelefono("123")
        viewModel.guardarYSeleccionarNuevoCliente()
        assertNotNull(viewModel.estadoUi.value.errorNuevoCliente)
        assertNull(viewModel.estadoUi.value.clienteSeleccionado)

        // Ingreso datos válidos
        viewModel.actualizarNuevoClienteNombre("Carlos Mendoza")
        viewModel.actualizarNuevoClienteTelefono("4459876543")
        viewModel.actualizarNuevoClienteDireccion("Calle Hidalgo 45")
        viewModel.guardarYSeleccionarNuevoCliente()

        // Diálogo cerrado, sin error y cliente seleccionado
        assertFalse(viewModel.estadoUi.value.mostrarDialogoNuevoCliente)
        assertNull(viewModel.estadoUi.value.errorNuevoCliente)
        assertNotNull(viewModel.estadoUi.value.clienteSeleccionado)
        assertEquals("Carlos Mendoza", viewModel.estadoUi.value.clienteSeleccionado?.name)
        assertEquals("4459876543", viewModel.estadoUi.value.clienteSeleccionado?.phone)

        // El cliente se persistió en la BD
        val clienteEnDb = fakeDao.clients.find { it.name == "Carlos Mendoza" }
        assertNotNull(clienteEnDb)
        assertEquals("4459876543", clienteEnDb?.phone)
    }

    @Test
    fun testRegistroNuevaMotoParaCliente() = runBlocking {
        val cliente = ClientEntity(id = "c_nuevo", name = "Laura Díaz", phone = "4451122334")
        fakeDao.insertClient(cliente)

        val viewModel = RecepcionViewModel(repository, Dispatchers.Unconfined)
        viewModel.seleccionarCliente(cliente)

        // Abrimos el modal para registrar moto
        viewModel.abrirDialogoNuevaMoto()
        assertTrue(viewModel.estadoUi.value.mostrarDialogoNuevaMoto)

        // Intentar guardar vacía
        viewModel.guardarYSeleccionarNuevaMoto()
        assertNotNull(viewModel.estadoUi.value.errorNuevaMoto)

        // Llenar campos de la moto
        viewModel.actualizarNuevaMotoMarca("Bajaj")
        viewModel.actualizarNuevaMotoModelo("Pulsar NS200")
        viewModel.actualizarNuevaMotoColor("Negro Mate")
        viewModel.actualizarNuevaMotoPlaca("PUL-45")
        viewModel.actualizarNuevaMotoAnio("2023")
        viewModel.guardarYSeleccionarNuevaMoto()

        assertFalse(viewModel.estadoUi.value.mostrarDialogoNuevaMoto)
        assertNull(viewModel.estadoUi.value.errorNuevaMoto)
        assertNotNull(viewModel.estadoUi.value.motoSeleccionada)
        assertEquals("Bajaj", viewModel.estadoUi.value.motoSeleccionada?.brand)
        assertEquals("Pulsar NS200", viewModel.estadoUi.value.motoSeleccionada?.model)
        assertEquals(cliente.id, viewModel.estadoUi.value.motoSeleccionada?.clientId)

        // La moto se persistió en la BD vinculada al cliente
        val motoEnDb = fakeDao.motorcycles.find { it.model == "Pulsar NS200" }
        assertNotNull(motoEnDb)
        assertEquals(cliente.id, motoEnDb?.clientId)
    }
}
