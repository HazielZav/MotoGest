package com.TallerJC.motogest.ui.gerencia

import com.TallerJC.motogest.data.local.entity.UserEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.domain.model.enums.UserRole

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
    val porcentaje: Float
)

data class GerenciaUiState(
    val pestanaSeleccionada: Int = 0, // 0 = Métricas y Finanzas, 1 = Historial y Directorio, 2 = Personal y Roles
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

    // Consulta Histórica de Órdenes Finalizadas
    val textoBusqueda: String = "",
    val ordenesHistorial: List<WorkOrderWithDetails> = emptyList(),
    val ordenesFiltradas: List<WorkOrderWithDetails> = emptyList(),

    // Directorio y Registro Completo de Unidades
    val todasLasOrdenes: List<WorkOrderWithDetails> = emptyList(),
    val textoBusquedaDirectorio: String = "",
    val ordenesDirectorioFiltradas: List<WorkOrderWithDetails> = emptyList(),

    // Gestión de Personal y Roles
    val empleados: List<UserEntity> = emptyList(),
    val empleadosFiltrados: List<UserEntity> = emptyList(),
    val textoBusquedaEmpleados: String = "",
    val filtroRolEmpleado: UserRole? = null,
    val mostrarDialogoEmpleado: Boolean = false,
    val empleadoEnEdicion: UserEntity? = null,
    val formEmpleadoNombre: String = "",
    val formEmpleadoCorreo: String = "",
    val formEmpleadoRoles: Set<UserRole> = emptySet(),
    val formEmpleadoActivo: Boolean = true,
    val errorFormularioEmpleado: String? = null,
    val empleadoGestionRoles: UserEntity? = null,
    val rolesTemporalesSeleccionados: Set<UserRole> = emptySet(),
    val errorGestionRoles: String? = null,
    val empleadoAEliminar: UserEntity? = null,
    val mensajePersonal: String? = null,

    // Modales de Detalle
    val ordenSeleccionadaDetalle: WorkOrderWithDetails? = null,
    val mostrarModalDetalleOrden: Boolean = false,
    val historialMotoSeleccionada: List<WorkOrderWithDetails> = emptyList(),
    val mostrarModalHistorialMoto: Boolean = false,

    // Banderas de UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val mostrarDialogoFecha: Boolean = false
)
