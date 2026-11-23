package com.TallerJC.motogest.ui.gerencia

import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.UserEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.repository.GerenciaRepository
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.domain.model.enums.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GerenciaViewModelTest {

    private fun crearMockRepository(
        ordenes: List<WorkOrderWithDetails>,
        empleadosIniciales: List<UserEntity> = emptyList()
    ): GerenciaRepository {
        val empleadosFlow = MutableStateFlow(empleadosIniciales)
        return object : GerenciaRepository {
            override fun obtenerOrdenesEntregadas(): Flow<List<WorkOrderWithDetails>> = flowOf(ordenes)
            override fun obtenerTodasLasOrdenes(): Flow<List<WorkOrderWithDetails>> = flowOf(ordenes)
            override fun obtenerHistorialMoto(motorcycleId: String): Flow<List<WorkOrderWithDetails>> = flowOf(ordenes.filter { it.motorcycle.id == motorcycleId })
            override fun obtenerHistorialCliente(clientId: String): Flow<List<WorkOrderWithDetails>> = flowOf(ordenes.filter { it.client.id == clientId })
            override fun obtenerEmpleados(): Flow<List<UserEntity>> = empleadosFlow
            override suspend fun guardarEmpleado(empleado: UserEntity) {
                val actual = empleadosFlow.value.toMutableList()
                val idx = actual.indexOfFirst { it.id == empleado.id }
                if (idx != -1) actual[idx] = empleado else actual.add(empleado)
                empleadosFlow.value = actual
            }
            override suspend fun eliminarEmpleado(userId: String) {
                empleadosFlow.value = empleadosFlow.value.filterNot { it.id == userId }
            }
            override suspend fun sincronizarEmpleados(): Boolean = true
            override suspend fun insertarDatosDemo() {}
        }
    }

    @Test
    fun testCalculoBrutoVsNetoExcluyendoRefacciones() = runBlocking {
        val now = System.currentTimeMillis()
        val cliente = ClientEntity(id = "c1", name = "Test Cliente", phone = "1234567890")
        val moto = MotorcycleEntity(id = "m1", clientId = "c1", brand = "Honda", model = "CGL125", color = "Rojo", photoUrl = "")

        val orden1 = WorkOrderEntity(
            id = "o1",
            folio = "ORD-001",
            clientId = "c1",
            motorcycleId = "m1",
            receivedByUserId = "u1",
            status = OrderStatus.ENTREGADA.name,
            serviceType = ServiceType.SERVICIO_GENERAL.name,
            reportedFailure = "Servicio",
            deliveryType = "TALLER",
            totalLaborCost = 300.0,
            totalPartsCost = 500.0,
            totalAmount = 800.0,
            deliveredAt = now
        )

        val mockRepo = crearMockRepository(
            listOf(
                WorkOrderWithDetails(
                    order = orden1,
                    client = cliente,
                    motorcycle = moto,
                    workItems = emptyList(),
                    spareParts = emptyList()
                )
            )
        )

        val viewModel = GerenciaViewModel(mockRepo, Dispatchers.Unconfined)

        // Estado inicial: excluirRefacciones = false (Ingreso Bruto)
        var state = viewModel.estadoUi.value
        assertFalse(state.excluirRefacciones)
        assertEquals(800.0, state.ingresosTotales, 0.001)
        assertEquals(500.0, state.totalRefaccionesCosto, 0.001)
        assertEquals(1, state.motosAtendidas)
        assertEquals(800.0, state.ticketPromedio, 0.001)

        // Activamos toggle de excluir refacciones (Ganancia Real / Neta)
        viewModel.toggleExcluirRefacciones()

        state = viewModel.estadoUi.value
        assertTrue(state.excluirRefacciones)
        // Ganancia neta
        assertEquals(300.0, state.ingresosTotales, 0.001)
        assertEquals(300.0, state.ticketPromedio, 0.001)
    }

    @Test
    fun testBusquedaPorClienteMotoYPlacas() = runBlocking {
        val now = System.currentTimeMillis()

        val c1 = ClientEntity(id = "c1", name = "Juan Pérez", phone = "111")
        val m1 = MotorcycleEntity(id = "m1", clientId = "c1", brand = "Italika", model = "FT150", color = "Negro", photoUrl = "", plate = "ABC-12")
        val o1 = WorkOrderEntity(
            id = "o1", folio = "ORD-001", clientId = "c1", motorcycleId = "m1",
            receivedByUserId = "u1", status = OrderStatus.ENTREGADA.name, serviceType = ServiceType.REPARACION.name,
            reportedFailure = "", deliveryType = "TALLER", totalAmount = 500.0, deliveredAt = now
        )

        val c2 = ClientEntity(id = "c2", name = "Laura Sánchez", phone = "222")
        val m2 = MotorcycleEntity(id = "m2", clientId = "c2", brand = "Yamaha", model = "R3", color = "Azul", photoUrl = "", plate = "XYZ-99")
        val o2 = WorkOrderEntity(
            id = "o2", folio = "ORD-002", clientId = "c2", motorcycleId = "m2",
            receivedByUserId = "u1", status = OrderStatus.ENTREGADA.name, serviceType = ServiceType.FRENOS.name,
            reportedFailure = "", deliveryType = "TALLER", totalAmount = 600.0, deliveredAt = now
        )

        val mockRepo = crearMockRepository(
            listOf(
                WorkOrderWithDetails(o1, c1, m1, emptyList(), emptyList()),
                WorkOrderWithDetails(o2, c2, m2, emptyList(), emptyList())
            )
        )

        val viewModel = GerenciaViewModel(mockRepo, Dispatchers.Unconfined)

        // Sin búsqueda
        assertEquals(2, viewModel.estadoUi.value.ordenesFiltradas.size)

        // Buscar por nombre de cliente
        viewModel.actualizarBusqueda("Laura")
        assertEquals(1, viewModel.estadoUi.value.ordenesFiltradas.size)
        assertEquals("Laura Sánchez", viewModel.estadoUi.value.ordenesFiltradas.first().client.name)

        // Buscar por marca de moto
        viewModel.actualizarBusqueda("Italika")
        assertEquals(1, viewModel.estadoUi.value.ordenesFiltradas.size)
        assertEquals("Italika", viewModel.estadoUi.value.ordenesFiltradas.first().motorcycle.brand)

        // Buscar por placa
        viewModel.actualizarBusqueda("XYZ")
        assertEquals(1, viewModel.estadoUi.value.ordenesFiltradas.size)
        assertEquals("XYZ-99", viewModel.estadoUi.value.ordenesFiltradas.first().motorcycle.plate)

        // Buscar texto inexistente
        viewModel.actualizarBusqueda("Ducati")
        assertTrue(viewModel.estadoUi.value.ordenesFiltradas.isEmpty())
    }

    @Test
    fun testDistribucionDeServicios() = runBlocking {
        val now = System.currentTimeMillis()
        val c = ClientEntity(id = "c1", name = "Cliente Test", phone = "")
        val m = MotorcycleEntity(id = "m1", clientId = "c1", brand = "Hero", model = "Ignitor", color = "", photoUrl = "")

        val ordenes = listOf(
            WorkOrderWithDetails(
                order = WorkOrderEntity(id = "o1", folio = "1", clientId = "c1", motorcycleId = "m1", receivedByUserId = "u", status = OrderStatus.ENTREGADA.name, serviceType = ServiceType.SERVICIO_GENERAL.name, reportedFailure = "", deliveryType = "TALLER", totalAmount = 500.0, deliveredAt = now),
                client = c, motorcycle = m, workItems = emptyList(), spareParts = emptyList()
            ),
            WorkOrderWithDetails(
                order = WorkOrderEntity(id = "o2", folio = "2", clientId = "c1", motorcycleId = "m1", receivedByUserId = "u", status = OrderStatus.ENTREGADA.name, serviceType = ServiceType.SERVICIO_GENERAL.name, reportedFailure = "", deliveryType = "TALLER", totalAmount = 500.0, deliveredAt = now),
                client = c, motorcycle = m, workItems = emptyList(), spareParts = emptyList()
            ),
            WorkOrderWithDetails(
                order = WorkOrderEntity(id = "o3", folio = "3", clientId = "c1", motorcycleId = "m1", receivedByUserId = "u", status = OrderStatus.ENTREGADA.name, serviceType = ServiceType.FRENOS.name, reportedFailure = "", deliveryType = "TALLER", totalAmount = 300.0, deliveredAt = now),
                client = c, motorcycle = m, workItems = emptyList(), spareParts = emptyList()
            )
        )

        val viewModel = GerenciaViewModel(crearMockRepository(ordenes), Dispatchers.Unconfined)

        val state = viewModel.estadoUi.value
        assertEquals(3, state.motosAtendidas)

        val distServicioGeneral = state.distribucionServicios.first { it.tipo == ServiceType.SERVICIO_GENERAL }
        assertEquals(2, distServicioGeneral.cantidad)
        assertEquals(2f / 3f, distServicioGeneral.porcentaje, 0.01f)

        val distFrenos = state.distribucionServicios.first { it.tipo == ServiceType.FRENOS }
        assertEquals(1, distFrenos.cantidad)
        assertEquals(1f / 3f, distFrenos.porcentaje, 0.01f)
    }

    @Test
    fun testEstadoVacioCuandoNoHayOrdenes() = runBlocking {
        val viewModel = GerenciaViewModel(crearMockRepository(emptyList()), Dispatchers.Unconfined)

        val state = viewModel.estadoUi.value
        assertEquals(0, state.motosAtendidas)
        assertEquals(0.0, state.ingresosTotales, 0.001)
        assertEquals(0.0, state.ticketPromedio, 0.001)
        assertTrue(state.ordenesFiltradas.isEmpty())
    }

    @Test
    fun testDirectorioEHistorialDeMotos() = runBlocking {
        val now = System.currentTimeMillis()
        val c = ClientEntity(id = "c1", name = "Guillermo Mendoza", phone = "4457774411")
        val m = MotorcycleEntity(id = "m1", clientId = "c1", brand = "Vento", model = "Nitrox 250", color = "Naranja", photoUrl = "", plate = "VNT-55")

        val orden = WorkOrderWithDetails(
            order = WorkOrderEntity(id = "o1", folio = "ORD-005", clientId = "c1", motorcycleId = "m1", receivedByUserId = "u", status = OrderStatus.ENTREGADA.name, serviceType = ServiceType.OTRO.name, reportedFailure = "Exploradoras", deliveryType = "TALLER", totalAmount = 1000.0, deliveredAt = now),
            client = c,
            motorcycle = m,
            workItems = emptyList(),
            spareParts = emptyList()
        )

        val viewModel = GerenciaViewModel(crearMockRepository(listOf(orden)), Dispatchers.Unconfined)

        assertEquals(1, viewModel.estadoUi.value.todasLasOrdenes.size)
        assertEquals(1, viewModel.estadoUi.value.ordenesDirectorioFiltradas.size)

        // Búsqueda en directorio por placa
        viewModel.actualizarBusquedaDirectorio("VNT")
        assertEquals(1, viewModel.estadoUi.value.ordenesDirectorioFiltradas.size)
        assertEquals("VNT-55", viewModel.estadoUi.value.ordenesDirectorioFiltradas.first().motorcycle.plate)

        // Detalle de orden modal
        viewModel.abrirDetalleOrden(orden)
        assertTrue(viewModel.estadoUi.value.mostrarModalDetalleOrden)
        assertEquals(orden, viewModel.estadoUi.value.ordenSeleccionadaDetalle)

        viewModel.cerrarDetalleOrden()
        assertFalse(viewModel.estadoUi.value.mostrarModalDetalleOrden)
    }

    @Test
    fun testAgregarYValidarEmpleado() = runBlocking {
        val viewModel = GerenciaViewModel(crearMockRepository(emptyList()), Dispatchers.Unconfined)

        viewModel.abrirDialogoNuevoEmpleado()
        assertTrue(viewModel.estadoUi.value.mostrarDialogoEmpleado)

        // Validación nombre vacío
        viewModel.guardarEmpleadoFormulario()
        assertNotNull(viewModel.estadoUi.value.errorFormularioEmpleado)
        assertEquals(0, viewModel.estadoUi.value.empleados.size)

        // Validación correo inválido
        viewModel.actualizarFormEmpleadoNombre("Carlos Ramírez")
        viewModel.actualizarFormEmpleadoCorreo("correo-invalido")
        viewModel.guardarEmpleadoFormulario()
        assertNotNull(viewModel.estadoUi.value.errorFormularioEmpleado)

        // Selección única de rol (reemplaza el rol anterior al elegir otro)
        viewModel.actualizarFormEmpleadoCorreo("carlos@motogest.com")
        viewModel.toggleFormEmpleadoRol(UserRole.REPARTIDOR)
        assertEquals(setOf(UserRole.REPARTIDOR), viewModel.estadoUi.value.formEmpleadoRoles)
        viewModel.guardarEmpleadoFormulario()

        val state = viewModel.estadoUi.value
        assertFalse(state.mostrarDialogoEmpleado)
        assertNull(state.errorFormularioEmpleado)
        assertEquals(1, state.empleados.size)
        assertEquals("Carlos Ramírez", state.empleados.first().fullName)
        assertEquals("carlos@motogest.com", state.empleados.first().email)
        assertEquals(listOf(UserRole.REPARTIDOR.name), state.empleados.first().roles)
        assertTrue(state.empleados.first().isActive)
    }

    @Test
    fun testEvitarCorreoDuplicadoAlRegistrarEmpleado() = runBlocking {
        val existente = UserEntity(
            id = "u1",
            fullName = "Ana López",
            email = "ana@motogest.com",
            roles = listOf(UserRole.RECEPCIONISTA.name)
        )
        val viewModel = GerenciaViewModel(crearMockRepository(emptyList(), listOf(existente)), Dispatchers.Unconfined)

        viewModel.abrirDialogoNuevoEmpleado()
        viewModel.actualizarFormEmpleadoNombre("Ana Dos")
        viewModel.actualizarFormEmpleadoCorreo("ANA@motogest.com")
        viewModel.guardarEmpleadoFormulario()

        assertNotNull(viewModel.estadoUi.value.errorFormularioEmpleado)
        assertTrue(viewModel.estadoUi.value.mostrarDialogoEmpleado)
        assertEquals(1, viewModel.estadoUi.value.empleados.size)
    }

    @Test
    fun testModificarEmpleadoYGestionarRoles() = runBlocking {
        val empleado = UserEntity(
            id = "u1",
            fullName = "Luis Torres",
            email = "luis@motogest.com",
            roles = listOf(UserRole.MECANICO.name),
            isActive = true
        )
        val viewModel = GerenciaViewModel(crearMockRepository(emptyList(), listOf(empleado)), Dispatchers.Unconfined)

        // Modificar datos del empleado
        viewModel.abrirDialogoEditarEmpleado(empleado)
        viewModel.actualizarFormEmpleadoNombre("Luis Torres Mendoza")
        viewModel.actualizarFormEmpleadoCorreo("luis.mendoza@motogest.com")
        viewModel.actualizarFormEmpleadoActivo(false)
        viewModel.guardarEmpleadoFormulario()

        val editado = viewModel.estadoUi.value.empleados.first()
        assertEquals("Luis Torres Mendoza", editado.fullName)
        assertEquals("luis.mendoza@motogest.com", editado.email)
        assertFalse(editado.isActive)

        // Gestionar rol rápido: al elegir otro rol reemplaza la selección única
        viewModel.abrirGestionRoles(editado)
        viewModel.toggleRolTemporal(UserRole.RECEPCIONISTA)
        assertEquals(setOf(UserRole.RECEPCIONISTA), viewModel.estadoUi.value.rolesTemporalesSeleccionados)
        viewModel.toggleRolTemporal(UserRole.GERENTE)
        assertEquals(setOf(UserRole.GERENTE), viewModel.estadoUi.value.rolesTemporalesSeleccionados)
        viewModel.guardarRolesEmpleado()

        assertNull(viewModel.estadoUi.value.empleadoGestionRoles)
        val conNuevoRol = viewModel.estadoUi.value.empleados.first()
        assertEquals(listOf(UserRole.GERENTE.name), conNuevoRol.roles)
    }

    @Test
    fun testFiltrarAlternarActivoYEliminarEmpleados() = runBlocking {
        val e1 = UserEntity("u1", "Sofía Recepción", "sofia@motogest.com", listOf(UserRole.RECEPCIONISTA.name), true)
        val e2 = UserEntity("u2", "Pedro Mecánico", "pedro@motogest.com", listOf(UserRole.MECANICO.name), true)
        val viewModel = GerenciaViewModel(crearMockRepository(emptyList(), listOf(e1, e2)), Dispatchers.Unconfined)

        assertEquals(2, viewModel.estadoUi.value.empleadosFiltrados.size)

        // Filtro por rol
        viewModel.cambiarFiltroRolEmpleado(UserRole.MECANICO)
        assertEquals(1, viewModel.estadoUi.value.empleadosFiltrados.size)
        assertEquals("Pedro Mecánico", viewModel.estadoUi.value.empleadosFiltrados.first().fullName)

        // Quitar filtro por rol y buscar por texto
        viewModel.cambiarFiltroRolEmpleado(null)
        viewModel.actualizarBusquedaEmpleados("sofia@")
        assertEquals(1, viewModel.estadoUi.value.empleadosFiltrados.size)
        assertEquals("Sofía Recepción", viewModel.estadoUi.value.empleadosFiltrados.first().fullName)

        // Limpiar búsqueda y alternar activo
        viewModel.actualizarBusquedaEmpleados("")
        viewModel.toggleEstadoActivoEmpleado(e1)
        assertFalse(viewModel.estadoUi.value.empleados.first { it.id == "u1" }.isActive)

        // Eliminar empleado
        viewModel.solicitarEliminarEmpleado(e2)
        assertEquals(e2, viewModel.estadoUi.value.empleadoAEliminar)
        viewModel.confirmarEliminarEmpleado()
        assertNull(viewModel.estadoUi.value.empleadoAEliminar)
        assertEquals(1, viewModel.estadoUi.value.empleados.size)
    }
}
