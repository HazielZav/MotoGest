package com.TallerJC.motogest.ui.gerencia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.TallerJC.motogest.R
import com.TallerJC.motogest.data.local.entity.UserEntity
import com.TallerJC.motogest.domain.model.enums.UserRole
import com.TallerJC.motogest.ui.util.getDescriptionRes
import com.TallerJC.motogest.ui.util.getLabelRes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeccionPersonalGerencia(
    estadoUi: GerenciaUiState,
    onBuscarEmpleadosCambio: (String) -> Unit,
    onFiltroRolCambio: (UserRole?) -> Unit,
    onAbrirNuevoEmpleado: () -> Unit,
    onAbrirEditarEmpleado: (UserEntity) -> Unit,
    onAbrirGestionRoles: (UserEntity) -> Unit,
    onToggleActivoEmpleado: (UserEntity) -> Unit,
    onSolicitarEliminarEmpleado: (UserEntity) -> Unit,
    onLimpiarMensajePersonal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalEmpleados = estadoUi.empleados.size
    val totalActivos = estadoUi.empleados.count { it.isActive }
    val totalInactivos = totalEmpleados - totalActivos

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Búsqueda
        OutlinedTextField(
            value = estadoUi.textoBusquedaEmpleados,
            onValueChange = onBuscarEmpleadosCambio,
            placeholder = {
                Text(
                    text = stringResource(R.string.gerencia_personal_buscar),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (estadoUi.textoBusquedaEmpleados.isNotEmpty()) {
                    IconButton(onClick = { onBuscarEmpleadosCambio("") }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.btn_limpiar))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Botón principal para agregar empleado
        Button(
            onClick = onAbrirNuevoEmpleado,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.gerencia_personal_btn_agregar), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Resumen de estado del personal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResumenChip(
                texto = stringResource(R.string.gerencia_personal_resumen_total, totalEmpleados),
                modifier = Modifier.weight(1f)
            )
            ResumenChip(
                texto = stringResource(R.string.gerencia_personal_resumen_activos, totalActivos),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            ResumenChip(
                texto = stringResource(R.string.gerencia_personal_resumen_inactivos, totalInactivos),
                modifier = Modifier.weight(1f)
            )
        }

        // Mensaje de retroalimentación
        estadoUi.mensajePersonal?.let { mensaje ->
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onLimpiarMensajePersonal, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.btn_cerrar),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filtros por Rol
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = estadoUi.filtroRolEmpleado == null,
                    onClick = { onFiltroRolCambio(null) },
                    label = { Text(stringResource(R.string.gerencia_personal_filtro_todos, totalEmpleados)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            items(UserRole.entries) { rol ->
                val cantidad = estadoUi.empleados.count { rol.name in it.roles }
                FilterChip(
                    selected = estadoUi.filtroRolEmpleado == rol,
                    onClick = {
                        onFiltroRolCambio(if (estadoUi.filtroRolEmpleado == rol) null else rol)
                    },
                    label = { Text("${stringResource(rol.getLabelRes())} ($cantidad)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (estadoUi.empleadosFiltrados.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (estadoUi.empleados.isEmpty())
                            stringResource(R.string.gerencia_personal_vacio_titulo)
                        else
                            stringResource(R.string.gerencia_personal_vacio_filtro),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (estadoUi.empleados.isEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.gerencia_personal_vacio_subtitulo),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(estadoUi.empleadosFiltrados, key = { it.id }) { empleado ->
                    TarjetaEmpleado(
                        empleado = empleado,
                        onEditar = { onAbrirEditarEmpleado(empleado) },
                        onGestionarRoles = { onAbrirGestionRoles(empleado) },
                        onToggleActivo = { onToggleActivoEmpleado(empleado) },
                        onEliminar = { onSolicitarEliminarEmpleado(empleado) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumenChip(
    texto: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = texto,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaEmpleado(
    empleado: UserEntity,
    onEditar: () -> Unit,
    onGestionarRoles: () -> Unit,
    onToggleActivo: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        onClick = onEditar,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (!empleado.isActive) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = empleado.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (empleado.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = empleado.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = if (empleado.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (empleado.isActive) R.string.gerencia_personal_estado_activo
                                else R.string.gerencia_personal_estado_inactivo
                            ),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (empleado.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Switch(
                        checked = empleado.isActive,
                        onCheckedChange = { onToggleActivo() }
                    )
                }
            }

            // Chips de roles asignados
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                empleado.roles.forEach { roleName ->
                    val roleEnum = runCatching { UserRole.valueOf(roleName) }.getOrNull()
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = roleEnum?.let { stringResource(it.getLabelRes()) } ?: roleName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Acciones del gerente sobre el empleado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onGestionarRoles) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.gerencia_personal_btn_roles), fontSize = 13.sp)
                }
                TextButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.gerencia_personal_btn_editar), fontSize = 13.sp)
                }
                TextButton(
                    onClick = onEliminar,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.gerencia_personal_btn_eliminar), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun DialogoFormularioEmpleado(
    estadoUi: GerenciaUiState,
    onNombreCambio: (String) -> Unit,
    onCorreoCambio: (String) -> Unit,
    onToggleRol: (UserRole) -> Unit,
    onActivoCambio: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    onCerrar: () -> Unit
) {
    val esEdicion = estadoUi.empleadoEnEdicion != null
    val scrollState = rememberScrollState()
    val scrollbarColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

    AlertDialog(
        onDismissRequest = onCerrar,
        icon = {
            Icon(
                if (esEdicion) Icons.Default.Edit else Icons.Default.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = stringResource(
                    if (esEdicion) R.string.gerencia_personal_dialog_editar_titulo
                    else R.string.gerencia_personal_dialog_nuevo_titulo
                ),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScrollbar(
                        scrollState = scrollState,
                        thumbColor = scrollbarColor,
                        trackColor = trackColor
                    )
                    .verticalScroll(scrollState)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = estadoUi.formEmpleadoNombre,
                    onValueChange = onNombreCambio,
                    label = { Text(stringResource(R.string.gerencia_personal_label_nombre)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estadoUi.formEmpleadoCorreo,
                    onValueChange = onCorreoCambio,
                    label = { Text(stringResource(R.string.gerencia_personal_label_correo)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = stringResource(R.string.gerencia_personal_label_roles),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    UserRole.entries.forEach { rol ->
                        val seleccionado = rol in estadoUi.formEmpleadoRoles
                        SelectorRolItem(
                            rol = rol,
                            seleccionado = seleccionado,
                            onToggle = { onToggleRol(rol) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.gerencia_personal_label_activo),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    Switch(
                        checked = estadoUi.formEmpleadoActivo,
                        onCheckedChange = onActivoCambio
                    )
                }

                estadoUi.errorFormularioEmpleado?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onGuardar) {
                Text(
                    stringResource(
                        if (esEdicion) R.string.gerencia_personal_btn_actualizar
                        else R.string.gerencia_personal_btn_guardar
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text(stringResource(R.string.btn_cancelar))
            }
        }
    )
}

@Composable
fun DialogoGestionRoles(
    empleado: UserEntity,
    rolesSeleccionados: Set<UserRole>,
    error: String?,
    onToggleRol: (UserRole) -> Unit,
    onGuardar: () -> Unit,
    onCerrar: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scrollbarColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

    AlertDialog(
        onDismissRequest = onCerrar,
        icon = {
            Icon(
                Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.gerencia_personal_dialog_roles_titulo),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScrollbar(
                        scrollState = scrollState,
                        thumbColor = scrollbarColor,
                        trackColor = trackColor
                    )
                    .verticalScroll(scrollState)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.gerencia_personal_dialog_roles_subtitulo, empleado.fullName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                UserRole.entries.forEach { rol ->
                    SelectorRolItem(
                        rol = rol,
                        seleccionado = rol in rolesSeleccionados,
                        onToggle = { onToggleRol(rol) }
                    )
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onGuardar) {
                Text(stringResource(R.string.gerencia_personal_btn_guardar_roles), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text(stringResource(R.string.btn_cancelar))
            }
        }
    )
}

@Composable
private fun SelectorRolItem(
    rol: UserRole,
    seleccionado: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(10.dp),
        color = if (seleccionado) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = if (seleccionado) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = seleccionado,
                onClick = onToggle
            )
            Column(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                Text(
                    text = stringResource(rol.getLabelRes()),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(rol.getDescriptionRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun Modifier.verticalScrollbar(
    scrollState: ScrollState,
    thumbColor: Color,
    trackColor: Color,
    width: Dp = 4.dp
): Modifier = drawWithContent {
    drawContent()
    val maxScroll = scrollState.maxValue
    if (maxScroll > 0) {
        val barWidthPx = width.toPx()
        val viewportHeight = size.height
        val totalHeight = viewportHeight + maxScroll
        val thumbHeight = (viewportHeight / totalHeight * viewportHeight)
            .coerceAtLeast(28.dp.toPx())
            .coerceAtMost(viewportHeight)
        val scrollFraction = scrollState.value.toFloat() / maxScroll.toFloat()
        val thumbOffsetY = scrollFraction * (viewportHeight - thumbHeight)
        val barX = size.width - barWidthPx
        val cornerRadius = CornerRadius(barWidthPx / 2f, barWidthPx / 2f)

        drawRoundRect(
            color = trackColor,
            topLeft = Offset(barX, 0f),
            size = Size(barWidthPx, viewportHeight),
            cornerRadius = cornerRadius
        )
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(barX, thumbOffsetY),
            size = Size(barWidthPx, thumbHeight),
            cornerRadius = cornerRadius
        )
    }
}
