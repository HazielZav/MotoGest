package com.TallerJC.motogest.ui.gerencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.TallerJC.motogest.data.local.entity.UserEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.repository.GerenciaRepository
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.domain.model.enums.UserRole
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

class GerenciaViewModel(
    private val repository: GerenciaRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(GerenciaUiState(isLoading = true))
    val estadoUi: StateFlow<GerenciaUiState> = _estadoUi.asStateFlow()

    private var todasLasOrdenesEntregadas: List<WorkOrderWithDetails> = emptyList()
    private var todasLasOrdenesSistema: List<WorkOrderWithDetails> = emptyList()
    private var todosLosEmpleados: List<UserEntity> = emptyList()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch(dispatcher) {
            _estadoUi.update { it.copy(isLoading = true, error = null) }

            // Observamos todas las órdenes entregadas (para finanzas)
            launch {
                repository.obtenerOrdenesEntregadas()
                    .catch { ex ->
                        _estadoUi.update {
                            it.copy(
                                isLoading = false,
                                error = "Error al consultar las órdenes de gerencia: ${ex.message}"
                            )
                        }
                    }
                    .collect { ordenes ->
                        todasLasOrdenesEntregadas = ordenes
                        recalcularMetricasYFiltros()
                    }
            }

            // Observamos todo el sistema (para historial de unidades y directorio)
            launch {
                repository.obtenerTodasLasOrdenes()
                    .catch { /* error ignorado */ }
                    .collect { todas ->
                        todasLasOrdenesSistema = todas
                        _estadoUi.update { actual ->
                            actual.copy(todasLasOrdenes = todas)
                        }
                        aplicarFiltroDirectorio()
                    }
            }

            // Observamos los empleados y sus roles
            launch {
                repository.obtenerEmpleados()
                    .catch { /* error ignorado */ }
                    .collect { listaEmpleados ->
                        todosLosEmpleados = listaEmpleados
                        _estadoUi.update { actual ->
                            actual.copy(empleados = listaEmpleados)
                        }
                        aplicarFiltroEmpleados()
                    }
            }
        }
    }

    fun cargarDatosDemo() {
        viewModelScope.launch(dispatcher) {
            _estadoUi.update { it.copy(isLoading = true, error = null) }
            try {
                repository.insertarDatosDemo()
            } catch (ex: Exception) {
                _estadoUi.update {
                    it.copy(
                        isLoading = false,
                        error = "No se pudieron insertar los datos demo: ${ex.message}"
                    )
                }
            }
        }
    }

    fun cambiarPestana(indice: Int) {
        _estadoUi.update { it.copy(pestanaSeleccionada = indice) }
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

    fun actualizarBusquedaDirectorio(texto: String) {
        _estadoUi.update { it.copy(textoBusquedaDirectorio = texto) }
        aplicarFiltroDirectorio()
    }

    fun abrirDetalleOrden(orden: WorkOrderWithDetails) {
        _estadoUi.update {
            it.copy(
                ordenSeleccionadaDetalle = orden,
                mostrarModalDetalleOrden = true
            )
        }
    }

    fun cerrarDetalleOrden() {
        _estadoUi.update {
            it.copy(
                ordenSeleccionadaDetalle = null,
                mostrarModalDetalleOrden = false
            )
        }
    }

    fun verHistorialMoto(motoId: String) {
        viewModelScope.launch(dispatcher) {
            repository.obtenerHistorialMoto(motoId)
                .catch { /* error ignorado */ }
                .collect { historial ->
                    _estadoUi.update {
                        it.copy(
                            historialMotoSeleccionada = historial,
                            mostrarModalHistorialMoto = true
                        )
                    }
                }
        }
    }

    fun cerrarModalHistorialMoto() {
        _estadoUi.update { it.copy(mostrarModalHistorialMoto = false) }
    }

    // --- GESTIÓN DE PERSONAL Y ROLES ---

    fun actualizarBusquedaEmpleados(texto: String) {
        _estadoUi.update { it.copy(textoBusquedaEmpleados = texto) }
        aplicarFiltroEmpleados()
    }

    fun cambiarFiltroRolEmpleado(rol: UserRole?) {
        _estadoUi.update { it.copy(filtroRolEmpleado = rol) }
        aplicarFiltroEmpleados()
    }

    fun abrirDialogoNuevoEmpleado() {
        _estadoUi.update {
            it.copy(
                mostrarDialogoEmpleado = true,
                empleadoEnEdicion = null,
                formEmpleadoNombre = "",
                formEmpleadoCorreo = "",
                formEmpleadoRoles = setOf(UserRole.MECANICO),
                formEmpleadoActivo = true,
                errorFormularioEmpleado = null,
                mensajePersonal = null
            )
        }
    }

    fun abrirDialogoEditarEmpleado(empleado: UserEntity) {
        _estadoUi.update {
            it.copy(
                mostrarDialogoEmpleado = true,
                empleadoEnEdicion = empleado,
                formEmpleadoNombre = empleado.fullName,
                formEmpleadoCorreo = empleado.email,
                formEmpleadoRoles = parseRoles(empleado.roles),
                formEmpleadoActivo = empleado.isActive,
                errorFormularioEmpleado = null,
                mensajePersonal = null
            )
        }
    }

    fun cerrarDialogoEmpleado() {
        _estadoUi.update {
            it.copy(
                mostrarDialogoEmpleado = false,
                empleadoEnEdicion = null,
                errorFormularioEmpleado = null
            )
        }
    }

    fun actualizarFormEmpleadoNombre(nombre: String) {
        _estadoUi.update { it.copy(formEmpleadoNombre = nombre, errorFormularioEmpleado = null) }
    }

    fun actualizarFormEmpleadoCorreo(correo: String) {
        _estadoUi.update { it.copy(formEmpleadoCorreo = correo, errorFormularioEmpleado = null) }
    }

    fun toggleFormEmpleadoRol(rol: UserRole) {
        _estadoUi.update { actual ->
            actual.copy(
                formEmpleadoRoles = setOf(rol),
                errorFormularioEmpleado = null
            )
        }
    }

    fun actualizarFormEmpleadoActivo(activo: Boolean) {
        _estadoUi.update { it.copy(formEmpleadoActivo = activo) }
    }

    fun guardarEmpleadoFormulario() {
        val estado = _estadoUi.value
        val nombre = estado.formEmpleadoNombre.trim()
        val correo = estado.formEmpleadoCorreo.trim()
        val roles = estado.formEmpleadoRoles

        val errorValidacion = when {
            nombre.isBlank() -> "El nombre completo es obligatorio."
            correo.isBlank() -> "El correo electrónico es obligatorio."
            !EMAIL_REGEX.matches(correo) -> "Ingresa un correo electrónico válido."
            todosLosEmpleados.any {
                it.id != estado.empleadoEnEdicion?.id && it.email.equals(correo, ignoreCase = true)
            } -> "Ya existe un empleado registrado con ese correo."
            roles.isEmpty() -> "Selecciona al menos un rol para el empleado."
            else -> null
        }

        if (errorValidacion != null) {
            _estadoUi.update { it.copy(errorFormularioEmpleado = errorValidacion) }
            return
        }

        val rolesOrdenados = serializeRoles(roles)
        val empleadoGuardar = if (estado.empleadoEnEdicion != null) {
            estado.empleadoEnEdicion.copy(
                fullName = nombre,
                email = correo,
                roles = rolesOrdenados,
                isActive = estado.formEmpleadoActivo
            )
        } else {
            UserEntity(
                id = UUID.randomUUID().toString(),
                fullName = nombre,
                email = correo,
                roles = rolesOrdenados,
                isActive = estado.formEmpleadoActivo
            )
        }

        val esEdicion = estado.empleadoEnEdicion != null
        viewModelScope.launch(dispatcher) {
            try {
                repository.guardarEmpleado(empleadoGuardar)
                _estadoUi.update {
                    it.copy(
                        mostrarDialogoEmpleado = false,
                        empleadoEnEdicion = null,
                        errorFormularioEmpleado = null,
                        mensajePersonal = if (esEdicion) {
                            "Empleado ${empleadoGuardar.fullName} actualizado."
                        } else {
                            "Empleado ${empleadoGuardar.fullName} registrado."
                        }
                    )
                }
            } catch (ex: Exception) {
                _estadoUi.update {
                    it.copy(errorFormularioEmpleado = "No se pudo guardar el empleado: ${ex.message}")
                }
            }
        }
    }

    fun abrirGestionRoles(empleado: UserEntity) {
        _estadoUi.update {
            it.copy(
                empleadoGestionRoles = empleado,
                rolesTemporalesSeleccionados = parseRoles(empleado.roles),
                errorGestionRoles = null,
                mensajePersonal = null
            )
        }
    }

    fun cerrarGestionRoles() {
        _estadoUi.update {
            it.copy(
                empleadoGestionRoles = null,
                rolesTemporalesSeleccionados = emptySet(),
                errorGestionRoles = null
            )
        }
    }

    fun toggleRolTemporal(rol: UserRole) {
        _estadoUi.update { actual ->
            actual.copy(
                rolesTemporalesSeleccionados = setOf(rol),
                errorGestionRoles = null
            )
        }
    }

    fun guardarRolesEmpleado() {
        val estado = _estadoUi.value
        val empleado = estado.empleadoGestionRoles ?: return
        val roles = estado.rolesTemporalesSeleccionados

        if (roles.isEmpty()) {
            _estadoUi.update {
                it.copy(errorGestionRoles = "El empleado debe tener al menos un rol asignado.")
            }
            return
        }

        val empleadoActualizado = empleado.copy(roles = serializeRoles(roles))

        viewModelScope.launch(dispatcher) {
            try {
                repository.guardarEmpleado(empleadoActualizado)
                _estadoUi.update {
                    it.copy(
                        empleadoGestionRoles = null,
                        rolesTemporalesSeleccionados = emptySet(),
                        errorGestionRoles = null,
                        mensajePersonal = "Roles actualizados para ${empleado.fullName}."
                    )
                }
            } catch (ex: Exception) {
                _estadoUi.update {
                    it.copy(errorGestionRoles = "Error al actualizar roles: ${ex.message}")
                }
            }
        }
    }

    private fun parseRoles(roles: List<String>): Set<UserRole> {
        return roles.mapNotNull { nombre ->
            runCatching { UserRole.valueOf(nombre) }.getOrNull()
        }.toSet()
    }

    private fun serializeRoles(roles: Set<UserRole>): List<String> {
        return UserRole.entries.filter { it in roles }.map { it.name }
    }

    fun toggleEstadoActivoEmpleado(empleado: UserEntity) {
        val actualizado = empleado.copy(isActive = !empleado.isActive)
        viewModelScope.launch(dispatcher) {
            try {
                repository.guardarEmpleado(actualizado)
                _estadoUi.update {
                    it.copy(
                        mensajePersonal = if (actualizado.isActive) {
                            "${actualizado.fullName} activado."
                        } else {
                            "${actualizado.fullName} desactivado."
                        }
                    )
                }
            } catch (ex: Exception) {
                _estadoUi.update {
                    it.copy(mensajePersonal = "No se pudo cambiar el estado: ${ex.message}")
                }
            }
        }
    }

    fun solicitarEliminarEmpleado(empleado: UserEntity) {
        _estadoUi.update { it.copy(empleadoAEliminar = empleado, mensajePersonal = null) }
    }

    fun cancelarEliminarEmpleado() {
        _estadoUi.update { it.copy(empleadoAEliminar = null) }
    }

    fun confirmarEliminarEmpleado() {
        val empleado = _estadoUi.value.empleadoAEliminar ?: return
        viewModelScope.launch(dispatcher) {
            try {
                repository.eliminarEmpleado(empleado.id)
                _estadoUi.update {
                    it.copy(
                        empleadoAEliminar = null,
                        mensajePersonal = "Empleado ${empleado.fullName} eliminado."
                    )
                }
            } catch (ex: Exception) {
                _estadoUi.update {
                    it.copy(
                        empleadoAEliminar = null,
                        mensajePersonal = "No se pudo eliminar: ${ex.message}"
                    )
                }
            }
        }
    }

    fun sincronizarEmpleadosNube() {
        viewModelScope.launch(dispatcher) {
            val ok = repository.sincronizarEmpleados()
            _estadoUi.update {
                it.copy(
                    mensajePersonal = if (ok) {
                        "Personal sincronizado con la nube."
                    } else {
                        "No se pudo sincronizar con la nube. Trabajando en modo local."
                    }
                )
            }
        }
    }

    fun limpiarMensajePersonal() {
        _estadoUi.update { it.copy(mensajePersonal = null) }
    }

    private fun aplicarFiltroEmpleados() {
        val estado = _estadoUi.value
        val query = estado.textoBusquedaEmpleados.trim()
        val filtroRol = estado.filtroRolEmpleado

        val filtrados = todosLosEmpleados.filter { empleado ->
            val coincideBusqueda = query.isEmpty() ||
                empleado.fullName.contains(query, ignoreCase = true) ||
                empleado.email.contains(query, ignoreCase = true)
            val coincideRol = filtroRol == null || filtroRol.name in empleado.roles
            coincideBusqueda && coincideRol
        }

        _estadoUi.update { it.copy(empleadosFiltrados = filtrados) }
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

    private fun aplicarFiltroDirectorio() {
        val query = _estadoUi.value.textoBusquedaDirectorio.trim()
        val todas = todasLasOrdenesSistema

        val filtradas = if (query.isEmpty()) {
            todas
        } else {
            todas.filter { item ->
                item.client.name.contains(query, ignoreCase = true) ||
                        item.client.phone.contains(query) ||
                        item.motorcycle.model.contains(query, ignoreCase = true) ||
                        item.motorcycle.brand.contains(query, ignoreCase = true) ||
                        item.motorcycle.plate?.contains(query, ignoreCase = true) == true ||
                        item.motorcycle.vin?.contains(query, ignoreCase = true) == true
            }
        }

        _estadoUi.update { it.copy(ordenesDirectorioFiltradas = filtradas) }
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

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        fun provideFactory(
            repository: GerenciaRepository,
            dispatcher: CoroutineDispatcher = Dispatchers.Default
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GerenciaViewModel(repository, dispatcher) as T
                }
            }
    }
}
