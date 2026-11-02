package com.TallerJC.motogest.ui.entregas

import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.OrderStatus

/**
 * Representa la acción que disparó la confirmación preventiva de costo $0.00
 */
enum class AccionCostoCero {
    GUARDAR_COSTO,
    MARCAR_ENTREGADA
}

/**
 * Estado inmutable de la pantalla de Entregas (US-06)
 */
data class EntregasUiState(
    val ordenesParaEntrega: List<WorkOrderWithDetails> = emptyList(),
    val filtroEstado: OrderStatus? = null,
    val ordenSeleccionada: WorkOrderWithDetails? = null,
    val costoEnvioEdicion: String = "",
    val mostrarDialogoEdicionCosto: Boolean = false,
    val mostrarAlertaCostoCero: Boolean = false,
    val accionPendienteCostoCero: AccionCostoCero? = null,
    val mostrarDialogoConfirmarEntrega: Boolean = false,
    val isLoading: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null
)
