package com.TallerJC.motogest.ui.recepcion

import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.local.storage.StoredImageFile
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.ui.util.UiText

enum class CampoDictado {
    FALLA_REPORTADA,
    NOTAS_DANOS
}

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
    val errorNuevoCliente: UiText? = null,

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

    // Diálogo para nueva o edición de motocicleta
    val mostrarDialogoNuevaMoto: Boolean = false,
    val motoEnEdicion: MotorcycleEntity? = null,
    val nuevaMotoMarca: String = "",
    val nuevaMotoModelo: String = "",
    val nuevaMotoColor: String = "",
    val nuevaMotoPlaca: String = "",
    val nuevaMotoAnio: String = "",
    val nuevaMotoVin: String = "",
    val nuevaMotoFotoUri: String = "",
    val nuevaMotoFoto: StoredImageFile? = null,
    val errorNuevaMoto: UiText? = null,

    // Datos de la Moto activa
    val marcaMoto: String = "",
    val modeloMoto: String = "",
    val colorMoto: String = "",
    val placaOpcional: String = "",
    val anioOpcional: String = "",
    val vinOpcional: String = "",
    val fotoMotoUri: String = "",

    // Orden de trabajo
    val servicioSeleccionado: ServiceType = ServiceType.SERVICIO_GENERAL,
    val fallaReportada: String = "",
    val fotosEvidencia: List<String> = emptyList(),
    val fotosEvidenciaCapturadas: List<StoredImageFile> = emptyList(),
    val procesandoImagen: Boolean = false,
    val notasDanosPrevios: String = "",
    val modalidadEntrega: DeliveryType = DeliveryType.TALLER,

    val mostrarAlertaSinDomicilio: Boolean = false,
    val mensajeError: UiText? = null,
    val mensajeDictado: UiText? = null,
    val ordenGuardadaConExito: Boolean = false,

    // --- MÓDULO DE ENTREGA EN TALLER ---
    val ordenesListasParaEntrega: List<WorkOrderWithDetails> = emptyList(),
    val ordenesFiltradasEntrega: List<WorkOrderWithDetails> = emptyList(),
    val textoBusquedaEntrega: String = "",
    val filtroModalidadEntrega: DeliveryType? = null,
    val ordenAEntregar: WorkOrderWithDetails? = null,
    val mostrarDialogoConfirmarEntrega: Boolean = false,
    val mensajeEntregaExitosa: UiText? = null,

    // Sincronización en la nube (Retrofit + Firebase)
    val sincronizandoNube: Boolean = false,
    val mensajeNube: UiText? = null
)
