package com.TallerJC.motogest.ui.recepcion

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecepcionViewModel : ViewModel() {

    private val _estadoUi = MutableStateFlow(RecepcionUiState())

    val estadoUi: StateFlow<RecepcionUiState> = _estadoUi.asStateFlow()



    fun actualizarNombreCliente(nuevoNombre: String) {
        _estadoUi.update { estadoActual -> estadoActual.copy(nombreCliente = nuevoNombre) }
    }

    fun actualizarTelefonoCliente(nuevoTelefono: String) {
        _estadoUi.update { estadoActual -> estadoActual.copy(telefonoCliente = nuevoTelefono) }
    }

    fun actualizarMarcaMoto(nuevaMarca: String) {
        _estadoUi.update { estadoActual -> estadoActual.copy(marcaMoto = nuevaMarca) }
    }

    fun actualizarModeloColorMoto(nuevoModelo: String) {
        _estadoUi.update { estadoActual -> estadoActual.copy(modeloColorMoto = nuevoModelo) }
    }

    fun seleccionarServicio(servicio: String) {
        _estadoUi.update { estadoActual -> estadoActual.copy(servicioSeleccionado = servicio) }
    }

    fun actualizarNotasFalla(nuevasNotas: String) {
        _estadoUi.update { estadoActual -> estadoActual.copy(notasDañosYFalla = nuevasNotas) }
    }

    fun cambiarModalidadEntrega(nuevaModalidad: String) {
        _estadoUi.update { estadoActual -> estadoActual.copy(modalidadEntrega = nuevaModalidad) }
    }

    fun guardarOrden() {
        // TODO: Aquí luego conectaremos el Repository y Room
    }
}