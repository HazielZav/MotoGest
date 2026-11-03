package com.TallerJC.motogest.ui.recepcion

import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.ServiceType

data class RecepcionUiState(
    // Pestaña activa (0 = Nueva Recepción, 1 = Entrega en Taller)
    val pestanaSeleccionada: Int = 0,

    // --- CLIENTE REGISTRADO / SUGERENCIAS ---
    val clientesRegistrados: List<ClientEntity> = emptyList(),
    val clienteSeleccionado: ClientEntity? = null,
    val textoBusquedaCliente: String = "",
    val clientesSugeridos: List<ClientEntity> = emptyList(),
    val mostrarSugerenciasClientes: Boolean = false,

    // Diálogo para nuevo cliente
    val mostrarDialogoNuevoCliente: Boolean = false,
    val nuevoClienteNombre: String = "",
    val nuevoClienteTelefono: String = "",
    val nuevoClienteDireccion: String = "",
    val errorNuevoCliente: String? = null,

    // --- FORMULARIO NUEVA RECEPCIÓN ---
    // Datos del Cliente (persistidos o activos)
    val nombreCliente: String = "",
    val telefonoCliente: String = "",
    val direccionCliente: String = "",

    // Motos del cliente seleccionado
    val motosClienteSeleccionado: List<MotorcycleEntity> = emptyList(),
    val motoSeleccionada: MotorcycleEntity? = null,
    val historialMotoSeleccionada: List<WorkOrderWithDetails> = emptyList(),
    val mostrarModalHistorial: Boolean = false,

    // Diálogo para nueva motocicleta
    val mostrarDialogoNuevaMoto: Boolean = false,
    val nuevaMotoMarca: String = "",
    val nuevaMotoModelo: String = "",
    val nuevaMotoColor: String = "",
    val nuevaMotoPlaca: String = "",
    val nuevaMotoAnio: String = "",
    val nuevaMotoVin: String = "",
    val nuevaMotoFotoUri: String = "local://moto_default.jpg",
    val errorNuevaMoto: String? = null,

    // Datos de la Moto activa
    val marcaMoto: String = "",
    val modeloMoto: String = "",
    val colorMoto: String = "",
    val placaOpcional: String = "",
    val anioOpcional: String = "",
    val vinOpcional: String = "",
    val fotoMotoUri: String = "local://moto_default.jpg",

    // Orden de trabajo
    val servicioSeleccionado: ServiceType = ServiceType.SERVICIO_GENERAL,
    val fallaReportada: String = "",
    val fotosEvidencia: List<String> = emptyList(),
    val notasDanosPrevios: String = "",
    val modalidadEntrega: DeliveryType = DeliveryType.TALLER,

    val mostrarAlertaSinDomicilio: Boolean = false,
    val mensajeError: String? = null,
    val ordenGuardadaConExito: Boolean = false,

    // Lista de opciones para los chips
    val opcionesServicio: List<String> = listOf("Servicio", "Frenos", "Ajuste", "Reparación", "Otro"),

    // --- MÓDULO DE ENTREGA EN TALLER ---
    val ordenesListasParaEntrega: List<WorkOrderWithDetails> = emptyList(),
    val ordenesFiltradasEntrega: List<WorkOrderWithDetails> = emptyList(),
    val textoBusquedaEntrega: String = "",
    val filtroModalidadEntrega: DeliveryType? = null,
    val ordenAEntregar: WorkOrderWithDetails? = null,
    val mostrarDialogoConfirmarEntrega: Boolean = false,
    val mensajeEntregaExitosa: String? = null,

    // Sincronización en la nube (Retrofit + Firebase)
    val sincronizandoNube: Boolean = false,
    val mensajeNube: String? = null
)
