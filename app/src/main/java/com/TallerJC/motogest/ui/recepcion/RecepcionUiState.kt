package com.TallerJC.motogest.ui.recepcion

import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.ServiceType

// Representa el estado actual de la pantalla en un momento dado
data class RecepcionUiState(
    // Datos del Cliente
    val nombreCliente: String = "",
    val telefonoCliente: String = "",
    val direccionCliente: String = "",

    // Datos de la Moto
    val marcaMoto: String = "",
    val modeloMoto: String = "",
    val colorMoto: String = "",
    val placaOpcional: String = "",
    val anioOpcional: String = "",
    val vinOpcional: String = "",
    val fotoMotoUri: String = " local://moto_default.jpg",

    // Orden de trabajo
    val servicioSeleccionado: ServiceType = ServiceType.SERVICIO_GENERAL,
    val fallaReportada: String = "",
    val fotosEvidencia: List<String> = emptyList(), // Máximo 5 fotos (RF-07)
    val notasDanosPrevios: String = "", // Se habilita si fotosEvidencia.isNotEmpty()
    val modalidadEntrega: DeliveryType = DeliveryType.TALLER,

    val mostrarAlertaSinDomicilio: Boolean = false,
    val mensajeError: String? = null,
    val ordenGuardadaConExito: Boolean = false,

    // Lista de opciones para los chips
    val opcionesServicio: List<String> = listOf("Servicio", "Frenos", "Ajuste", "Reparación", "Otro")
)