package com.TallerJC.motogest.ui.recepcion

// Representa el estado actual de la pantalla en un momento dado
data class RecepcionUiState(
    // Datos del Cliente
    val nombreCliente: String = "",
    val telefonoCliente: String = "",
    val direccionCliente: String = "",

    // Datos de la Moto
    val marcaMoto: String = "",
    val modeloColorMoto: String = "",
    val numeroSerieOpcional: String = "",

    // Orden de Servicio
    val servicioSeleccionado: String = "Servicio",
    val notasDañosYFalla: String = "",
    val modalidadEntrega: String = "Taller",

    // Lista de opciones para los chips[cite: 11, 24]
    val opcionesServicio: List<String> = listOf("Servicio", "Frenos", "Ajuste", "Reparación", "Otro")
)