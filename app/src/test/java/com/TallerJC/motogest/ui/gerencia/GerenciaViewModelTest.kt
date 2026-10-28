package com.TallerJC.motogest.ui.gerencia

import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.repository.GerenciaRepository
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GerenciaViewModelTest {

    private fun crearMockRepository(ordenes: List<WorkOrderWithDetails>): GerenciaRepository {
        return object : GerenciaRepository {
            override fun obtenerOrdenesEntregadas(): Flow<List<WorkOrderWithDetails>> = flowOf(ordenes)
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
}
