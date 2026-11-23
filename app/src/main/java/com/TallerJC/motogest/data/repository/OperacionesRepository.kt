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
import com.TallerJC.motogest.data.sync.SyncCooldownStore
import com.TallerJC.motogest.data.sync.SyncScheduler
import com.TallerJC.motogest.data.sync.SyncState
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OperacionesRepository(
    private val workOrderDao: WorkOrderDao,
    private val remoteDataSource: RemoteWorkOrderDataSource? = null,
    private val syncScheduler: SyncScheduler? = null,
    private val syncCooldownStore: SyncCooldownStore? = null,
    private val ahoraProvider: () -> Long = { System.currentTimeMillis() }
) : WorkOrderRepository {

    private val syncMutex = Mutex()

    val estadoSincronizacion: Flow<SyncState>
        get() = syncScheduler?.estadoSincronizacion ?: flowOf(SyncState.IDLE)

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
        syncScheduler?.programarSincronizacionCambios()
    }

    override suspend fun registrarCliente(cliente: ClientEntity) {
        workOrderDao.insertClient(cliente)
        syncScheduler?.programarSincronizacionCambios()
    }

    override suspend fun registrarMoto(moto: MotorcycleEntity) {
        workOrderDao.insertMotorcycle(moto)
        syncScheduler?.programarSincronizacionCambios()
    }

    override suspend fun cambiarEstadoOrden(ordenActual: WorkOrderEntity, nuevoEstado: OrderStatus) {
        val ahora = ahoraProvider()
        workOrderDao.updateOrderStatus(
            orderId = ordenActual.id,
            status = nuevoEstado.name,
            startedAt = if (nuevoEstado == OrderStatus.EN_PROCESO) ahora else null,
            completedAt = if (nuevoEstado == OrderStatus.LISTA_ENTREGA) ahora else null,
            deliveredAt = if (nuevoEstado == OrderStatus.ENTREGADA) ahora else null,
            updatedAt = ahora
        )
        syncScheduler?.programarSincronizacionCambios()
    }

    override suspend fun agregarManoDeObra(ordenActual: WorkOrderEntity, descripcion: String, costo: Double) {
        workOrderDao.addWorkItemAndUpdateTotals(
            WorkItemEntity(orderId = ordenActual.id, description = descripcion, cost = costo)
        )
        syncScheduler?.programarSincronizacionCambios()
    }

    override suspend fun agregarRefaccion(ordenActual: WorkOrderEntity, nombrePieza: String, costo: Double) {
        workOrderDao.addSparePartAndUpdateTotals(
            SparePartEntity(orderId = ordenActual.id, partName = nombrePieza, cost = costo)
        )
        syncScheduler?.programarSincronizacionCambios()
    }

    override suspend fun actualizarNotasYVinTaller(ordenActual: WorkOrderEntity, recomendaciones: String, nuevoVin: String?) {
        if (!nuevoVin.isNullOrBlank()) {
            workOrderDao.updateMotorcycleVin(ordenActual.motorcycleId, nuevoVin)
        }
        workOrderDao.updateRecommendations(ordenActual.id, recomendaciones)
        syncScheduler?.programarSincronizacionCambios()
    }

    override suspend fun actualizarCostoEnvio(ordenActual: WorkOrderEntity, nuevoCostoEnvio: Double) {
        val ahora = ahoraProvider()
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
        syncScheduler?.programarSincronizacionCambios()
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
        syncScheduler?.programarSincronizacionCambios()
    }

    override suspend fun eliminarOrden(orderId: String) {
        workOrderDao.deleteOrderWithPendingSync(orderId)
        syncScheduler?.programarSincronizacionCambios()
    }

    override suspend fun sincronizarConNube(): Boolean {
        val exito = sincronizarInterno(forzarPull = true)
        if (!exito && hayCambiosPendientes()) {
            syncScheduler?.programarSincronizacionCambios()
        }
        return exito
    }

    suspend fun sincronizarAutomaticamente(forzarPull: Boolean = false): Boolean {
        return sincronizarInterno(forzarPull = forzarPull)
    }

    // Ejecuta una sola sincronizacion a la vez para evitar colisiones entre worker y boton manual
    private suspend fun sincronizarInterno(forzarPull: Boolean): Boolean = syncMutex.withLock {
        val remote = remoteDataSource ?: return@withLock true

        return@withLock try {
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
                if (!subirYMarcarCliente(remote, cliente)) todoExitoso = false
            }

            for (moto in workOrderDao.getUnsyncedMotorcycles()) {
                if (!subirYMarcarMoto(remote, moto)) todoExitoso = false
            }

            for (orden in workOrderDao.getUnsyncedOrders()) {
                if (!subirYMarcarOrden(remote, orden)) todoExitoso = false
            }

            val ahora = ahoraProvider()
            val ultimoPull = syncCooldownStore?.obtenerUltimoPullMs() ?: 0L
            val debeHacerPull = forzarPull || (ahora - ultimoPull) >= PULL_COOLDOWN_MS

            if (debeHacerPull) {
                val pullExitoso = descargarDesdeNube(remote, idsPendientesEliminar)
                if (pullExitoso) {
                    syncCooldownStore?.guardarUltimoPullMs(ahora)
                } else {
                    todoExitoso = false
                }
            }

            todoExitoso
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun descargarDesdeNube(
        remote: RemoteWorkOrderDataSource,
        idsPendientesEliminar: Set<String>
    ): Boolean {
        var pullExitoso = true

        val clientesRemotos = remote.fetchClients().getOrElse {
            pullExitoso = false
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
            pullExitoso = false
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
            pullExitoso = false
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

        return pullExitoso
    }

    private suspend fun hayCambiosPendientes(): Boolean {
        return workOrderDao.getPendingDeletions().isNotEmpty() ||
            workOrderDao.getUnsyncedClients().isNotEmpty() ||
            workOrderDao.getUnsyncedMotorcycles().isNotEmpty() ||
            workOrderDao.getUnsyncedOrders().isNotEmpty()
    }

    private suspend fun subirYMarcarCliente(remote: RemoteWorkOrderDataSource, cliente: ClientEntity): Boolean {
        val subido = remote.uploadClient(cliente.toDto())
        if (subido) {
            workOrderDao.markClientAsSynced(cliente.id)
        }
        return subido
    }

    private suspend fun subirYMarcarMoto(remote: RemoteWorkOrderDataSource, moto: MotorcycleEntity): Boolean {
        val subido = remote.uploadMotorcycle(moto.toDto())
        if (subido) {
            workOrderDao.markMotorcycleAsSynced(moto.id)
        }
        return subido
    }

    private suspend fun subirYMarcarOrden(remote: RemoteWorkOrderDataSource, orden: WorkOrderEntity): Boolean {
        val subido = remote.uploadOrder(orden.toDto())
        if (subido) {
            workOrderDao.markOrderAsSynced(orden.id)
        }
        return subido
    }

    companion object {
        const val PULL_COOLDOWN_MS = 2 * 60_000L
    }
}
