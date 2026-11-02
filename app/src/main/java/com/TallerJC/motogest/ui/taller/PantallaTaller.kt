package com.TallerJC.motogest.ui.taller

import android.content.res.Configuration
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.TallerJC.motogest.ui.util.getLabelRes
import java.text.NumberFormat
import java.util.Locale

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
    val formatoMoneda = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")) }
    var ordenAEntregar by remember { mutableStateOf<WorkOrderWithDetails?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.taller_titulo), fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp)
        ) {
            // FILTROS DE ESTADO DEL TALLER
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(estadosDisponibles) { estado ->
                    FilterChip(
                        selected = estadoUi.filtroEstadoSeleccionado == estado,
                        onClick = { onFiltroCambio(estado) },
                        label = { Text(stringResource(estado.getLabelRes(plural = true))) },
                        modifier = Modifier.height(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                        Text("Folio: ${ordenDetalle.order.folio} • Cliente: ${ordenDetalle.client.name}")
                        Text("Moto: ${ordenDetalle.motorcycle.brand} ${ordenDetalle.motorcycle.model}")
                        Text("Total cobrado: ${formatoMoneda.format(totalCobro)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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

        // MODAL DE TRABAJO MECÁNICO, REFACCIONES Y VIN
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

@Composable
fun TarjetaOrdenMecanico(
    ordenDetalle: WorkOrderWithDetails,
    formatoMoneda: NumberFormat,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${moto.brand} ${moto.model} (${moto.color})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(
                        text = "${minutosTranscurridos} min",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Text(
                text = "${orden.folio} • Cliente: ${cliente.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val serviceTypeEnum = runCatching { ServiceType.valueOf(orden.serviceType) }.getOrNull()
            val serviceLabel = if (serviceTypeEnum != null) stringResource(serviceTypeEnum.getLabelRes()) else orden.serviceType.replace("_", " ")

            Text(
                text = "Servicio: $serviceLabel | Falla: ${orden.reportedFailure}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Acumulado: ${formatoMoneda.format(orden.totalAmount)} (M.O: ${formatoMoneda.format(orden.totalLaborCost)} | Piezas: ${formatoMoneda.format(orden.totalPartsCost)})",
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
    var conceptoTrabajo by remember { mutableStateOf("") }
    var costoTrabajo by remember { mutableStateOf("") }
    var nombrePieza by remember { mutableStateOf("") }
    var costoPieza by remember { mutableStateOf("") }
    var recomendaciones by remember { mutableStateOf(ordenDetalle.order.recommendations ?: "") }
    var vinMoto by remember { mutableStateOf(ordenDetalle.motorcycle.vin ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.taller_modal_titulo, ordenDetalle.motorcycle.brand, ordenDetalle.motorcycle.model),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

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
                    Icon(Icons.Default.Add, contentDescription = "Agregar Trabajo", tint = MaterialTheme.colorScheme.primary)
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
                    Icon(Icons.Default.Add, contentDescription = "Agregar Refacción", tint = MaterialTheme.colorScheme.primary)
                }
            }

            HorizontalDivider()

            // RECOMENDACIONES CON DICTADO POR VOZ
            OutlinedTextField(
                value = recomendaciones,
                onValueChange = { recomendaciones = it },
                label = { Text(stringResource(R.string.taller_modal_label_recs)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                trailingIcon = {
                    IconButton(onClick = { /* SpeechRecognizer */ }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Mic, contentDescription = "Dictar recomendaciones", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            Button(
                onClick = { onGuardarCambios(recomendaciones, vinMoto) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(stringResource(R.string.taller_modal_btn_guardar), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
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
