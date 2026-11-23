package com.TallerJC.motogest.data.repository

import com.TallerJC.motogest.data.local.dao.WorkOrderDao
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.PendingDeletionEntity
import com.TallerJC.motogest.data.local.entity.SparePartEntity
import com.TallerJC.motogest.data.local.entity.WorkItemEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.remote.datasource.RemoteWorkOrderDataSource
import com.TallerJC.motogest.data.remote.mapper.toDto
import com.TallerJC.motogest.data.remote.mapper.toEntity
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class OperacionesRepository(
    private val workOrderDao: WorkOrderDao,
    private val remoteDataSource: RemoteWorkOrderDataSource? = null
) : WorkOrderRepository {

    override fun obtenerTodasLasOrdenes(): Flow<List<WorkOrderWithDetails>> {
        return workOrderDao.getAllOrdersWithDetails()
    }

    override fun obtenerOrdenesPorEstado(status: OrderStatus): Flow<List<WorkOrderWithDetails>> {
        return workOrderDao.getOrdersByStatusWithDetails(status.name)
    }

    override fun obtenerClientes(): Flow<List<ClientEntity>> {
        return workOrderDao.getAllClients()
    }

    override fun obtenerMotosDeCliente(clientId: String): Flow<List<MotorcycleEntity>> {
        return workOrderDao.getMotorcyclesByClientId(clientId)
    }

    override fun obtenerHistorialMoto(motorcycleId: String): Flow<List<WorkOrderWithDetails>> {
        return workOrderDao.getOrdersByMotorcycleId(motorcycleId)
    }

    override suspend fun registrarNuevaOrden(
        cliente: ClientEntity,
        moto: MotorcycleEntity,
        orden: WorkOrderEntity
    ) {
        workOrderDao.insertarRecepcionCompleta(cliente, moto, orden)
        if (!cliente.isSynced) subirYMarcarCliente(cliente)
        if (!moto.isSynced) subirYMarcarMoto(moto)
        subirYMarcarOrden(orden)
    }

    override suspend fun registrarCliente(cliente: ClientEntity) {
        workOrderDao.insertClient(cliente)
        subirYMarcarCliente(cliente)
    }

    override suspend fun registrarMoto(moto: MotorcycleEntity) {
        workOrderDao.insertMotorcycle(moto)
        subirYMarcarMoto(moto)
    }

    override suspend fun cambiarEstadoOrden(ordenActual: WorkOrderEntity, nuevoEstado: OrderStatus) {
        val ahora = System.currentTimeMillis()
        workOrderDao.updateOrderStatus(
            orderId = ordenActual.id,
            status = nuevoEstado.name,
            startedAt = if (nuevoEstado == OrderStatus.EN_PROCESO) ahora else null,
            completedAt = if (nuevoEstado == OrderStatus.LISTA_ENTREGA) ahora else null,
            deliveredAt = if (nuevoEstado == OrderStatus.ENTREGADA) ahora else null,
            updatedAt = ahora
        )
        workOrderDao.getOrderById(ordenActual.id)?.let { subirYMarcarOrden(it) }
    }

    override suspend fun agregarManoDeObra(ordenActual: WorkOrderEntity, descripcion: String, costo: Double) {
        workOrderDao.addWorkItemAndUpdateTotals(
            WorkItemEntity(orderId = ordenActual.id, description = descripcion, cost = costo)
        )
        workOrderDao.getOrderById(ordenActual.id)?.let { subirYMarcarOrden(it) }
    }

    override suspend fun agregarRefaccion(ordenActual: WorkOrderEntity, nombrePieza: String, costo: Double) {
        workOrderDao.addSparePartAndUpdateTotals(
            SparePartEntity(orderId = ordenActual.id, partName = nombrePieza, cost = costo)
        )
        workOrderDao.getOrderById(ordenActual.id)?.let { subirYMarcarOrden(it) }
    }

    override suspend fun actualizarNotasYVinTaller(ordenActual: WorkOrderEntity, recomendaciones: String, nuevoVin: String?) {
        if (!nuevoVin.isNullOrBlank()) {
            workOrderDao.updateMotorcycleVin(ordenActual.motorcycleId, nuevoVin)
            workOrderDao.getMotorcycleById(ordenActual.motorcycleId)?.let { subirYMarcarMoto(it) }
        }
        workOrderDao.updateRecommendations(ordenActual.id, recomendaciones)
        workOrderDao.getOrderById(ordenActual.id)?.let { subirYMarcarOrden(it) }
    }

    override suspend fun actualizarCostoEnvio(ordenActual: WorkOrderEntity, nuevoCostoEnvio: Double) {
        val ahora = System.currentTimeMillis()
        val labor = workOrderDao.getLaborTotal(ordenActual.id)
        val parts = workOrderDao.getPartsTotal(ordenActual.id)
        val nuevoTotal = labor + parts + nuevoCostoEnvio
        val ordenActualizada = ordenActual.copy(
            deliveryCost = nuevoCostoEnvio,
            totalLaborCost = labor,
            totalPartsCost = parts,
            totalAmount = nuevoTotal,
            updatedAt = ahora,
            isSynced = false
        )
        workOrderDao.insertOrder(ordenActualizada)
        subirYMarcarOrden(ordenActualizada)
    }

    override suspend fun marcarOrdenEntregada(
        ordenActual: WorkOrderEntity,
        clienteActual: ClientEntity,
        costoEnvioFinal: Double?,
        modalidadFinal: DeliveryType?
    ) {
        val modalidad = modalidadFinal?.name ?: ordenActual.deliveryType
        val costoEnvio = if (modalidad == DeliveryType.TALLER.name) 0.0 else (costoEnvioFinal ?: ordenActual.deliveryCost)
        workOrderDao.completeDelivery(
            orderId = ordenActual.id,
            clientId = clienteActual.id,
            deliveryType = modalidad,
            deliveryCost = costoEnvio
        )
        workOrderDao.getOrderById(ordenActual.id)?.let { subirYMarcarOrden(it) }
        if (modalidad == DeliveryType.DOMICILIO.name && costoEnvio > 0.0) {
            workOrderDao.getClientById(clienteActual.id)?.let { subirYMarcarCliente(it) }
        }
    }

    override suspend fun eliminarOrden(orderId: String) {
        workOrderDao.deleteOrderWithPendingSync(orderId)
        if (remoteDataSource?.deleteOrder(orderId) == true) {
            workOrderDao.deletePendingDeletion(orderId)
        }
    }

    // Sube primero los cambios locales pendientes y luego descarga los datos remotos sin pisar cambios sin subir
    override suspend fun sincronizarConNube(): Boolean {
        val remote = remoteDataSource ?: return true

        return try {
            var todoExitoso = true
            val idsPendientesEliminar = mutableSetOf<String>()

            for (eliminacion in workOrderDao.getPendingDeletions()) {
                if (eliminacion.entityType == PendingDeletionEntity.TYPE_WORK_ORDER) {
                    if (remote.deleteOrder(eliminacion.entityId)) {
                        workOrderDao.deletePendingDeletion(eliminacion.entityId)
                    } else {
                        idsPendientesEliminar.add(eliminacion.entityId)
                        todoExitoso = false
                    }
                }
            }

            for (cliente in workOrderDao.getUnsyncedClients()) {
                if (!subirYMarcarCliente(cliente)) todoExitoso = false
            }

            for (moto in workOrderDao.getUnsyncedMotorcycles()) {
                if (!subirYMarcarMoto(moto)) todoExitoso = false
            }

            for (orden in workOrderDao.getUnsyncedOrders()) {
                if (!subirYMarcarOrden(orden)) todoExitoso = false
            }

            val clientesRemotos = remote.fetchClients().getOrElse {
                todoExitoso = false
                emptyList()
            }
            val idsClientesPendientes = workOrderDao.getUnsyncedClients().map { it.id }.toSet()
            val clientesParaGuardar = clientesRemotos
                .filter { it.id !in idsClientesPendientes }
                .map { it.toEntity() }
            if (clientesParaGuardar.isNotEmpty()) {
                workOrderDao.insertClients(clientesParaGuardar)
            }

            val motosRemotas = remote.fetchMotorcycles().getOrElse {
                todoExitoso = false
                emptyList()
            }
            val idsMotosPendientes = workOrderDao.getUnsyncedMotorcycles().map { it.id }.toSet()
            val motosParaGuardar = motosRemotas
                .filter { it.id !in idsMotosPendientes && workOrderDao.getClientById(it.clientId) != null }
                .map { it.toEntity() }
            if (motosParaGuardar.isNotEmpty()) {
                workOrderDao.insertMotorcycles(motosParaGuardar)
            }

            val ordenesRemotas = remote.fetchOrders().getOrElse {
                todoExitoso = false
                emptyList()
            }
            val idsOrdenesPendientes = workOrderDao.getUnsyncedOrders().map { it.id }.toSet()
            val ordenesParaGuardar = ordenesRemotas
                .filter {
                    it.id !in idsOrdenesPendientes &&
                        it.id !in idsPendientesEliminar &&
                        workOrderDao.getClientById(it.clientId) != null &&
                        workOrderDao.getMotorcycleById(it.motorcycleId) != null
                }
                .map { dto -> dto.toEntity(workOrderDao.getOrderById(dto.id)) }
            if (ordenesParaGuardar.isNotEmpty()) {
                workOrderDao.insertOrders(ordenesParaGuardar)
            }

            todoExitoso
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun subirYMarcarCliente(cliente: ClientEntity): Boolean {
        val remote = remoteDataSource ?: return true
        val subido = remote.uploadClient(cliente.toDto())
        if (subido) {
            workOrderDao.markClientAsSynced(cliente.id)
        }
        return subido
    }

    private suspend fun subirYMarcarMoto(moto: MotorcycleEntity): Boolean {
        val remote = remoteDataSource ?: return true
        val subido = remote.uploadMotorcycle(moto.toDto())
        if (subido) {
            workOrderDao.markMotorcycleAsSynced(moto.id)
        }
        return subido
    }

    private suspend fun subirYMarcarOrden(orden: WorkOrderEntity): Boolean {
        val remote = remoteDataSource ?: return true
        val subido = remote.uploadOrder(orden.toDto())
        if (subido) {
            workOrderDao.markOrderAsSynced(orden.id)
        }
        return subido
    }
}
