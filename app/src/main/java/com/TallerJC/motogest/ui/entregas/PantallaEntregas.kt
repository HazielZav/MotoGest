package com.TallerJC.motogest.ui.entregas

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PantallaEntregas(
    viewModel: EntregasViewModel
) {
    val estadoUi by viewModel.estadoUi.collectAsStateWithLifecycle()
    val context = LocalContext.current

    PantallaEntregasContenido(
        estadoUi = estadoUi,
        estadosDisponibles = viewModel.estadosEntrega,
        onFiltroCambio = viewModel::cambiarFiltroEstado,
        onMarcarEnCamino = viewModel::marcarEnCamino,
        onSolicitarEntrega = viewModel::solicitarConfirmacionEntrega,
        onAbrirEdicionCosto = viewModel::abrirDialogoEdicionCosto,
        onCostoCambio = viewModel::actualizarCostoEnvioTexto,
        onGuardarCosto = viewModel::guardarCostoEnvio,
        onConfirmarCostoCero = viewModel::confirmarCostoCero,
        onDescartarAlertaCostoCero = viewModel::descartarAlertaCostoCero,
        onCerrarEdicionCosto = viewModel::cerrarDialogoEdicionCosto,
        onConfirmarEntregaDirecta = viewModel::confirmarEntregaDirecta,
        onCerrarConfirmarEntrega = viewModel::cerrarDialogoConfirmarEntrega,
        onLimpiarMensajes = viewModel::limpiarMensajes,
        onNavegarEnMaps = {
            Toast.makeText(context, "Navegación en Maps próximamente", Toast.LENGTH_SHORT).show()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEntregasContenido(
    estadoUi: EntregasUiState,
    estadosDisponibles: List<OrderStatus>,
    onFiltroCambio: (OrderStatus?) -> Unit,
    onMarcarEnCamino: (WorkOrderWithDetails) -> Unit,
    onSolicitarEntrega: (WorkOrderWithDetails) -> Unit,
    onAbrirEdicionCosto: (WorkOrderWithDetails) -> Unit,
    onCostoCambio: (String) -> Unit,
    onGuardarCosto: () -> Unit,
    onConfirmarCostoCero: () -> Unit,
    onDescartarAlertaCostoCero: () -> Unit,
    onCerrarEdicionCosto: () -> Unit,
    onConfirmarEntregaDirecta: () -> Unit,
    onCerrarConfirmarEntrega: () -> Unit,
    onLimpiarMensajes: () -> Unit,
    onNavegarEnMaps: () -> Unit
) {
    val formatoMoneda = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Módulo de Entregas", fontWeight = FontWeight.Bold) },
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
            // Mensajes de Feedback (Éxito / Error)
            estadoUi.mensajeError?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onLimpiarMensajes, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            estadoUi.mensajeExito?.let { exito ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "✓ $exito",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onLimpiarMensajes, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Chips de filtrado rápido
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = estadoUi.filtroEstado == null,
                        onClick = { onFiltroCambio(null) },
                        label = { Text("Todas (${estadoUi.ordenesParaEntrega.size})") },
                        modifier = Modifier.height(48.dp)
                    )
                }
                items(estadosDisponibles) { estado ->
                    FilterChip(
                        selected = estadoUi.filtroEstado == estado,
                        onClick = { onFiltroCambio(estado) },
                        label = { Text(formatearEstadoEntrega(estado)) },
                        modifier = Modifier.height(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Contenido de la lista reactiva
            if (estadoUi.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (estadoUi.ordenesParaEntrega.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "No hay órdenes pendientes de entrega a domicilio.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(
                        items = estadoUi.ordenesParaEntrega,
                        key = { it.order.id }
                    ) { ordenDetalle ->
                        TarjetaOrdenEntrega(
                            ordenDetalle = ordenDetalle,
                            formatoMoneda = formatoMoneda,
                            onMarcarEnCamino = { onMarcarEnCamino(ordenDetalle) },
                            onSolicitarEntrega = { onSolicitarEntrega(ordenDetalle) },
                            onAbrirEdicionCosto = { onAbrirEdicionCosto(ordenDetalle) },
                            onNavegarEnMaps = onNavegarEnMaps
                        )
                    }
                }
            }
        }

        // --- DIÁLOGO DE EDICIÓN DE COSTO DE ENVÍO ---
        if (estadoUi.mostrarDialogoEdicionCosto && estadoUi.ordenSeleccionada != null) {
            val orden = estadoUi.ordenSeleccionada.order
            val cliente = estadoUi.ordenSeleccionada.client

            AlertDialog(
                onDismissRequest = onCerrarEdicionCosto,
                icon = {
                    Icon(
                        Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Costo de Envío a Domicilio",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Folio: ${orden.folio} • Cliente: ${cliente.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = estadoUi.costoEnvioEdicion,
                            onValueChange = onCostoCambio,
                            label = { Text("Monto de envío ($)*") },
                            placeholder = { Text("0.00") },
                            leadingIcon = {
                                Icon(Icons.Default.AttachMoney, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Nota: Al entregarse, este monto se guardará como costo habitual en la ficha del cliente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onGuardarCosto,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Guardar Costo")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onCerrarEdicionCosto,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // --- DIÁLOGO PREVENTIVO DE ALERTA: COSTO $0.00 ---
        if (estadoUi.mostrarAlertaCostoCero) {
            AlertDialog(
                onDismissRequest = onDescartarAlertaCostoCero,
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Confirmación de Envío Gratuito",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "El costo de envío es $0.00. ¿Está seguro de entregar el pedido de manera gratuita?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmarCostoCero,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Sí, continuar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onDescartarAlertaCostoCero,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Modificar costo")
                    }
                }
            )
        }

        // --- DIÁLOGO DE CONFIRMACIÓN DE ENTREGA ---
        if (estadoUi.mostrarDialogoConfirmarEntrega && estadoUi.ordenSeleccionada != null) {
            val orden = estadoUi.ordenSeleccionada.order
            val cliente = estadoUi.ordenSeleccionada.client

            AlertDialog(
                onDismissRequest = onCerrarConfirmarEntrega,
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "¿Confirmar entrega del pedido?",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Se marcará la orden ${orden.folio} como ENTREGADA y finalizará el ciclo de servicio."
                        )
                        Text(
                            text = "Costo de envío aplicado: ${formatoMoneda.format(orden.deliveryCost)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Total a cobrar al cliente: ${formatoMoneda.format(orden.totalAmount)}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "La tarifa habitual de ${cliente.name} se actualizará automáticamente a ${formatoMoneda.format(orden.deliveryCost)}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmarEntregaDirecta,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("ENTREGAR PEDIDO", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onCerrarConfirmarEntrega,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Volver")
                    }
                }
            )
        }
    }
}

@Composable
fun TarjetaOrdenEntrega(
    ordenDetalle: WorkOrderWithDetails,
    formatoMoneda: NumberFormat,
    onMarcarEnCamino: () -> Unit,
    onSolicitarEntrega: () -> Unit,
    onAbrirEdicionCosto: () -> Unit,
    onNavegarEnMaps: () -> Unit
) {
    val orden = ordenDetalle.order
    val moto = ordenDetalle.motorcycle
    val cliente = ordenDetalle.client

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Encabezado: Folio + Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = orden.folio,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Badge(
                    containerColor = if (orden.status == OrderStatus.EN_CAMINO.name) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                ) {
                    Text(
                        text = if (orden.status == OrderStatus.EN_CAMINO.name) "EN CAMINO" else "LISTA PARA ENTREGA",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Datos del Cliente (Nombre y Teléfono)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = cliente.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = cliente.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Dirección de Entrega a Domicilio
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = cliente.address?.ifBlank { null } ?: "Sin dirección registrada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (cliente.address.isNullOrBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }

            // Datos de la Moto
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${moto.brand} ${moto.model} • Color: ${moto.color}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Resumen Financiero y Costo de Envío
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Costo de Envío:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (orden.deliveryCost > 0.0) formatoMoneda.format(orden.deliveryCost) else "$0.00 (Gratis)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (orden.deliveryCost > 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total a Cobrar:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatoMoneda.format(orden.totalAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Fila de acciones secundarias: Ver en Maps y Editar Costo de Envío
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón "Ver en Maps" (Placeholder Toast)
                OutlinedButton(
                    onClick = onNavegarEnMaps,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Ver en Maps",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver en Maps", fontSize = 13.sp)
                }

                // Botón para editar costo de envío
                OutlinedButton(
                    onClick = onAbrirEdicionCosto,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Costo",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Editar Costo", fontSize = 13.sp)
                }
            }

            // Botón de Transición de Estados
            if (orden.status == OrderStatus.LISTA_ENTREGA.name) {
                Button(
                    onClick = onMarcarEnCamino,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INICIAR ENTREGA (EN CAMINO)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else if (orden.status == OrderStatus.EN_CAMINO.name) {
                Button(
                    onClick = onSolicitarEntrega,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MARCAR COMO ENTREGADA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private fun formatearEstadoEntrega(estado: OrderStatus): String {
    return when (estado) {
        OrderStatus.LISTA_ENTREGA -> "Listas para entrega"
        OrderStatus.EN_CAMINO -> "En camino"
        else -> estado.name.replace("_", " ")
    }
}

@Preview(
    name = "Entregas Completa - Modo Oscuro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PantallaEntregasPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        val muestra = WorkOrderWithDetails(
            order = WorkOrderEntity(
                id = "1",
                folio = "ORD-00101",
                clientId = "c1",
                motorcycleId = "m1",
                receivedByUserId = "u1",
                status = OrderStatus.LISTA_ENTREGA.name,
                serviceType = ServiceType.SERVICIO_GENERAL.name,
                reportedFailure = "Cambio de balatas",
                deliveryType = DeliveryType.DOMICILIO.name,
                deliveryCost = 50.0,
                totalLaborCost = 250.0,
                totalPartsCost = 150.0,
                totalAmount = 450.0
            ),
            client = ClientEntity(
                id = "c1",
                name = "Carlos Santana",
                phone = "4451234567",
                address = "Av. Insurgentes Sur 450, Col. Roma",
                defaultDeliveryCost = 50.0
            ),
            motorcycle = MotorcycleEntity(
                id = "m1",
                clientId = "c1",
                brand = "Yamaha",
                model = "FZ-25",
                color = "Azul Metálico",
                photoUrl = "local://moto.jpg"
            ),
            workItems = emptyList(),
            spareParts = emptyList()
        )

        PantallaEntregasContenido(
            estadoUi = EntregasUiState(
                ordenesParaEntrega = listOf(muestra),
                isLoading = false
            ),
            estadosDisponibles = listOf(OrderStatus.LISTA_ENTREGA, OrderStatus.EN_CAMINO),
            onFiltroCambio = {},
            onMarcarEnCamino = {},
            onSolicitarEntrega = {},
            onAbrirEdicionCosto = {},
            onCostoCambio = {},
            onGuardarCosto = {},
            onConfirmarCostoCero = {},
            onDescartarAlertaCostoCero = {},
            onCerrarEdicionCosto = {},
            onConfirmarEntregaDirecta = {},
            onCerrarConfirmarEntrega = {},
            onLimpiarMensajes = {},
            onNavegarEnMaps = {}
        )
    }
}
