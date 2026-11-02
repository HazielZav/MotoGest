package com.TallerJC.motogest.ui.recepcion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.repository.OperacionesRepository
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class RecepcionViewModel(
    private val repository: OperacionesRepository
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(RecepcionUiState())
    val estadoUi: StateFlow<RecepcionUiState> = _estadoUi.asStateFlow()

    fun actualizarNombreCliente(nuevoNombre: String) { _estadoUi.update { estadoActual -> estadoActual.copy(nombreCliente = nuevoNombre) } }
    fun actualizarTelefonoCliente(nuevoTelefono: String) { _estadoUi.update { estadoActual -> estadoActual.copy(telefonoCliente = nuevoTelefono) } }
    fun actualizarDireccionCliente(valor: String) = _estadoUi.update { it.copy(direccionCliente = valor) }

    fun actualizarMarcaMoto(valor: String) = _estadoUi.update { it.copy(marcaMoto = valor, mensajeError = null) }
    fun actualizarModeloMoto(valor: String) = _estadoUi.update { it.copy(modeloMoto = valor, mensajeError = null) }
    fun actualizarColorMoto(valor: String) = _estadoUi.update { it.copy(colorMoto = valor, mensajeError = null) }
    fun actualizarPlaca(valor: String) = _estadoUi.update { it.copy(placaOpcional = valor) }
    fun actualizarAnio(valor: String) = _estadoUi.update { it.copy(anioOpcional = valor.filter { c -> c.isDigit() }.take(4)) }
    fun actualizarVin(valor: String) = _estadoUi.update { it.copy(vinOpcional = valor) }

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

    fun guardarOrden() {
        val actual = _estadoUi.value

        if (actual.nombreCliente.isBlank() || actual.telefonoCliente.length < 10) {
            _estadoUi.update { it.copy(mensajeError = "Ingresa nombre y teléfono de 10 dígitos del cliente.") }
            return
        }
        if (actual.marcaMoto.isBlank() || actual.modeloMoto.isBlank() || actual.colorMoto.isBlank()) {
            _estadoUi.update { it.copy(mensajeError = "Marca, modelo y color de la moto son obligatorios.") }
            return
        }
        if (actual.fallaReportada.isBlank()) {
            _estadoUi.update { it.copy(mensajeError = "Describe o dicta el motivo de ingreso / falla.") }
            return
        }

        viewModelScope.launch {
            val ahora = System.currentTimeMillis()
            val idCliente = UUID.randomUUID().toString()
            val idMoto = UUID.randomUUID().toString()
            val idOrden = UUID.randomUUID().toString()

            val clienteEntity = ClientEntity(
                id = idCliente,
                name = actual.nombreCliente.trim(),
                phone = actual.telefonoCliente.trim(),
                address = actual.direccionCliente.trim().ifBlank { null },
                defaultDeliveryCost = 0.0,
                createdAt = ahora,
                isSynced = false
            )

            val motoEntity = MotorcycleEntity(
                id = idMoto,
                clientId = idCliente,
                brand = actual.marcaMoto.trim(),
                model = actual.modeloMoto.trim(),
                color = actual.colorMoto.trim(),
                photoUrl = actual.fotoMotoUri,
                plate = actual.placaOpcional.trim().ifBlank { null },
                year = actual.anioOpcional.toIntOrNull(),
                vin = actual.vinOpcional.trim().ifBlank { null },
                createdAt = ahora,
                isSynced = false
            )

            val ordenEntity = WorkOrderEntity(
                id = idOrden,
                folio = "ORD-${ahora.toString().takeLast(5)}",
                clientId = idCliente,
                motorcycleId = idMoto,
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
            // Reiniciamos el formulario y notificamos éxito
            _estadoUi.value = RecepcionUiState(ordenGuardadaConExito = true)
        }
    }

    companion object {
        fun provideFactory(repository: OperacionesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RecepcionViewModel(repository) as T
                }
            }
    }
}