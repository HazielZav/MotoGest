package com.TallerJC.motogest.ui.taller

import android.content.ActivityNotFoundException
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.TallerJC.motogest.R
import com.TallerJC.motogest.data.local.entity.*
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.ui.components.MotoThumbnail
import com.TallerJC.motogest.ui.util.AdaptiveEmptyDetailPane
import com.TallerJC.motogest.ui.util.LocalAdaptiveState
import com.TallerJC.motogest.ui.util.MotoGestTopBar
import com.TallerJC.motogest.ui.util.SpeechRecognizerHelper
import com.TallerJC.motogest.ui.util.getLabelRes
import com.TallerJC.motogest.ui.util.rememberCurrencyFormatter
import java.text.NumberFormat

@Composable
fun PantallaTaller(
    viewModel: TallerViewModel
) {
    val estadoUi by viewModel.estadoUi.collectAsStateWithLifecycle()

    PantallaTallerContenido(
        estadoUi = estadoUi,
        estadosDisponibles = viewModel.estadosTaller,
        onFiltroCambio = viewModel::cambiarFiltroEstado,
        onCambiarEstadoOrden = viewModel::cambiarEstadoOrden,
        onEntregarEnTaller = viewModel::entregarOrdenEnTaller,
        onAbrirDetalleOrden = { viewModel.seleccionarOrdenParaDetalle(it.order.id) },
        onCerrarDetalleOrden = { viewModel.seleccionarOrdenParaDetalle(null) },
        onAgregarManoDeObra = viewModel::agregarTrabajoMecanico,
        onAgregarRefaccion = viewModel::agregarRefaccion,
        onGuardarRecomendacionesYVin = viewModel::guardarRecomendacionesYVin
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTallerContenido(
    estadoUi: TallerUiState,
    estadosDisponibles: List<OrderStatus>,
    onFiltroCambio: (OrderStatus) -> Unit,
    onCambiarEstadoOrden: (WorkOrderWithDetails, OrderStatus) -> Unit,
    onEntregarEnTaller: (WorkOrderWithDetails) -> Unit,
    onAbrirDetalleOrden: (WorkOrderWithDetails) -> Unit,
    onCerrarDetalleOrden: () -> Unit,
    onAgregarManoDeObra: (WorkOrderWithDetails, String, String) -> Unit,
    onAgregarRefaccion: (WorkOrderWithDetails, String, String) -> Unit,
    onGuardarRecomendacionesYVin: (WorkOrderWithDetails, String, String) -> Unit
) {
    val formatoMoneda = rememberCurrencyFormatter()
    val adaptive = LocalAdaptiveState.current
    var ordenAEntregar by remember { mutableStateOf<WorkOrderWithDetails?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            MotoGestTopBar(
                title = stringResource(R.string.taller_titulo),
                showBottomDivider = true
            )
        }
    ) { paddingValues ->
        val listaPanel: @Composable (Modifier) -> Unit = { panelModifier ->
            Column(modifier = panelModifier) {
                // FILTROS DE ESTADO DEL TALLER
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = if (adaptive.isCompactHeight) 4.dp else 8.dp)
                ) {
                    items(estadosDisponibles) { estado ->
                        FilterChip(
                            selected = estadoUi.filtroEstadoSeleccionado == estado,
                            onClick = { onFiltroCambio(estado) },
                            label = { Text(stringResource(estado.getLabelRes(plural = true))) },
                            modifier = Modifier.height(if (adaptive.isCompactHeight) 36.dp else 48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (adaptive.isCompactHeight) 4.dp else 8.dp))

                // LISTA REACTIVA DE MOTOCICLETAS
                if (estadoUi.ordenesFiltradas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val estadoNombre = stringResource(estadoUi.filtroEstadoSeleccionado.getLabelRes(plural = true))
                        Text(
                            text = stringResource(R.string.taller_vacio_estado, estadoNombre),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(
                            items = estadoUi.ordenesFiltradas,
                            key = { it.order.id }
                        ) { ordenDetalle ->
                            TarjetaOrdenMecanico(
                                ordenDetalle = ordenDetalle,
                                formatoMoneda = formatoMoneda,
                                isSelected = adaptive.isDualPane && estadoUi.ordenSeleccionadaDetalle?.order?.id == ordenDetalle.order.id,
                                onCambiarEstado = { nuevoEstado ->
                                    onCambiarEstadoOrden(ordenDetalle, nuevoEstado)
                                },
                                onEntregarEnTaller = { ordenAEntregar = ordenDetalle },
                                onAbrirDetalle = { onAbrirDetalleOrden(ordenDetalle) }
                            )
                        }
                    }
                }
            }
        }

        if (adaptive.isDualPane) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listaPanel(Modifier.weight(0.48f).fillMaxHeight())
                VerticalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Box(
                    modifier = Modifier
                        .weight(0.52f)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                ) {
                    val ordenSeleccionada = estadoUi.ordenSeleccionadaDetalle
                    if (ordenSeleccionada != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            ContenidoHojaTaller(
                                ordenDetalle = ordenSeleccionada,
                                formatoMoneda = formatoMoneda,
                                onCerrar = onCerrarDetalleOrden,
                                onAgregarManoObra = { concepto, costo ->
                                    onAgregarManoDeObra(ordenSeleccionada, concepto, costo)
                                },
                                onAgregarRefaccion = { pieza, costo ->
                                    onAgregarRefaccion(ordenSeleccionada, pieza, costo)
                                },
                                onGuardarCambios = { recomendaciones, vin ->
                                    onGuardarRecomendacionesYVin(ordenSeleccionada, recomendaciones, vin)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                        }
                    } else {
                        AdaptiveEmptyDetailPane(
                            icon = Icons.Default.Build,
                            title = stringResource(R.string.taller_panel_detalle_vacio_titulo),
                            subtitle = stringResource(R.string.taller_panel_detalle_vacio_subtitulo)
                        )
                    }
                }
            }
        } else {
            listaPanel(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            )
        }

        // CONFIRMACIÓN DE ENTREGA EN TALLER
        ordenAEntregar?.let { ordenDetalle ->
            val totalCobro = ordenDetalle.order.totalLaborCost + ordenDetalle.order.totalPartsCost
            AlertDialog(
                onDismissRequest = { ordenAEntregar = null },
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = { Text(stringResource(R.string.taller_dialog_entregar_titulo), fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.entregas_dialog_folio_cliente, ordenDetalle.order.folio, ordenDetalle.client.name))
                        Text(stringResource(R.string.label_moto, ordenDetalle.motorcycle.brand, ordenDetalle.motorcycle.model))
                        Text(
                            stringResource(R.string.taller_dialog_total_cobrado, formatoMoneda.format(totalCobro)),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.taller_dialog_entregar_nota),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        onEntregarEnTaller(ordenDetalle)
                        ordenAEntregar = null
                    }) {
                        Text(stringResource(R.string.taller_dialog_entregar_btn), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { ordenAEntregar = null }) {
                        Text(stringResource(R.string.btn_cancelar))
                    }
                }
            )
        }

        // MODAL DE TRABAJO MECÁNICO, REFACCIONES Y VIN (solo en modo Compact; en Medium/Expanded se muestra en panel derecho)
        if (!adaptive.isDualPane) {
            estadoUi.ordenSeleccionadaDetalle?.let { ordenSeleccionada ->
                ModalDetalleMecanico(
                    ordenDetalle = ordenSeleccionada,
                    formatoMoneda = formatoMoneda,
                    onDismiss = onCerrarDetalleOrden,
                    onAgregarManoObra = { concepto, costo ->
                        onAgregarManoDeObra(ordenSeleccionada, concepto, costo)
                    },
                    onAgregarRefaccion = { pieza, costo ->
                        onAgregarRefaccion(ordenSeleccionada, pieza, costo)
                    },
                    onGuardarCambios = { recomendaciones, vin ->
                        onGuardarRecomendacionesYVin(ordenSeleccionada, recomendaciones, vin)
                    }
                )
            }
        }
    }
}

@Composable
fun TarjetaOrdenMecanico(
    ordenDetalle: WorkOrderWithDetails,
    formatoMoneda: NumberFormat,
    isSelected: Boolean = false,
    onCambiarEstado: (OrderStatus) -> Unit,
    onEntregarEnTaller: () -> Unit,
    onAbrirDetalle: () -> Unit
) {
    val orden = ordenDetalle.order
    val moto = ordenDetalle.motorcycle
    val cliente = ordenDetalle.client
    val minutosTranscurridos = remember(orden.startedAt, orden.createdAt) {
        val baseTime = orden.startedAt ?: orden.createdAt
        ((System.currentTimeMillis() - baseTime) / 60000).coerceAtLeast(1)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MotoThumbnail(
                    photoUrl = moto.photoUrl,
                    size = 60.dp,
                    cornerRadius = 10.dp
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${moto.brand} ${moto.model} (${moto.color})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text(
                                text = stringResource(R.string.label_minutos, minutosTranscurridos),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.taller_card_folio_cliente, orden.folio, cliente.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val serviceTypeEnum = runCatching { ServiceType.valueOf(orden.serviceType) }.getOrNull()
            val serviceLabel = if (serviceTypeEnum != null) stringResource(serviceTypeEnum.getLabelRes()) else orden.serviceType.replace("_", " ")

            Text(
                text = stringResource(R.string.label_servicio_falla, serviceLabel, orden.reportedFailure),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(
                    R.string.taller_card_acumulado,
                    formatoMoneda.format(orden.totalAmount),
                    formatoMoneda.format(orden.totalLaborCost),
                    formatoMoneda.format(orden.totalPartsCost)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // BOTONES DE MÁQUINA DE ESTADOS FLEXIBLE
            when (orden.status) {
                OrderStatus.RECIBIDA.name -> {
                    Button(
                        onClick = { onCambiarEstado(OrderStatus.EN_PROCESO) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.taller_btn_comenzar), fontWeight = FontWeight.Bold)
                    }
                }
                OrderStatus.EN_PROCESO.name,
                OrderStatus.ESPERANDO_REFACCIONES.name,
                OrderStatus.EN_PRUEBAS.name -> {
                    // Botón para abrir hoja de captura de piezas/mano de obra
                    OutlinedButton(
                        onClick = onAbrirDetalle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.taller_btn_abrir_hoja))
                    }

                    // Estados intermedios opcionales con un toque
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (orden.status != OrderStatus.ESPERANDO_REFACCIONES.name) {
                            FilledTonalButton(
                                onClick = { onCambiarEstado(OrderStatus.ESPERANDO_REFACCIONES) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(stringResource(R.string.taller_btn_esp_piezas))
                            }
                        }
                        if (orden.status != OrderStatus.EN_PRUEBAS.name) {
                            FilledTonalButton(
                                onClick = { onCambiarEstado(OrderStatus.EN_PRUEBAS) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(stringResource(R.string.taller_btn_en_pruebas))
                            }
                        }
                    }

                    // Salto directo a Lista para entrega
                    Button(
                        onClick = { onCambiarEstado(OrderStatus.LISTA_ENTREGA) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.taller_btn_marcar_lista), fontWeight = FontWeight.Bold)
                    }
                }
                OrderStatus.LISTA_ENTREGA.name -> {
                    // MOTO LISTA: PERMITE ENTREGAR DIRECTAMENTE O VER DESTINO
                    if (orden.deliveryType == DeliveryType.TALLER.name) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = stringResource(R.string.taller_badge_recoger_mostrador),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Button(
                            onClick = onEntregarEnTaller,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.taller_btn_entregar_directo), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Text(
                                    text = stringResource(R.string.taller_badge_esperando_repartidor),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = onEntregarEnTaller,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Handshake, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.taller_btn_entregar_si_paso))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalDetalleMecanico(
    ordenDetalle: WorkOrderWithDetails,
    formatoMoneda: NumberFormat,
    onDismiss: () -> Unit,
    onAgregarManoObra: (String, String) -> Unit,
    onAgregarRefaccion: (String, String) -> Unit,
    onGuardarCambios: (String, String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        ContenidoHojaTaller(
            ordenDetalle = ordenDetalle,
            formatoMoneda = formatoMoneda,
            onCerrar = null,
            onAgregarManoObra = onAgregarManoObra,
            onAgregarRefaccion = onAgregarRefaccion,
            onGuardarCambios = onGuardarCambios,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun ContenidoHojaTaller(
    ordenDetalle: WorkOrderWithDetails,
    formatoMoneda: NumberFormat,
    onCerrar: (() -> Unit)?,
    onAgregarManoObra: (String, String) -> Unit,
    onAgregarRefaccion: (String, String) -> Unit,
    onGuardarCambios: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var conceptoTrabajo by rememberSaveable(ordenDetalle.order.id) { mutableStateOf("") }
    var costoTrabajo by rememberSaveable(ordenDetalle.order.id) { mutableStateOf("") }
    var nombrePieza by rememberSaveable(ordenDetalle.order.id) { mutableStateOf("") }
    var costoPieza by rememberSaveable(ordenDetalle.order.id) { mutableStateOf("") }
    var recomendaciones by rememberSaveable(ordenDetalle.order.id) {
        mutableStateOf(ordenDetalle.order.recommendations ?: "")
    }
    var vinMoto by rememberSaveable(ordenDetalle.order.id) {
        mutableStateOf(ordenDetalle.motorcycle.vin ?: "")
    }
    var mensajeDictado by rememberSaveable(ordenDetalle.order.id) { mutableStateOf<String?>(null) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val texto = SpeechRecognizerHelper.extractTranscribedText(result.resultCode, result.data)
        if (texto != null) {
            recomendaciones = SpeechRecognizerHelper.combineWithExistingText(recomendaciones, texto)
            mensajeDictado = null
        }
    }

    fun lanzarDictadoRecomendaciones() {
        val intent = SpeechRecognizerHelper.createSpeechIntent(
            context.getString(R.string.taller_dictado_prompt_recs)
        )
        try {
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            mensajeDictado = context.getString(R.string.recepcion_dictado_no_disponible)
        }
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MotoThumbnail(
                photoUrl = ordenDetalle.motorcycle.photoUrl,
                size = 52.dp,
                cornerRadius = 10.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.taller_modal_titulo, ordenDetalle.motorcycle.brand, ordenDetalle.motorcycle.model),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${ordenDetalle.order.folio} • ${ordenDetalle.motorcycle.color}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onCerrar != null) {
                IconButton(onClick = onCerrar) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_cerrar))
                }
            }
        }

            // EDICIÓN RÁPIDA DE VIN
            OutlinedTextField(
                value = vinMoto,
                onValueChange = { vinMoto = it },
                label = { Text(stringResource(R.string.taller_modal_vin_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            // MANO DE OBRA
            Text(stringResource(R.string.taller_modal_mano_obra_titulo), fontWeight = FontWeight.Bold)
            ordenDetalle.workItems.forEach { item ->
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("• ${item.description}")
                    Text(formatoMoneda.format(item.cost), fontWeight = FontWeight.SemiBold)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = conceptoTrabajo,
                    onValueChange = { conceptoTrabajo = it },
                    label = { Text(stringResource(R.string.taller_modal_label_concepto)) },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = costoTrabajo,
                    onValueChange = { costoTrabajo = it },
                    label = { Text(stringResource(R.string.taller_modal_label_costo_mxn)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        onAgregarManoObra(conceptoTrabajo, costoTrabajo)
                        conceptoTrabajo = ""
                        costoTrabajo = ""
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.taller_desc_agregar_trabajo), tint = MaterialTheme.colorScheme.primary)
                }
            }

            HorizontalDivider()

            // REFACCIONES
            Text(stringResource(R.string.taller_modal_refacciones_titulo), fontWeight = FontWeight.Bold)
            ordenDetalle.spareParts.forEach { part ->
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("• ${part.partName}")
                    Text(formatoMoneda.format(part.cost), fontWeight = FontWeight.SemiBold)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nombrePieza,
                    onValueChange = { nombrePieza = it },
                    label = { Text(stringResource(R.string.taller_modal_label_pieza)) },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = costoPieza,
                    onValueChange = { costoPieza = it },
                    label = { Text(stringResource(R.string.taller_modal_label_costo_mxn)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        onAgregarRefaccion(nombrePieza, costoPieza)
                        nombrePieza = ""
                        costoPieza = ""
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.taller_desc_agregar_refaccion), tint = MaterialTheme.colorScheme.primary)
                }
            }

            HorizontalDivider()

            // RECOMENDACIONES CON DICTADO POR VOZ
            mensajeDictado?.let { aviso ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.MicOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = aviso,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        IconButton(onClick = { mensajeDictado = null }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.btn_cerrar),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = recomendaciones,
                onValueChange = { recomendaciones = it },
                label = { Text(stringResource(R.string.taller_modal_label_recs)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                trailingIcon = {
                    IconButton(onClick = ::lanzarDictadoRecomendaciones, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = stringResource(R.string.taller_desc_dictado_recs),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            Button(
                onClick = {
                    if (conceptoTrabajo.isNotBlank() && costoTrabajo.isNotBlank()) {
                        onAgregarManoObra(conceptoTrabajo, costoTrabajo)
                        conceptoTrabajo = ""
                        costoTrabajo = ""
                    }
                    if (nombrePieza.isNotBlank() && costoPieza.isNotBlank()) {
                        onAgregarRefaccion(nombrePieza, costoPieza)
                        nombrePieza = ""
                        costoPieza = ""
                    }
                    onGuardarCambios(recomendaciones, vinMoto)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(stringResource(R.string.taller_modal_btn_guardar), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(
    name = "Tablero Taller - Modo Oscuro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PantallaTallerPreview() {
    val ordenMock = WorkOrderWithDetails(
        order = WorkOrderEntity(
            id = "1",
            folio = "ORD-2026-0089",
            clientId = "c1",
            motorcycleId = "m1",
            receivedByUserId = "u1",
            status = OrderStatus.EN_PROCESO.name,
            serviceType = ServiceType.SERVICIO_GENERAL.name,
            reportedFailure = "Ruido en punterías y cambio de aceite",
            deliveryType = DeliveryType.TALLER.name,
            totalLaborCost = 350.0,
            totalPartsCost = 180.0,
            totalAmount = 530.0
        ),
        client = ClientEntity(id = "c1", name = "Carlos Mendoza", phone = "4451002030"),
        motorcycle = MotorcycleEntity(
            id = "m1",
            clientId = "c1",
            brand = "Hero",
            model = "Ignitor 125",
            color = "Rojo",
            photoUrl = ""
        ),
        workItems = listOf(WorkItemEntity(orderId = "1", description = "Ajuste de punterías", cost = 350.0)),
        spareParts = listOf(SparePartEntity(orderId = "1", partName = "Aceite 20W50", cost = 180.0))
    )

    MaterialTheme(colorScheme = darkColorScheme()) {
        PantallaTallerContenido(
            estadoUi = TallerUiState(
                filtroEstadoSeleccionado = OrderStatus.EN_PROCESO,
                ordenesFiltradas = listOf(ordenMock)
            ),
            estadosDisponibles = listOf(
                OrderStatus.RECIBIDA,
                OrderStatus.EN_PROCESO,
                OrderStatus.ESPERANDO_REFACCIONES,
                OrderStatus.EN_PRUEBAS,
                OrderStatus.LISTA_ENTREGA
            ),
            onFiltroCambio = {},
            onCambiarEstadoOrden = { _, _ -> },
            onEntregarEnTaller = {},
            onAbrirDetalleOrden = {},
            onCerrarDetalleOrden = {},
            onAgregarManoDeObra = { _, _, _ -> },
            onAgregarRefaccion = { _, _, _ -> },
            onGuardarRecomendacionesYVin = { _, _, _ -> }
        )
    }
}
