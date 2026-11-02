package com.TallerJC.motogest.ui.entregas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.repository.OperacionesRepository
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EntregasViewModel(
    private val repository: OperacionesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(EntregasUiState(isLoading = true))
    val estadoUi: StateFlow<EntregasUiState> = _estadoUi.asStateFlow()

    private var todasLasOrdenes: List<WorkOrderWithDetails> = emptyList()

    val estadosEntrega = listOf(
        OrderStatus.LISTA_ENTREGA,
        OrderStatus.EN_CAMINO
    )

    init {
        observarOrdenes()
    }

    private fun observarOrdenes() {
        viewModelScope.launch(dispatcher) {
            _estadoUi.update { it.copy(isLoading = true, mensajeError = null) }
            repository.obtenerTodasLasOrdenes()
                .catch { ex ->
                    _estadoUi.update {
                        it.copy(
                            isLoading = false,
                            mensajeError = "Error al consultar las órdenes de entrega: ${ex.message}"
                        )
                    }
                }
                .collect { ordenes ->
                    todasLasOrdenes = ordenes
                    aplicarFiltroYActualizar(todasLasOrdenes)
                }
        }
    }

    private fun aplicarFiltroYActualizar(ordenes: List<WorkOrderWithDetails>) {
        val filtroActual = _estadoUi.value.filtroEstado

        // Filtro de negocio: deliveryType == DOMICILIO y status in (LISTA_ENTREGA, EN_CAMINO)
        val ordenesDomicilio = ordenes.filter { detalle ->
            detalle.order.deliveryType == DeliveryType.DOMICILIO.name &&
                    (detalle.order.status == OrderStatus.LISTA_ENTREGA.name ||
                            detalle.order.status == OrderStatus.EN_CAMINO.name)
        }

        val ordenesFiltradas = if (filtroActual != null) {
            ordenesDomicilio.filter { it.order.status == filtroActual.name }
        } else {
            ordenesDomicilio
        }

        // Mantener sincronizada la orden actualmente seleccionada si sigue existiendo
        val seleccionadaActual = _estadoUi.value.ordenSeleccionada?.let { actual ->
            ordenesDomicilio.find { it.order.id == actual.order.id }
        }

        _estadoUi.update {
            it.copy(
                ordenesParaEntrega = ordenesFiltradas,
                ordenSeleccionada = seleccionadaActual,
                isLoading = false
            )
        }
    }

    fun cambiarFiltroEstado(nuevoFiltro: OrderStatus?) {
        _estadoUi.update { it.copy(filtroEstado = nuevoFiltro) }
        aplicarFiltroYActualizar(todasLasOrdenes)
    }

    // --- TRANSICIÓN DE ESTADOS (RF-09 / US-06) ---

    fun marcarEnCamino(ordenDetalle: WorkOrderWithDetails) {
        viewModelScope.launch(dispatcher) {
            try {
                repository.cambiarEstadoOrden(ordenDetalle.order, OrderStatus.EN_CAMINO)
                _estadoUi.update {
                    it.copy(
                        mensajeExito = "Orden ${ordenDetalle.order.folio} marcada EN CAMINO."
                    )
                }
            } catch (ex: Exception) {
                _estadoUi.update {
                    it.copy(mensajeError = "No se pudo actualizar a EN CAMINO: ${ex.message}")
                }
            }
        }
    }

    fun solicitarConfirmacionEntrega(ordenDetalle: WorkOrderWithDetails) {
        // Regla preventiva: Si el costo de envío es $0.00, alertar antes de confirmar
        if (ordenDetalle.order.deliveryCost <= 0.0) {
            _estadoUi.update {
                it.copy(
                    ordenSeleccionada = ordenDetalle,
                    mostrarAlertaCostoCero = true,
                    accionPendienteCostoCero = AccionCostoCero.MARCAR_ENTREGADA
                )
            }
        } else {
            _estadoUi.update {
                it.copy(
                    ordenSeleccionada = ordenDetalle,
                    mostrarDialogoConfirmarEntrega = true
                )
            }
        }
    }

    fun confirmarEntregaDirecta() {
        val orden = _estadoUi.value.ordenSeleccionada ?: return
        completarEntrega(orden, orden.order.deliveryCost)
    }

    private fun completarEntrega(ordenDetalle: WorkOrderWithDetails, costoEnvioFinal: Double) {
        viewModelScope.launch(dispatcher) {
            try {
                repository.marcarOrdenEntregada(
                    ordenActual = ordenDetalle.order,
                    clienteActual = ordenDetalle.client,
                    costoEnvioFinal = costoEnvioFinal
                )
                _estadoUi.update {
                    it.copy(
                        ordenSeleccionada = null,
                        mostrarDialogoConfirmarEntrega = false,
                        mostrarAlertaCostoCero = false,
                        accionPendienteCostoCero = null,
                        mensajeExito = "Orden ${ordenDetalle.order.folio} ENTREGADA. Costo de envío ($${String.format("%.2f", costoEnvioFinal)}) guardado en ficha del cliente."
                    )
                }
            } catch (ex: Exception) {
                _estadoUi.update {
                    it.copy(mensajeError = "Error al marcar como entregada: ${ex.message}")
                }
            }
        }
    }

    // --- GESTIÓN DEL COSTO DE ENVÍO ---

    fun abrirDialogoEdicionCosto(ordenDetalle: WorkOrderWithDetails) {
        val costoActual = if (ordenDetalle.order.deliveryCost > 0.0) {
            String.format("%.2f", ordenDetalle.order.deliveryCost)
        } else {
            ""
        }
        _estadoUi.update {
            it.copy(
                ordenSeleccionada = ordenDetalle,
                costoEnvioEdicion = costoActual,
                mostrarDialogoEdicionCosto = true,
                mensajeError = null
            )
        }
    }

    fun actualizarCostoEnvioTexto(nuevoTexto: String) {
        // Permitir dígitos y un punto decimal
        val filtrado = nuevoTexto.filter { it.isDigit() || it == '.' }
        if (filtrado.count { it == '.' } <= 1) {
            _estadoUi.update { it.copy(costoEnvioEdicion = filtrado) }
        }
    }

    fun guardarCostoEnvio() {
        val ordenDetalle = _estadoUi.value.ordenSeleccionada ?: return
        val monto = _estadoUi.value.costoEnvioEdicion.toDoubleOrNull() ?: 0.0

        if (monto <= 0.0) {
            // Regla preventiva: AlertDialog de advertencia de costo $0.00
            _estadoUi.update {
                it.copy(
                    mostrarAlertaCostoCero = true,
                    accionPendienteCostoCero = AccionCostoCero.GUARDAR_COSTO
                )
            }
        } else {
            ejecutarGuardadoCosto(ordenDetalle, monto)
        }
    }

    fun confirmarCostoCero() {
        val ordenDetalle = _estadoUi.value.ordenSeleccionada ?: return
        val accion = _estadoUi.value.accionPendienteCostoCero

        when (accion) {
            AccionCostoCero.GUARDAR_COSTO -> {
                ejecutarGuardadoCosto(ordenDetalle, 0.0)
            }
            AccionCostoCero.MARCAR_ENTREGADA -> {
                completarEntrega(ordenDetalle, 0.0)
            }
            null -> {
                _estadoUi.update { it.copy(mostrarAlertaCostoCero = false) }
            }
        }
    }

    private fun ejecutarGuardadoCosto(ordenDetalle: WorkOrderWithDetails, costo: Double) {
        viewModelScope.launch(dispatcher) {
            try {
                repository.actualizarCostoEnvio(ordenDetalle.order, costo)
                _estadoUi.update {
                    it.copy(
                        mostrarDialogoEdicionCosto = false,
                        mostrarAlertaCostoCero = false,
                        accionPendienteCostoCero = null,
                        ordenSeleccionada = null,
                        mensajeExito = "Costo de envío actualizado a $${String.format("%.2f", costo)}."
                    )
                }
            } catch (ex: Exception) {
                _estadoUi.update {
                    it.copy(mensajeError = "No se pudo actualizar el costo de envío: ${ex.message}")
                }
            }
        }
    }

    fun descartarAlertaCostoCero() {
        _estadoUi.update {
            it.copy(
                mostrarAlertaCostoCero = false,
                accionPendienteCostoCero = null
            )
        }
    }

    fun cerrarDialogoEdicionCosto() {
        _estadoUi.update {
            it.copy(
                mostrarDialogoEdicionCosto = false,
                ordenSeleccionada = null,
                costoEnvioEdicion = ""
            )
        }
    }

    fun cerrarDialogoConfirmarEntrega() {
        _estadoUi.update {
            it.copy(
                mostrarDialogoConfirmarEntrega = false,
                ordenSeleccionada = null
            )
        }
    }

    fun limpiarMensajes() {
        _estadoUi.update {
            it.copy(
                mensajeError = null,
                mensajeExito = null
            )
        }
    }

    companion object {
        fun provideFactory(repository: OperacionesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EntregasViewModel(repository) as T
                }
            }
    }
}
