package com.TallerJC.motogest.ui.gerencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.repository.GerenciaRepository
import com.TallerJC.motogest.data.repository.GerenciaRepositoryImpl
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class GerenciaViewModel(
    private val repository: GerenciaRepository = GerenciaRepositoryImpl(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(GerenciaUiState(isLoading = true))
    val estadoUi: StateFlow<GerenciaUiState> = _estadoUi.asStateFlow()

    // Cache interno de todas las órdenes entregadas obtenidas del repositorio
    private var todasLasOrdenesEntregadas: List<WorkOrderWithDetails> = emptyList()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch(dispatcher) {
            _estadoUi.update { it.copy(isLoading = true, error = null) }
            repository.obtenerOrdenesEntregadas()
                .catch { ex ->
                    _estadoUi.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al consultar las órdenes: ${ex.message}"
                        )
                    }
                }
                .collect { ordenes ->
                    todasLasOrdenesEntregadas = ordenes
                    recalcularMetricasYFiltros()
                }
        }
    }

    fun cambiarPeriodo(nuevoPeriodo: PeriodoFiltro) {
        if (nuevoPeriodo == PeriodoFiltro.PERSONALIZADO) {
            _estadoUi.update {
                it.copy(
                    periodoSeleccionado = nuevoPeriodo,
                    mostrarDialogoFecha = true
                )
            }
        } else {
            _estadoUi.update { it.copy(periodoSeleccionado = nuevoPeriodo) }
            recalcularMetricasYFiltros()
        }
    }

    fun establecerRangoPersonalizado(inicioMs: Long, finMs: Long) {
        _estadoUi.update {
            it.copy(
                periodoSeleccionado = PeriodoFiltro.PERSONALIZADO,
                fechaInicioPersonalizada = inicioMs,
                fechaFinPersonalizada = finMs,
                mostrarDialogoFecha = false
            )
        }
        recalcularMetricasYFiltros()
    }

    fun cerrarDialogoFecha() {
        _estadoUi.update { it.copy(mostrarDialogoFecha = false) }
    }

    fun toggleExcluirRefacciones() {
        _estadoUi.update { it.copy(excluirRefacciones = !it.excluirRefacciones) }
        recalcularMetricasYFiltros()
    }

    fun actualizarBusqueda(texto: String) {
        _estadoUi.update { it.copy(textoBusqueda = texto) }
        aplicarFiltroBusqueda()
    }

    private fun recalcularMetricasYFiltros() {
        val estadoActual = _estadoUi.value
        val (inicioMs, finMs) = obtenerRangoMilisegundos(
            periodo = estadoActual.periodoSeleccionado,
            inicioPersonalizado = estadoActual.fechaInicioPersonalizada,
            finPersonalizado = estadoActual.fechaFinPersonalizada
        )

        // Filtrar órdenes entregadas en el periodo
        val ordenesPeriodo = todasLasOrdenesEntregadas.filter { item ->
            val fechaOrden = item.order.deliveredAt ?: item.order.completedAt ?: item.order.createdAt
            val enRango = fechaOrden in inicioMs..finMs
            val esEntregada = item.order.status == OrderStatus.ENTREGADA.name
            enRango && esEntregada
        }

        // Cálculos Financieros
        var sumaIngresos = 0.0
        var sumaRefacciones = 0.0
        var sumaManoObra = 0.0

        for (item in ordenesPeriodo) {
            val totalParts = item.order.totalPartsCost
            val totalLabor = item.order.totalLaborCost
            val totalOrder = item.order.totalAmount

            sumaRefacciones += totalParts
            sumaManoObra += totalLabor

            if (estadoActual.excluirRefacciones) {
                val gananciaReal = maxOf(0.0, totalOrder - totalParts)
                sumaIngresos += gananciaReal
            } else {
                // Ingreso bruto
                sumaIngresos += totalOrder
            }
        }

        val totalMotos = ordenesPeriodo.size
        val ticketPromedio = if (totalMotos > 0) sumaIngresos / totalMotos else 0.0

        // Distribución Operativa de Servicios
        val distribucion = ServiceType.values().map { tipo ->
            val cantidad = ordenesPeriodo.count { it.order.serviceType == tipo.name }
            val porcentaje = if (totalMotos > 0) cantidad.toFloat() / totalMotos.toFloat() else 0f
            val nombreLegible = when (tipo) {
                ServiceType.REPARACION -> "Reparación General"
                ServiceType.SERVICIO_GENERAL -> "Servicio General / Afinación"
                ServiceType.AJUSTE_MOTOR -> "Ajuste de Motor"
                ServiceType.FRENOS -> "Sistema de Frenos"
                ServiceType.OTRO -> "Otros Servicios"
            }
            DistribucionServicio(
                tipo = tipo,
                nombre = nombreLegible,
                cantidad = cantidad,
                porcentaje = porcentaje
            )
        }

        _estadoUi.update {
            it.copy(
                isLoading = false,
                ingresosTotales = sumaIngresos,
                totalRefaccionesCosto = sumaRefacciones,
                totalManoObraCosto = sumaManoObra,
                motosAtendidas = totalMotos,
                ticketPromedio = ticketPromedio,
                distribucionServicios = distribucion,
                ordenesHistorial = ordenesPeriodo
            )
        }

        aplicarFiltroBusqueda()
    }

    private fun aplicarFiltroBusqueda() {
        val query = _estadoUi.value.textoBusqueda.trim()
        val historial = _estadoUi.value.ordenesHistorial

        val filtradas = if (query.isEmpty()) {
            historial
        } else {
            historial.filter { item ->
                item.client.name.contains(query, ignoreCase = true) ||
                        item.motorcycle.model.contains(query, ignoreCase = true) ||
                        item.motorcycle.brand.contains(query, ignoreCase = true) ||
                        item.motorcycle.plate?.contains(query, ignoreCase = true) == true ||
                        item.order.folio.contains(query, ignoreCase = true)
            }
        }

        _estadoUi.update { it.copy(ordenesFiltradas = filtradas) }
    }

    private fun obtenerRangoMilisegundos(
        periodo: PeriodoFiltro,
        inicioPersonalizado: Long?,
        finPersonalizado: Long?
    ): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val hoy = LocalDate.now(zone)

        return when (periodo) {
            PeriodoFiltro.HOY -> {
                val inicio = hoy.atStartOfDay(zone).toInstant().toEpochMilli()
                val fin = hoy.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
                inicio to fin
            }
            PeriodoFiltro.ESTA_SEMANA -> {
                val haceSieteDias = hoy.minusDays(6)
                val inicio = haceSieteDias.atStartOfDay(zone).toInstant().toEpochMilli()
                val fin = hoy.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
                inicio to fin
            }
            PeriodoFiltro.ESTE_MES -> {
                val primerDia = hoy.withDayOfMonth(1)
                val ultimoDia = hoy.withDayOfMonth(hoy.lengthOfMonth())
                val inicio = primerDia.atStartOfDay(zone).toInstant().toEpochMilli()
                val fin = ultimoDia.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
                inicio to fin
            }
            PeriodoFiltro.PERSONALIZADO -> {
                val inicio = inicioPersonalizado
                    ?: hoy.minusDays(30).atStartOfDay(zone).toInstant().toEpochMilli()
                val fin = finPersonalizado
                    ?: hoy.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
                inicio to fin
            }
        }
    }
}
