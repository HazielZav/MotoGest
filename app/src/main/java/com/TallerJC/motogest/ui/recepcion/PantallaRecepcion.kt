package com.TallerJC.motogest.ui.recepcion

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.TallerJC.motogest.R
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.ui.util.getLabelRes
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PantallaRecepcion(
    viewModel: RecepcionViewModel
) {
    val estadoUi by viewModel.estadoUi.collectAsStateWithLifecycle()

    PantallaRecepcionContenido(
        estadoUi = estadoUi,
        onPestanaCambio = viewModel::cambiarPestana,
        onBusquedaClienteCambio = viewModel::actualizarBusquedaCliente,
        onNombreCambio = viewModel::actualizarNombreCliente,
        onTelefonoCambio = viewModel::actualizarTelefonoCliente,
        onDireccionCambio = viewModel::actualizarDireccionCliente,
        onSeleccionarCliente = viewModel::seleccionarCliente,
        onDeseleccionarCliente = viewModel::deseleccionarCliente,
        onAbrirDialogoNuevoCliente = viewModel::abrirDialogoNuevoCliente,
        onCerrarDialogoNuevoCliente = viewModel::cerrarDialogoNuevoCliente,
        onNuevoClienteNombreCambio = viewModel::actualizarNuevoClienteNombre,
        onNuevoClienteTelefonoCambio = viewModel::actualizarNuevoClienteTelefono,
        onNuevoClienteDireccionCambio = viewModel::actualizarNuevoClienteDireccion,
        onGuardarNuevoCliente = viewModel::guardarYSeleccionarNuevoCliente,
        onSeleccionarMoto = viewModel::seleccionarMoto,
        onDeseleccionarMoto = viewModel::deseleccionarMoto,
        onAbrirDialogoNuevaMoto = viewModel::abrirDialogoNuevaMoto,
        onCerrarDialogoNuevaMoto = viewModel::cerrarDialogoNuevaMoto,
        onNuevaMotoMarcaCambio = viewModel::actualizarNuevaMotoMarca,
        onNuevaMotoModeloCambio = viewModel::actualizarNuevaMotoModelo,
        onNuevaMotoColorCambio = viewModel::actualizarNuevaMotoColor,
        onNuevaMotoPlacaCambio = viewModel::actualizarNuevaMotoPlaca,
        onNuevaMotoAnioCambio = viewModel::actualizarNuevaMotoAnio,
        onNuevaMotoVinCambio = viewModel::actualizarNuevaMotoVin,
        onNuevaMotoFotoCambio = viewModel::actualizarNuevaMotoFoto,
        onGuardarNuevaMoto = viewModel::guardarYSeleccionarNuevaMoto,
        onVerHistorialMoto = viewModel::verHistorialMoto,
        onCerrarHistorialMoto = viewModel::cerrarModalHistorial,
        onFotoMotoCambio = viewModel::actualizarFotoMoto,
        onMarcaCambio = viewModel::actualizarMarcaMoto,
        onModeloCambio = viewModel::actualizarModeloMoto,
        onColorCambio = viewModel::actualizarColorMoto,
        onPlacaCambio = viewModel::actualizarPlaca,
        onAnioCambio = viewModel::actualizarAnio,
        onVinCambio = viewModel::actualizarVin,
        onServicioSeleccionado = viewModel::seleccionarServicio,
        onFallaCambio = viewModel::actualizarFallaReportada,
        onAgregarFoto = viewModel::agregarFotoEvidenciaSimulada,
        onNotasDanosCambio = viewModel::actualizarNotasDanos,
        onModalidadCambio = viewModel::cambiarModalidadEntrega,
        onDescartarAlertaDomicilio = viewModel::descartarAlertaDomicilio,
        onGuardarClic = viewModel::guardarOrden,
        onBusquedaEntregaCambio = viewModel::actualizarBusquedaEntrega,
        onFiltroModalidadEntregaCambio = viewModel::cambiarFiltroModalidadEntrega,
        onSolicitarEntrega = viewModel::solicitarEntregaEnTaller,
        onCancelarEntrega = viewModel::descartarDialogoConfirmarEntrega,
        onConfirmarEntrega = viewModel::confirmarEntregaEnTaller,
        onLimpiarMensajeEntrega = viewModel::consumirMensajeEntregaExitosa,
        onSincronizarNube = viewModel::sincronizarConNube,
        onConsumirMensajeNube = viewModel::consumirMensajeNube
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRecepcionContenido(
    estadoUi: RecepcionUiState,
    onPestanaCambio: (Int) -> Unit,
    onBusquedaClienteCambio: (String) -> Unit = {},
    onNombreCambio: (String) -> Unit,
    onTelefonoCambio: (String) -> Unit,
    onDireccionCambio: (String) -> Unit,
    onSeleccionarCliente: (ClientEntity) -> Unit = {},
    onDeseleccionarCliente: () -> Unit = {},
    onAbrirDialogoNuevoCliente: () -> Unit = {},
    onCerrarDialogoNuevoCliente: () -> Unit = {},
    onNuevoClienteNombreCambio: (String) -> Unit = {},
    onNuevoClienteTelefonoCambio: (String) -> Unit = {},
    onNuevoClienteDireccionCambio: (String) -> Unit = {},
    onGuardarNuevoCliente: () -> Unit = {},
    onSeleccionarMoto: (MotorcycleEntity) -> Unit = {},
    onDeseleccionarMoto: () -> Unit = {},
    onAbrirDialogoNuevaMoto: () -> Unit = {},
    onCerrarDialogoNuevaMoto: () -> Unit = {},
    onNuevaMotoMarcaCambio: (String) -> Unit = {},
    onNuevaMotoModeloCambio: (String) -> Unit = {},
    onNuevaMotoColorCambio: (String) -> Unit = {},
    onNuevaMotoPlacaCambio: (String) -> Unit = {},
    onNuevaMotoAnioCambio: (String) -> Unit = {},
    onNuevaMotoVinCambio: (String) -> Unit = {},
    onNuevaMotoFotoCambio: (String) -> Unit = {},
    onGuardarNuevaMoto: () -> Unit = {},
    onRegistrarNuevaMoto: () -> Unit = onAbrirDialogoNuevaMoto,
    onVerHistorialMoto: (MotorcycleEntity) -> Unit = {},
    onCerrarHistorialMoto: () -> Unit = {},
    onFotoMotoCambio: (String) -> Unit = {},
    onMarcaCambio: (String) -> Unit,
    onModeloCambio: (String) -> Unit,
    onColorCambio: (String) -> Unit,
    onPlacaCambio: (String) -> Unit,
    onAnioCambio: (String) -> Unit,
    onVinCambio: (String) -> Unit,
    onServicioSeleccionado: (ServiceType) -> Unit,
    onFallaCambio: (String) -> Unit,
    onAgregarFoto: () -> Unit,
    onNotasDanosCambio: (String) -> Unit,
    onModalidadCambio: (DeliveryType) -> Unit,
    onDescartarAlertaDomicilio: () -> Unit,
    onGuardarClic: () -> Unit,
    onBusquedaEntregaCambio: (String) -> Unit,
    onFiltroModalidadEntregaCambio: (DeliveryType?) -> Unit,
    onSolicitarEntrega: (WorkOrderWithDetails) -> Unit,
    onCancelarEntrega: () -> Unit,
    onConfirmarEntrega: () -> Unit,
    onLimpiarMensajeEntrega: () -> Unit,
    onSincronizarNube: () -> Unit = {},
    onConsumirMensajeNube: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val formatoMoneda = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.recepcion_titulo),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = if (estadoUi.pestanaSeleccionada == 0)
                                stringResource(R.string.recepcion_subtitulo)
                            else
                                stringResource(R.string.recepcion_entrega_subtitulo),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSincronizarNube,
                        enabled = !estadoUi.sincronizandoNube
                    ) {
                        if (estadoUi.sincronizandoNube) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Default.CloudSync,
                                contentDescription = stringResource(R.string.recepcion_btn_sincronizar_nube),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // PESTAÑAS: 0 = Nueva Recepción, 1 = Entrega en Mostrador / Taller
            PrimaryTabRow(
                selectedTabIndex = estadoUi.pestanaSeleccionada,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = estadoUi.pestanaSeleccionada == 0,
                    onClick = { onPestanaCambio(0) },
                    text = {
                        Text(stringResource(R.string.recepcion_tab_nueva), fontWeight = FontWeight.Bold)
                    },
                    icon = {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                    }
                )
                Tab(
                    selected = estadoUi.pestanaSeleccionada == 1,
                    onClick = { onPestanaCambio(1) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(stringResource(R.string.recepcion_tab_entrega), fontWeight = FontWeight.Bold)
                            if (estadoUi.ordenesListasParaEntrega.isNotEmpty()) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(
                                        text = "${estadoUi.ordenesListasParaEntrega.size}",
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.TaskAlt, contentDescription = null)
                    }
                )
            }

            if (estadoUi.pestanaSeleccionada == 0) {
                // --- PESTAÑA 0: FORMULARIO DE NUEVA RECEPCIÓN ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Feedback de sincronización en la nube
                    estadoUi.mensajeNube?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(text = msg, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                IconButton(onClick = onConsumirMensajeNube, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_cerrar), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Feedback de Éxito al guardar
                    if (estadoUi.ordenGuardadaConExito) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.recepcion_exito_guardar),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(14.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Mensaje de Error de Validación
                    estadoUi.mensajeError?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // ==========================================
                    // 1. SECCIÓN CLIENTE
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recepcion_seccion_cliente),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (estadoUi.clienteSeleccionado != null) {
                            TextButton(onClick = onDeseleccionarCliente) {
                                Text(stringResource(R.string.recepcion_btn_cambiar_cliente))
                            }
                        }
                    }

                    if (estadoUi.clienteSeleccionado == null) {
                        // Barra de búsqueda con botón "+" para nuevo cliente
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = estadoUi.textoBusquedaCliente,
                                onValueChange = {
                                    onBusquedaClienteCambio(it)
                                    onNombreCambio(it)
                                },
                                placeholder = { Text(stringResource(R.string.recepcion_buscar_cliente_placeholder), fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = {
                                    if (estadoUi.textoBusquedaCliente.isNotEmpty()) {
                                        IconButton(onClick = {
                                            onBusquedaClienteCambio("")
                                            onNombreCambio("")
                                        }) {
                                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.btn_limpiar))
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            FilledTonalButton(
                                onClick = onAbrirDialogoNuevoCliente,
                                modifier = Modifier.height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.recepcion_btn_nuevo_cliente), fontWeight = FontWeight.Bold)
                            }
                        }

                        // Sugerencias reactivas al escribir
                        AnimatedVisibility(visible = estadoUi.mostrarSugerenciasClientes && estadoUi.clientesSugeridos.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = stringResource(R.string.recepcion_cliente_sugerencias_label),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    estadoUi.clientesSugeridos.forEach { cliente ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onSeleccionarCliente(cliente) }
                                                .padding(vertical = 8.dp, horizontal = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(cliente.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                Text("Tel: ${cliente.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                cliente.address?.let {
                                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            Badge { Text(stringResource(R.string.recepcion_cliente_registrado_badge)) }
                                        }
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }

                        if (!estadoUi.mostrarSugerenciasClientes) {
                            Text(
                                text = stringResource(R.string.recepcion_sin_cliente_aviso),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Tarjeta de Cliente Seleccionado
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = estadoUi.clienteSeleccionado.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        Text(text = estadoUi.clienteSeleccionado.phone, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                        Text(
                                            text = estadoUi.clienteSeleccionado.address ?: stringResource(R.string.entregas_sin_direccion),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // ==========================================
                    // 2. SECCIÓN MOTOCICLETA
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recepcion_seccion_moto),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (estadoUi.clienteSeleccionado != null) {
                            FilledTonalButton(
                                onClick = onAbrirDialogoNuevaMoto,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.recepcion_btn_nueva_moto), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (estadoUi.clienteSeleccionado == null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = stringResource(R.string.recepcion_sin_cliente_aviso),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Motos del cliente seleccionado
                        if (estadoUi.motosClienteSeleccionado.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.recepcion_sin_motos_aviso),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(onClick = onAbrirDialogoNuevaMoto) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(R.string.recepcion_btn_otra_moto))
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.recepcion_motos_registradas_titulo),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(estadoUi.motosClienteSeleccionado, key = { it.id }) { moto ->
                                    val seleccionada = estadoUi.motoSeleccionada?.id == moto.id
                                    Card(
                                        onClick = { onSeleccionarMoto(moto) },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (seleccionada) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(
                                            width = if (seleccionada) 2.dp else 1.dp,
                                            color = if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.width(230.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${moto.brand} ${moto.model}",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                if (seleccionada) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                            Text("Color: ${moto.color}", style = MaterialTheme.typography.bodySmall)
                                            moto.plate?.let { Text("Placa: $it", style = MaterialTheme.typography.bodySmall) }
                                            moto.year?.let { Text("Año: $it", style = MaterialTheme.typography.bodySmall) }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedButton(
                                                onClick = { onVerHistorialMoto(moto) },
                                                modifier = Modifier.fillMaxWidth().height(34.dp),
                                                contentPadding = PaddingValues(2.dp)
                                            ) {
                                                Text(stringResource(R.string.recepcion_btn_ver_historial), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Detalle de la moto seleccionada activa
                        if (estadoUi.motoSeleccionada != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.recepcion_moto_seleccionada_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${estadoUi.motoSeleccionada.brand} ${estadoUi.motoSeleccionada.model} (${estadoUi.motoSeleccionada.color})",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        val extraInfo = listOfNotNull(
                                            estadoUi.motoSeleccionada.plate?.let { "Placa: $it" },
                                            estadoUi.motoSeleccionada.year?.let { "Año: $it" },
                                            estadoUi.motoSeleccionada.vin?.let { "VIN: $it" }
                                        ).joinToString(" • ")
                                        if (extraInfo.isNotEmpty()) {
                                            Text(text = extraInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    TextButton(onClick = onDeseleccionarMoto) {
                                        Text(stringResource(R.string.recepcion_btn_cambiar_moto))
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // ==========================================
                    // 3. SECCIÓN ORDEN DE SERVICIO
                    // ==========================================
                    Text(
                        text = stringResource(R.string.recepcion_seccion_servicio),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    // Chips de tipos de servicio
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ServiceType.values()) { tipo ->
                            val seleccionado = estadoUi.servicioSeleccionado == tipo
                            FilterChip(
                                selected = seleccionado,
                                onClick = { onServicioSeleccionado(tipo) },
                                label = { Text(stringResource(tipo.getLabelRes())) },
                                leadingIcon = if (seleccionado) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    // Falla reportada / Motivo de ingreso
                    OutlinedTextField(
                        value = estadoUi.fallaReportada,
                        onValueChange = onFallaCambio,
                        label = { Text(stringResource(R.string.recepcion_label_falla)) },
                        trailingIcon = {
                            IconButton(onClick = {
                                onFallaCambio(if (estadoUi.fallaReportada.isBlank()) "Cliente reporta ruido en motor y freno trasero bajo." else "${estadoUi.fallaReportada} Revisión general.")
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = stringResource(R.string.recepcion_desc_dictado), tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    // Evidencia fotográfica
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recepcion_seccion_evidencia, estadoUi.fotosEvidencia.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onAgregarFoto,
                            enabled = estadoUi.fotosEvidencia.size < 5
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.recepcion_desc_foto))
                        }
                    }

                    if (estadoUi.fotosEvidencia.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(estadoUi.fotosEvidencia) { foto ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(foto.substringAfterLast("/")) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                )
                            }
                        }
                    }

                    // Notas sobre daños previos
                    OutlinedTextField(
                        value = estadoUi.notasDanosPrevios,
                        onValueChange = onNotasDanosCambio,
                        label = { Text(stringResource(R.string.recepcion_label_danos)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Modalidad de Entrega
                    Text(
                        text = stringResource(R.string.recepcion_seccion_modalidad),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = estadoUi.modalidadEntrega == DeliveryType.TALLER,
                            onClick = { onModalidadCambio(DeliveryType.TALLER) },
                            label = { Text(stringResource(R.string.delivery_taller)) },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = estadoUi.modalidadEntrega == DeliveryType.DOMICILIO,
                            onClick = { onModalidadCambio(DeliveryType.DOMICILIO) },
                            label = { Text(stringResource(R.string.delivery_domicilio)) },
                            leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botón Principal para guardar la orden
                    Button(
                        onClick = onGuardarClic,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.recepcion_btn_guardar),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            } else {
                // --- PESTAÑA 1: MÓDULO DE ENTREGA EN TALLER ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Feedback de entrega exitosa
                    estadoUi.mensajeEntregaExitosa?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = msg,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = onLimpiarMensajeEntrega) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_cerrar))
                                }
                            }
                        }
                    }

                    // Campo de búsqueda rápida
                    OutlinedTextField(
                        value = estadoUi.textoBusquedaEntrega,
                        onValueChange = onBusquedaEntregaCambio,
                        placeholder = { Text(stringResource(R.string.recepcion_buscar_placeholder)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (estadoUi.textoBusquedaEntrega.isNotEmpty()) {
                                IconButton(onClick = { onBusquedaEntregaCambio("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.btn_limpiar))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filtros por modalidad
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = estadoUi.filtroModalidadEntrega == null,
                                onClick = { onFiltroModalidadEntregaCambio(null) },
                                label = { Text(stringResource(R.string.recepcion_filtro_todas, estadoUi.ordenesListasParaEntrega.size)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = estadoUi.filtroModalidadEntrega == DeliveryType.TALLER,
                                onClick = { onFiltroModalidadEntregaCambio(DeliveryType.TALLER) },
                                label = { Text(stringResource(R.string.delivery_taller)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = estadoUi.filtroModalidadEntrega == DeliveryType.DOMICILIO,
                                onClick = { onFiltroModalidadEntregaCambio(DeliveryType.DOMICILIO) },
                                label = { Text(stringResource(R.string.delivery_domicilio)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (estadoUi.ordenesFiltradasEntrega.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (estadoUi.textoBusquedaEntrega.isNotEmpty())
                                        stringResource(R.string.recepcion_vacio_busqueda, estadoUi.textoBusquedaEntrega)
                                    else
                                        stringResource(R.string.recepcion_vacio_titulo),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.recepcion_vacio_subtitulo),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(estadoUi.ordenesFiltradasEntrega, key = { it.order.id }) { ordenDetalle ->
                                TarjetaEntregaTaller(
                                    ordenDetalle = ordenDetalle,
                                    formatoMoneda = formatoMoneda,
                                    onEntregarClic = { onSolicitarEntrega(ordenDetalle) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // DIÁLOGOS Y MODALES
        // ==========================================

        // 1. DIÁLOGO: NUEVO CLIENTE
        if (estadoUi.mostrarDialogoNuevoCliente) {
            AlertDialog(
                onDismissRequest = onCerrarDialogoNuevoCliente,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
                title = { Text(stringResource(R.string.recepcion_dialog_nuevo_cliente_titulo), fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        estadoUi.errorNuevoCliente?.let { err ->
                            Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedTextField(
                            value = estadoUi.nuevoClienteNombre,
                            onValueChange = onNuevoClienteNombreCambio,
                            label = { Text(stringResource(R.string.recepcion_label_nombre)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = estadoUi.nuevoClienteTelefono,
                            onValueChange = onNuevoClienteTelefonoCambio,
                            label = { Text(stringResource(R.string.recepcion_label_telefono)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = estadoUi.nuevoClienteDireccion,
                            onValueChange = onNuevoClienteDireccionCambio,
                            label = { Text(stringResource(R.string.recepcion_label_direccion)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = onGuardarNuevoCliente) {
                        Text(stringResource(R.string.recepcion_dialog_nuevo_cliente_guardar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCerrarDialogoNuevoCliente) {
                        Text(stringResource(R.string.btn_cancelar))
                    }
                }
            )
        }

        // 2. DIÁLOGO: NUEVA MOTOCICLETA
        if (estadoUi.mostrarDialogoNuevaMoto) {
            val nombreCliente = estadoUi.clienteSeleccionado?.name ?: ""
            AlertDialog(
                onDismissRequest = onCerrarDialogoNuevaMoto,
                icon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
                title = {
                    Column {
                        Text(stringResource(R.string.recepcion_dialog_nueva_moto_titulo), fontWeight = FontWeight.Bold)
                        Text(
                            text = stringResource(R.string.recepcion_dialog_nueva_moto_subtitulo, nombreCliente),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        estadoUi.errorNuevaMoto?.let { err ->
                            Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = estadoUi.nuevaMotoMarca,
                                onValueChange = onNuevaMotoMarcaCambio,
                                label = { Text(stringResource(R.string.recepcion_label_marca)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = estadoUi.nuevaMotoModelo,
                                onValueChange = onNuevaMotoModeloCambio,
                                label = { Text(stringResource(R.string.recepcion_label_modelo)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = estadoUi.nuevaMotoColor,
                                onValueChange = onNuevaMotoColorCambio,
                                label = { Text(stringResource(R.string.recepcion_label_color)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = estadoUi.nuevaMotoPlaca,
                                onValueChange = onNuevaMotoPlacaCambio,
                                label = { Text(stringResource(R.string.recepcion_label_placa)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = estadoUi.nuevaMotoAnio,
                                onValueChange = onNuevaMotoAnioCambio,
                                label = { Text(stringResource(R.string.recepcion_label_anio)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = estadoUi.nuevaMotoVin,
                                onValueChange = onNuevaMotoVinCambio,
                                label = { Text(stringResource(R.string.recepcion_label_vin)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        // Botón de captura / simulación de foto
                        OutlinedButton(
                            onClick = {
                                onNuevaMotoFotoCambio("file://moto_${System.currentTimeMillis()}.jpg")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (estadoUi.nuevaMotoFotoUri.startsWith("file://"))
                                    stringResource(R.string.recepcion_foto_capturada)
                                else
                                    stringResource(R.string.recepcion_foto_tomar)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = onGuardarNuevaMoto) {
                        Text(stringResource(R.string.recepcion_dialog_nueva_moto_guardar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCerrarDialogoNuevaMoto) {
                        Text(stringResource(R.string.btn_cancelar))
                    }
                }
            )
        }

        // 3. DIÁLOGO: CONFIRMAR ENTREGA EN TALLER
        if (estadoUi.mostrarDialogoConfirmarEntrega && estadoUi.ordenAEntregar != null) {
            val orden = estadoUi.ordenAEntregar.order
            val totalCobro = orden.totalLaborCost + orden.totalPartsCost

            AlertDialog(
                onDismissRequest = onCancelarEntrega,
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
                title = { Text(stringResource(R.string.recepcion_dialog_entregar_titulo), fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Folio: ${orden.folio}")
                        Text("Cliente: ${estadoUi.ordenAEntregar.client.name}")
                        Text("Motocicleta: ${estadoUi.ordenAEntregar.motorcycle.brand} ${estadoUi.ordenAEntregar.motorcycle.model}")
                        HorizontalDivider()
                        Text(
                            text = "Monto a cobrar en mostrador: ${formatoMoneda.format(totalCobro)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = stringResource(R.string.recepcion_dialog_entregar_nota),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = onConfirmarEntrega) {
                        Text(stringResource(R.string.recepcion_dialog_entregar_btn))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCancelarEntrega) {
                        Text(stringResource(R.string.btn_cancelar))
                    }
                }
            )
        }

        // 4. DIÁLOGO: HISTORIAL DE SERVICIOS DE LA MOTO
        if (estadoUi.mostrarModalHistorial) {
            val moto = estadoUi.motoSeleccionada
            val marca = moto?.brand ?: ""
            val modelo = moto?.model ?: ""

            AlertDialog(
                onDismissRequest = onCerrarHistorialMoto,
                icon = { Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
                title = { Text(stringResource(R.string.recepcion_dialog_historial_titulo, marca, modelo), fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (estadoUi.historialMotoSeleccionada.isEmpty()) {
                            Text(
                                text = stringResource(R.string.recepcion_historial_sin_ordenes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            estadoUi.historialMotoSeleccionada.forEach { ordenDetalle ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(ordenDetalle.order.folio, fontWeight = FontWeight.Bold)
                                            Badge { Text(ordenDetalle.order.status) }
                                        }
                                        Text("Falla: ${ordenDetalle.order.reportedFailure}", style = MaterialTheme.typography.bodySmall)
                                        Text("Total: ${formatoMoneda.format(ordenDetalle.order.totalAmount)}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                        ordenDetalle.order.recommendations?.let {
                                            Text("Recs: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = onCerrarHistorialMoto) {
                        Text(stringResource(R.string.btn_cerrar))
                    }
                }
            )
        }

        // 5. ALERTA PREVENTIVA: CLIENTE SIN DOMICILIO
        if (estadoUi.mostrarAlertaSinDomicilio) {
            AlertDialog(
                onDismissRequest = onDescartarAlertaDomicilio,
                title = { Text(stringResource(R.string.recepcion_alerta_sin_domicilio_titulo)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.recepcion_alerta_sin_domicilio_mensaje))
                        OutlinedTextField(
                            value = estadoUi.direccionCliente,
                            onValueChange = onDireccionCambio,
                            label = { Text(stringResource(R.string.recepcion_alerta_sin_domicilio_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = onDescartarAlertaDomicilio) {
                        Text(stringResource(R.string.recepcion_alerta_sin_domicilio_btn_guardar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDescartarAlertaDomicilio) {
                        Text(stringResource(R.string.recepcion_alerta_sin_domicilio_btn_continuar))
                    }
                }
            )
        }
    }
}

@Composable
fun TarjetaEntregaTaller(
    ordenDetalle: WorkOrderWithDetails,
    formatoMoneda: NumberFormat,
    onEntregarClic: () -> Unit
) {
    val orden = ordenDetalle.order
    val cliente = ordenDetalle.client
    val moto = ordenDetalle.motorcycle
    val totalCobroTaller = orden.totalLaborCost + orden.totalPartsCost

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = orden.folio,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Badge(
                    containerColor = if (orden.deliveryType == DeliveryType.DOMICILIO.name)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = if (orden.deliveryType == DeliveryType.DOMICILIO.name)
                            stringResource(R.string.delivery_domicilio_en_taller)
                        else
                            stringResource(R.string.delivery_taller),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // Datos del Cliente
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                Text(text = cliente.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "• ${cliente.phone}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Datos de la Moto
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                val placaTxt = moto.plate?.let { " • Placa: $it" } ?: ""
                Text(text = "${moto.brand} ${moto.model} (${moto.color})$placaTxt", style = MaterialTheme.typography.bodyMedium)
            }

            // Servicio & Falla
            val serviceTypeEnum = runCatching { ServiceType.valueOf(orden.serviceType) }.getOrNull()
            val serviceLabel = if (serviceTypeEnum != null) stringResource(serviceTypeEnum.getLabelRes()) else orden.serviceType.replace("_", " ")
            Text(
                text = "Servicio: $serviceLabel | Motivo: ${orden.reportedFailure}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Recomendaciones técnicas del mecánico si existen
            if (!orden.recommendations.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.recepcion_recs_label),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = orden.recommendations,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Desglose de cobro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = stringResource(R.string.recepcion_card_labor, formatoMoneda.format(orden.totalLaborCost)), style = MaterialTheme.typography.bodySmall)
                    Text(text = stringResource(R.string.recepcion_card_parts, formatoMoneda.format(orden.totalPartsCost)), style = MaterialTheme.typography.bodySmall)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = stringResource(R.string.recepcion_card_total_cobrar), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = formatoMoneda.format(totalCobroTaller),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Botón de Cobro y Entrega
            Button(
                onClick = onEntregarClic,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.recepcion_card_btn_entregar), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(
    name = "Recepción Completa - Modo Oscuro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PantallaRecepcionPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        PantallaRecepcionContenido(
            estadoUi = RecepcionUiState(
                nombreCliente = "Kevin Haziel Elías",
                telefonoCliente = "4451234567",
                marcaMoto = "Hero",
                modeloMoto = "Ignitor 125",
                colorMoto = "Rojo",
                fotosEvidencia = listOf("foto1.jpg")
            ),
            onPestanaCambio = {},
            onNombreCambio = {},
            onTelefonoCambio = {},
            onDireccionCambio = {},
            onMarcaCambio = {},
            onModeloCambio = {},
            onColorCambio = {},
            onPlacaCambio = {},
            onAnioCambio = {},
            onVinCambio = {},
            onServicioSeleccionado = {},
            onFallaCambio = {},
            onAgregarFoto = {},
            onNotasDanosCambio = {},
            onModalidadCambio = {},
            onDescartarAlertaDomicilio = {},
            onGuardarClic = {},
            onBusquedaEntregaCambio = {},
            onFiltroModalidadEntregaCambio = {},
            onSolicitarEntrega = {},
            onCancelarEntrega = {},
            onConfirmarEntrega = {},
            onLimpiarMensajeEntrega = {}
        )
    }
}
