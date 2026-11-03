package com.TallerJC.motogest.ui.recepcion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.repository.OperacionesRepository
import com.TallerJC.motogest.domain.model.enums.DeliveryType
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
import java.util.UUID

class RecepcionViewModel(
    private val repository: OperacionesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(RecepcionUiState())
    val estadoUi: StateFlow<RecepcionUiState> = _estadoUi.asStateFlow()

    init {
        observarOrdenesListasParaEntrega()
        observarClientesRegistrados()
    }

    private fun observarOrdenesListasParaEntrega() {
        viewModelScope.launch(dispatcher) {
            repository.obtenerTodasLasOrdenes()
                .catch { ex ->
                    _estadoUi.update { it.copy(mensajeError = "Error al sincronizar órdenes: ${ex.message}") }
                }
                .collect { todasLasOrdenes ->
                    val listas = todasLasOrdenes.filter { it.order.status == OrderStatus.LISTA_ENTREGA.name }
                    _estadoUi.update { actual ->
                        actual.copy(ordenesListasParaEntrega = listas)
                    }
                    aplicarFiltrosEntrega()
                }
        }
    }

    private fun observarClientesRegistrados() {
        viewModelScope.launch(dispatcher) {
            repository.obtenerClientes()
                .catch { /* ignorar o registrar */ }
                .collect { listaClientes ->
                    _estadoUi.update { actual ->
                        actual.copy(clientesRegistrados = listaClientes)
                    }
                }
        }
    }

    // --- NAVEGACIÓN ENTRE PESTAÑAS ---
    fun cambiarPestana(indice: Int) {
        _estadoUi.update { it.copy(pestanaSeleccionada = indice, mensajeError = null) }
    }

    // --- GESTIÓN DE CLIENTES (BÚSQUEDA / SELECCIÓN / ALTA) ---

    fun actualizarBusquedaCliente(query: String) {
        _estadoUi.update { actual ->
            val queryLimpia = query.trim()
            val sugerencias = if (actual.clienteSeleccionado == null && queryLimpia.isNotEmpty()) {
                actual.clientesRegistrados.filter {
                    it.name.contains(queryLimpia, ignoreCase = true) ||
                    it.phone.contains(queryLimpia)
                }.take(6)
            } else {
                emptyList()
            }
            actual.copy(
                textoBusquedaCliente = query,
                nombreCliente = query,
                mensajeError = null,
                clientesSugeridos = sugerencias,
                mostrarSugerenciasClientes = sugerencias.isNotEmpty()
            )
        }
    }

    fun actualizarNombreCliente(nuevoNombre: String) {
        actualizarBusquedaCliente(nuevoNombre)
    }

    fun actualizarTelefonoCliente(nuevoTelefono: String) {
        _estadoUi.update { actual ->
            val sugerencias = if (actual.clienteSeleccionado == null && nuevoTelefono.trim().length >= 3) {
                actual.clientesRegistrados.filter {
                    it.phone.contains(nuevoTelefono)
                }.take(5)
            } else {
                emptyList()
            }
            actual.copy(
                telefonoCliente = nuevoTelefono,
                mensajeError = null,
                clientesSugeridos = sugerencias,
                mostrarSugerenciasClientes = sugerencias.isNotEmpty()
            )
        }
    }

    fun actualizarDireccionCliente(valor: String) = _estadoUi.update { it.copy(direccionCliente = valor) }

    fun abrirDialogoNuevoCliente() {
        _estadoUi.update {
            it.copy(
                mostrarDialogoNuevoCliente = true,
                nuevoClienteNombre = it.textoBusquedaCliente.ifBlank { "" },
                nuevoClienteTelefono = "",
                nuevoClienteDireccion = "",
                errorNuevoCliente = null
            )
        }
    }

    fun cerrarDialogoNuevoCliente() {
        _estadoUi.update { it.copy(mostrarDialogoNuevoCliente = false, errorNuevoCliente = null) }
    }

    fun actualizarNuevoClienteNombre(valor: String) = _estadoUi.update { it.copy(nuevoClienteNombre = valor, errorNuevoCliente = null) }
    fun actualizarNuevoClienteTelefono(valor: String) = _estadoUi.update { it.copy(nuevoClienteTelefono = valor.filter { c -> c.isDigit() }.take(10), errorNuevoCliente = null) }
    fun actualizarNuevoClienteDireccion(valor: String) = _estadoUi.update { it.copy(nuevoClienteDireccion = valor) }

    fun guardarYSeleccionarNuevoCliente() {
        val actual = _estadoUi.value
        val nombre = actual.nuevoClienteNombre.trim()
        val telefono = actual.nuevoClienteTelefono.trim()
        val direccion = actual.nuevoClienteDireccion.trim().ifBlank { null }

        if (nombre.isBlank()) {
            _estadoUi.update { it.copy(errorNuevoCliente = "El nombre del cliente es obligatorio.") }
            return
        }
        if (telefono.length < 10) {
            _estadoUi.update { it.copy(errorNuevoCliente = "El celular debe contener los 10 dígitos.") }
            return
        }

        viewModelScope.launch(dispatcher) {
            val nuevoCliente = ClientEntity(
                id = UUID.randomUUID().toString(),
                name = nombre,
                phone = telefono,
                address = direccion,
                defaultDeliveryCost = 0.0,
                createdAt = System.currentTimeMillis(),
                isSynced = false
            )
            repository.registrarCliente(nuevoCliente)
            seleccionarCliente(nuevoCliente)
            _estadoUi.update {
                it.copy(
                    mostrarDialogoNuevoCliente = false,
                    errorNuevoCliente = null,
                    textoBusquedaCliente = "",
                    mostrarSugerenciasClientes = false
                )
            }
        }
    }

    fun seleccionarCliente(cliente: ClientEntity) {
        _estadoUi.update { actual ->
            actual.copy(
                clienteSeleccionado = cliente,
                nombreCliente = cliente.name,
                telefonoCliente = cliente.phone,
                direccionCliente = cliente.address ?: "",
                mostrarSugerenciasClientes = false,
                textoBusquedaCliente = "",
                clientesSugeridos = emptyList(),
                motoSeleccionada = null,
                marcaMoto = "",
                modeloMoto = "",
                colorMoto = "",
                placaOpcional = "",
                anioOpcional = "",
                vinOpcional = ""
            )
        }
        // Cargamos las motocicletas asociadas a este cliente
        viewModelScope.launch(dispatcher) {
            repository.obtenerMotosDeCliente(cliente.id)
                .catch { /* error ignorado */ }
                .collect { motos ->
                    _estadoUi.update { actual ->
                        actual.copy(
                            motosClienteSeleccionado = motos,
                            motoSeleccionada = actual.motoSeleccionada?.let { mSel ->
                                motos.find { it.id == mSel.id }
                            }
                        )
                    }
                }
        }
    }

    fun deseleccionarCliente() {
        _estadoUi.update { actual ->
            actual.copy(
                clienteSeleccionado = null,
                nombreCliente = "",
                telefonoCliente = "",
                direccionCliente = "",
                motosClienteSeleccionado = emptyList(),
                motoSeleccionada = null,
                marcaMoto = "",
                modeloMoto = "",
                colorMoto = "",
                placaOpcional = "",
                anioOpcional = "",
                vinOpcional = "",
                mostrarSugerenciasClientes = false,
                textoBusquedaCliente = ""
            )
        }
    }

    // --- GESTIÓN DE MOTOCICLETAS (SELECCIÓN / ALTA VÍA MODAL) ---

    fun abrirDialogoNuevaMoto() {
        _estadoUi.update {
            it.copy(
                mostrarDialogoNuevaMoto = true,
                nuevaMotoMarca = "",
                nuevaMotoModelo = "",
                nuevaMotoColor = "",
                nuevaMotoPlaca = "",
                nuevaMotoAnio = "",
                nuevaMotoVin = "",
                nuevaMotoFotoUri = "local://moto_${System.currentTimeMillis()}.jpg",
                errorNuevaMoto = null
            )
        }
    }

    fun cerrarDialogoNuevaMoto() {
        _estadoUi.update { it.copy(mostrarDialogoNuevaMoto = false, errorNuevaMoto = null) }
    }

    fun actualizarNuevaMotoMarca(valor: String) = _estadoUi.update { it.copy(nuevaMotoMarca = valor, errorNuevaMoto = null) }
    fun actualizarNuevaMotoModelo(valor: String) = _estadoUi.update { it.copy(nuevaMotoModelo = valor, errorNuevaMoto = null) }
    fun actualizarNuevaMotoColor(valor: String) = _estadoUi.update { it.copy(nuevaMotoColor = valor, errorNuevaMoto = null) }
    fun actualizarNuevaMotoPlaca(valor: String) = _estadoUi.update { it.copy(nuevaMotoPlaca = valor) }
    fun actualizarNuevaMotoAnio(valor: String) = _estadoUi.update { it.copy(nuevaMotoAnio = valor.filter { c -> c.isDigit() }.take(4)) }
    fun actualizarNuevaMotoVin(valor: String) = _estadoUi.update { it.copy(nuevaMotoVin = valor) }
    fun actualizarNuevaMotoFoto(fotoUri: String) = _estadoUi.update { it.copy(nuevaMotoFotoUri = fotoUri) }

    fun guardarYSeleccionarNuevaMoto() {
        val actual = _estadoUi.value
        val cliente = actual.clienteSeleccionado
        if (cliente == null) {
            _estadoUi.update { it.copy(errorNuevaMoto = "Primero debes seleccionar un cliente.") }
            return
        }
        val marca = actual.nuevaMotoMarca.trim()
        val modelo = actual.nuevaMotoModelo.trim()
        val color = actual.nuevaMotoColor.trim()

        if (marca.isBlank() || modelo.isBlank() || color.isBlank()) {
            _estadoUi.update { it.copy(errorNuevaMoto = "Marca, modelo y color son obligatorios.") }
            return
        }

        viewModelScope.launch(dispatcher) {
            val nuevaMoto = MotorcycleEntity(
                id = UUID.randomUUID().toString(),
                clientId = cliente.id,
                brand = marca,
                model = modelo,
                color = color,
                photoUrl = actual.nuevaMotoFotoUri,
                plate = actual.nuevaMotoPlaca.trim().ifBlank { null },
                year = actual.nuevaMotoAnio.toIntOrNull(),
                vin = actual.nuevaMotoVin.trim().ifBlank { null },
                createdAt = System.currentTimeMillis(),
                isSynced = false
            )
            repository.registrarMoto(nuevaMoto)
            seleccionarMoto(nuevaMoto)
            _estadoUi.update {
                it.copy(
                    mostrarDialogoNuevaMoto = false,
                    errorNuevaMoto = null
                )
            }
        }
    }

    fun seleccionarMoto(moto: MotorcycleEntity) {
        _estadoUi.update { actual ->
            actual.copy(
                motoSeleccionada = moto,
                marcaMoto = moto.brand,
                modeloMoto = moto.model,
                colorMoto = moto.color,
                placaOpcional = moto.plate ?: "",
                anioOpcional = moto.year?.toString() ?: "",
                vinOpcional = moto.vin ?: "",
                fotoMotoUri = moto.photoUrl
            )
        }
    }

    fun deseleccionarMoto() {
        _estadoUi.update { actual ->
            actual.copy(
                motoSeleccionada = null,
                marcaMoto = "",
                modeloMoto = "",
                colorMoto = "",
                placaOpcional = "",
                anioOpcional = "",
                vinOpcional = "",
                fotoMotoUri = "local://moto_default.jpg"
            )
        }
    }

    fun registrarNuevaMotoParaCliente() {
        abrirDialogoNuevaMoto()
    }

    fun verHistorialMoto(moto: MotorcycleEntity) {
        viewModelScope.launch(dispatcher) {
            repository.obtenerHistorialMoto(moto.id)
                .catch { /* error ignorado */ }
                .collect { ordenes ->
                    _estadoUi.update {
                        it.copy(
                            historialMotoSeleccionada = ordenes,
                            mostrarModalHistorial = true
                        )
                    }
                }
        }
    }

    fun cerrarModalHistorial() {
        _estadoUi.update { it.copy(mostrarModalHistorial = false) }
    }

    // --- FORMULARIO DE ORDEN DE SERVICIO ---
    fun actualizarMarcaMoto(valor: String) = _estadoUi.update { it.copy(marcaMoto = valor, mensajeError = null) }
    fun actualizarModeloMoto(valor: String) = _estadoUi.update { it.copy(modeloMoto = valor, mensajeError = null) }
    fun actualizarColorMoto(valor: String) = _estadoUi.update { it.copy(colorMoto = valor, mensajeError = null) }
    fun actualizarPlaca(valor: String) = _estadoUi.update { it.copy(placaOpcional = valor) }
    fun actualizarAnio(valor: String) = _estadoUi.update { it.copy(anioOpcional = valor.filter { c -> c.isDigit() }.take(4)) }
    fun actualizarVin(valor: String) = _estadoUi.update { it.copy(vinOpcional = valor) }
    fun actualizarFotoMoto(fotoUri: String) = _estadoUi.update { it.copy(fotoMotoUri = fotoUri) }

    fun seleccionarServicio(servicio: ServiceType) = _estadoUi.update { it.copy(servicioSeleccionado = servicio) }
    fun actualizarFallaReportada(valor: String) = _estadoUi.update { it.copy(fallaReportada = valor, mensajeError = null) }
    fun actualizarNotasDanos(valor: String) = _estadoUi.update { it.copy(notasDanosPrevios = valor) }

    fun cambiarModalidadEntrega(modalidad: DeliveryType) {
        _estadoUi.update { actual ->
            val requiereAlerta = modalidad == DeliveryType.DOMICILIO && actual.direccionCliente.isBlank()
            actual.copy(
                modalidadEntrega = modalidad,
                mostrarAlertaSinDomicilio = requiereAlerta
            )
        }
    }

    fun agregarFotoEvidenciaSimulada() {
        _estadoUi.update { actual ->
            if (actual.fotosEvidencia.size < 5) {
                val nuevaFoto = "file://evidencia_${actual.fotosEvidencia.size + 1}.jpg"
                actual.copy(fotosEvidencia = actual.fotosEvidencia + nuevaFoto)
            } else actual
        }
    }

    fun descartarAlertaDomicilio() = _estadoUi.update { it.copy(mostrarAlertaSinDomicilio = false) }
    fun consumirMensajeExito() = _estadoUi.update { it.copy(ordenGuardadaConExito = false) }
    fun consumirMensajeNube() = _estadoUi.update { it.copy(mensajeNube = null) }

    fun sincronizarConNube() {
        viewModelScope.launch(dispatcher) {
            _estadoUi.update { it.copy(sincronizandoNube = true, mensajeNube = null) }
            val exito = repository.sincronizarConNube()
            _estadoUi.update {
                it.copy(
                    sincronizandoNube = false,
                    mensajeNube = if (exito) "✓ Sincronización con Firebase completada." else "No se pudo sincronizar con Firebase. Verifique su conexión."
                )
            }
        }
    }

    fun guardarOrden() {
        val actual = _estadoUi.value

        // Validación de cliente
        val clienteEntity = if (actual.clienteSeleccionado != null) {
            actual.clienteSeleccionado.copy(
                name = actual.nombreCliente.trim().ifBlank { actual.clienteSeleccionado.name },
                phone = actual.telefonoCliente.trim().ifBlank { actual.clienteSeleccionado.phone },
                address = actual.direccionCliente.trim().ifBlank { actual.clienteSeleccionado.address }
            )
        } else {
            if (actual.nombreCliente.isBlank() || actual.telefonoCliente.length < 10) {
                _estadoUi.update { it.copy(mensajeError = "Ingresa nombre y teléfono de 10 dígitos del cliente (o selecciónalo arriba).") }
                return
            }
            ClientEntity(
                id = UUID.randomUUID().toString(),
                name = actual.nombreCliente.trim(),
                phone = actual.telefonoCliente.trim(),
                address = actual.direccionCliente.trim().ifBlank { null },
                defaultDeliveryCost = 0.0,
                createdAt = System.currentTimeMillis(),
                isSynced = false
            )
        }

        // Validación de motocicleta
        val motoEntity = if (actual.motoSeleccionada != null) {
            actual.motoSeleccionada.copy(
                brand = actual.marcaMoto.trim().ifBlank { actual.motoSeleccionada.brand },
                model = actual.modeloMoto.trim().ifBlank { actual.motoSeleccionada.model },
                color = actual.colorMoto.trim().ifBlank { actual.motoSeleccionada.color },
                photoUrl = actual.fotoMotoUri,
                plate = actual.placaOpcional.trim().ifBlank { actual.motoSeleccionada.plate },
                year = actual.anioOpcional.toIntOrNull() ?: actual.motoSeleccionada.year,
                vin = actual.vinOpcional.trim().ifBlank { actual.motoSeleccionada.vin }
            )
        } else {
            if (actual.marcaMoto.isBlank() || actual.modeloMoto.isBlank() || actual.colorMoto.isBlank()) {
                _estadoUi.update { it.copy(mensajeError = "Marca, modelo y color de la moto son obligatorios.") }
                return
            }
            MotorcycleEntity(
                id = UUID.randomUUID().toString(),
                clientId = clienteEntity.id,
                brand = actual.marcaMoto.trim(),
                model = actual.modeloMoto.trim(),
                color = actual.colorMoto.trim(),
                photoUrl = actual.fotoMotoUri,
                plate = actual.placaOpcional.trim().ifBlank { null },
                year = actual.anioOpcional.toIntOrNull(),
                vin = actual.vinOpcional.trim().ifBlank { null },
                createdAt = System.currentTimeMillis(),
                isSynced = false
            )
        }

        if (actual.fallaReportada.isBlank()) {
            _estadoUi.update { it.copy(mensajeError = "Describe el problema o motivo de ingreso de la unidad.") }
            return
        }

        viewModelScope.launch(dispatcher) {
            val ahora = System.currentTimeMillis()
            val idOrden = UUID.randomUUID().toString()
            val ordenEntity = WorkOrderEntity(
                id = idOrden,
                folio = "ORD-${ahora.toString().takeLast(5)}",
                clientId = clienteEntity.id,
                motorcycleId = motoEntity.id,
                receivedByUserId = "EMP-RECEPCION-01",
                mechanicUserId = null,
                status = OrderStatus.RECIBIDA.name,
                serviceType = actual.servicioSeleccionado.name,
                reportedFailure = actual.fallaReportada.trim(),
                intakeDamageNotes = actual.notasDanosPrevios.trim().ifBlank { null },
                intakePhotos = actual.fotosEvidencia,
                deliveryType = actual.modalidadEntrega.name,
                deliveryCost = 0.0,
                totalLaborCost = 0.0,
                totalPartsCost = 0.0,
                totalAmount = 0.0,
                recommendations = null,
                createdAt = ahora,
                startedAt = null,
                completedAt = null,
                deliveredAt = null,
                updatedAt = ahora,
                isSynced = false
            )

            repository.registrarNuevaOrden(clienteEntity, motoEntity, ordenEntity)

            // Reiniciamos el formulario manteniendo listas
            _estadoUi.update {
                RecepcionUiState(
                    pestanaSeleccionada = it.pestanaSeleccionada,
                    ordenGuardadaConExito = true,
                    clientesRegistrados = it.clientesRegistrados,
                    ordenesListasParaEntrega = it.ordenesListasParaEntrega,
                    ordenesFiltradasEntrega = it.ordenesFiltradasEntrega
                )
            }
        }
    }

    // --- MÓDULO DE ENTREGA EN TALLER ---

    fun actualizarBusquedaEntrega(texto: String) {
        _estadoUi.update { it.copy(textoBusquedaEntrega = texto) }
        aplicarFiltrosEntrega()
    }

    fun cambiarFiltroModalidadEntrega(modalidad: DeliveryType?) {
        _estadoUi.update { it.copy(filtroModalidadEntrega = modalidad) }
        aplicarFiltrosEntrega()
    }

    fun solicitarEntregaEnTaller(ordenDetalle: WorkOrderWithDetails) {
        _estadoUi.update {
            it.copy(
                ordenAEntregar = ordenDetalle,
                mostrarDialogoConfirmarEntrega = true
            )
        }
    }

    fun descartarDialogoConfirmarEntrega() {
        _estadoUi.update {
            it.copy(
                ordenAEntregar = null,
                mostrarDialogoConfirmarEntrega = false
            )
        }
    }

    fun confirmarEntregaEnTaller() {
        val ordenDetalle = _estadoUi.value.ordenAEntregar ?: return
        viewModelScope.launch(dispatcher) {
            try {
                repository.marcarOrdenEntregada(
                    ordenActual = ordenDetalle.order,
                    clienteActual = ordenDetalle.client,
                    costoEnvioFinal = 0.0,
                    modalidadFinal = DeliveryType.TALLER
                )
                _estadoUi.update {
                    it.copy(
                        ordenAEntregar = null,
                        mostrarDialogoConfirmarEntrega = false,
                        mensajeEntregaExitosa = "Orden ${ordenDetalle.order.folio} entregada con éxito al cliente en mostrador."
                    )
                }
            } catch (ex: Exception) {
                _estadoUi.update {
                    it.copy(mensajeError = "No se pudo registrar la entrega: ${ex.message}")
                }
            }
        }
    }

    fun consumirMensajeEntregaExitosa() {
        _estadoUi.update { it.copy(mensajeEntregaExitosa = null) }
    }

    private fun aplicarFiltrosEntrega() {
        val estadoActual = _estadoUi.value
        val busqueda = estadoActual.textoBusquedaEntrega.trim().lowercase()
        val filtroMod = estadoActual.filtroModalidadEntrega

        val filtradas = estadoActual.ordenesListasParaEntrega.filter { detalle ->
            val coincideModalidad = (filtroMod == null) || (detalle.order.deliveryType == filtroMod.name)
            val coincideTexto = busqueda.isEmpty() ||
                    detalle.client.name.lowercase().contains(busqueda) ||
                    detalle.client.phone.contains(busqueda) ||
                    detalle.motorcycle.brand.lowercase().contains(busqueda) ||
                    detalle.motorcycle.model.lowercase().contains(busqueda) ||
                    (detalle.motorcycle.plate?.lowercase()?.contains(busqueda) == true) ||
                    detalle.order.folio.lowercase().contains(busqueda)

            coincideModalidad && coincideTexto
        }

        _estadoUi.update { it.copy(ordenesFiltradasEntrega = filtradas) }
    }

    companion object {
        fun provideFactory(
            repository: OperacionesRepository,
            dispatcher: CoroutineDispatcher = Dispatchers.Main
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RecepcionViewModel(repository, dispatcher) as T
                }
            }
    }
}
