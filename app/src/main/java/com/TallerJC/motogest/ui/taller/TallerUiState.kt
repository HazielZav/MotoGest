package com.TallerJC.motogest.ui.taller

import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.OrderStatus

data class TallerUiState(
    val filtroEstadoSeleccionado: OrderStatus = OrderStatus.RECIBIDA,
    val ordenesFiltradas: List<WorkOrderWithDetails> = emptyList(),
    val ordenSeleccionadaDetalle: WorkOrderWithDetails? = null
)