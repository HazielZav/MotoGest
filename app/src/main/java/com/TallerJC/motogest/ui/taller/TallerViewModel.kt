package com.TallerJC.motogest.ui.taller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.repository.OperacionesRepository
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TallerViewModel(
    private val repository: OperacionesRepository
) : ViewModel() {

    private val _filtroEstado = MutableStateFlow(OrderStatus.RECIBIDA)
    private val _idOrdenSeleccionada = MutableStateFlow<String?>(null)

    // Estados operativos que le interesan al mecánico (US-04)
    val estadosTaller = listOf(
        OrderStatus.RECIBIDA,
        OrderStatus.EN_PROCESO,
        OrderStatus.ESPERANDO_REFACCIONES,
        OrderStatus.EN_PRUEBAS,
        OrderStatus.LISTA_ENTREGA
    )

    // Combinamos el flujo reactivo de Room con el filtro actual en tiempo real
    val estadoUi: StateFlow<TallerUiState> = combine(
        repository.obtenerTodasLasOrdenes(),
        _filtroEstado,
        _idOrdenSeleccionada
    ) { todasLasOrdenes, filtroActual, idSeleccionada ->
        val filtradas = todasLasOrdenes.filter { it.order.status == filtroActual.name }
        val seleccionada = todasLasOrdenes.find { it.order.id == idSeleccionada }
        TallerUiState(
            filtroEstadoSeleccionado = filtroActual,
            ordenesFiltradas = filtradas,
            ordenSeleccionadaDetalle = seleccionada
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TallerUiState()
    )

    fun cambiarFiltroEstado(nuevoFiltro: OrderStatus) {
        _filtroEstado.value = nuevoFiltro
    }

    fun seleccionarOrdenParaDetalle(ordenId: String?) {
        _idOrdenSeleccionada.value = ordenId
    }

    fun cambiarEstadoOrden(ordenDetalle: WorkOrderWithDetails, nuevoEstado: OrderStatus) {
        viewModelScope.launch {
            repository.cambiarEstadoOrden(ordenDetalle.order, nuevoEstado)
            // Cambiamos a la pestaña del nuevo estado o cerramos modal si ya está lista
            if (nuevoEstado == OrderStatus.LISTA_ENTREGA) {
                _idOrdenSeleccionada.value = null
            }
        }
    }

    fun agregarTrabajoMecanico(ordenDetalle: WorkOrderWithDetails, concepto: String, costoTexto: String) {
        val costo = costoTexto.toDoubleOrNull() ?: return
        if (concepto.isBlank() || costo <= 0.0) return
        viewModelScope.launch {
            repository.agregarManoDeObra(ordenDetalle.order, concepto.trim(), costo)
        }
    }

    fun agregarRefaccion(ordenDetalle: WorkOrderWithDetails, pieza: String, costoTexto: String) {
        val costo = costoTexto.toDoubleOrNull() ?: return
        if (pieza.isBlank() || costo <= 0.0) return
        viewModelScope.launch {
            repository.agregarRefaccion(ordenDetalle.order, pieza.trim(), costo)
        }
    }

    fun guardarRecomendacionesYVin(ordenDetalle: WorkOrderWithDetails, recomendaciones: String, vin: String) {
        viewModelScope.launch {
            repository.actualizarNotasYVinTaller(ordenDetalle.order, recomendaciones.trim(), vin.trim())
            _idOrdenSeleccionada.value = null
        }
    }

    companion object {
        fun provideFactory(repository: OperacionesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TallerViewModel(repository) as T
                }
            }
    }
}