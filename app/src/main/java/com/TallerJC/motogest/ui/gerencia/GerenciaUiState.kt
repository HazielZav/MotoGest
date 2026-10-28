package com.TallerJC.motogest.ui.gerencia

import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.ServiceType

enum class PeriodoFiltro(val titulo: String) {
    HOY("Hoy"),
    ESTA_SEMANA("Esta Semana"),
    ESTE_MES("Este Mes"),
    PERSONALIZADO("Personalizado")
}

data class DistribucionServicio(
    val tipo: ServiceType,
    val nombre: String,
    val cantidad: Int,
    val porcentaje: Float // Valor entre 0.0f y 1.0f para LinearProgressIndicator
)

data class GerenciaUiState(
    val periodoSeleccionado: PeriodoFiltro = PeriodoFiltro.ESTE_MES,
    val fechaInicioPersonalizada: Long? = null,
    val fechaFinPersonalizada: Long? = null,
    val excluirRefacciones: Boolean = false,

    // Métricas Financieras (KPIs)
    val ingresosTotales: Double = 0.0,
    val totalRefaccionesCosto: Double = 0.0,
    val totalManoObraCosto: Double = 0.0,
    val motosAtendidas: Int = 0,
    val ticketPromedio: Double = 0.0,

    // Métricas Operativas
    val distribucionServicios: List<DistribucionServicio> = emptyList(),

    // Consulta Histórica
    val textoBusqueda: String = "",
    val ordenesHistorial: List<WorkOrderWithDetails> = emptyList(),
    val ordenesFiltradas: List<WorkOrderWithDetails> = emptyList(),

    // Banderas de UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val mostrarDialogoFecha: Boolean = false
)
