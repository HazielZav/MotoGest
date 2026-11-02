package com.TallerJC.motogest.data.repository

import com.TallerJC.motogest.data.local.dao.WorkOrderDao
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.SparePartEntity
import com.TallerJC.motogest.data.local.entity.WorkItemEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.data.remote.datasource.RemoteWorkOrderDataSource
import com.TallerJC.motogest.data.remote.mapper.toDto
import com.TallerJC.motogest.data.remote.mapper.toEntity
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
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
        // Intentar subir a la nube en segundo plano si hay fuente remota
        remoteDataSource?.let { remote ->
            remote.uploadClient(cliente.toDto())
            remote.uploadMotorcycle(moto.toDto())
            if (remote.uploadOrder(orden.toDto())) {
                workOrderDao.markOrderAsSynced(orden.id)
                workOrderDao.markClientAsSynced(cliente.id)
                workOrderDao.markMotorcycleAsSynced(moto.id)
            }
        }
    }

    override suspend fun registrarCliente(cliente: ClientEntity) {
        workOrderDao.insertClient(cliente)
        remoteDataSource?.uploadClient(cliente.toDto())
    }

    override suspend fun registrarMoto(moto: MotorcycleEntity) {
        workOrderDao.insertMotorcycle(moto)
        remoteDataSource?.uploadMotorcycle(moto.toDto())
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
        val actualizada = workOrderDao.getOrderById(ordenActual.id)
        if (actualizada != null) {
            remoteDataSource?.uploadOrder(actualizada.toDto())
        }
    }

    override suspend fun agregarManoDeObra(ordenActual: WorkOrderEntity, descripcion: String, costo: Double) {
        workOrderDao.addWorkItemAndUpdateTotals(
            WorkItemEntity(orderId = ordenActual.id, description = descripcion, cost = costo)
        )
        val actualizada = workOrderDao.getOrderById(ordenActual.id)
        if (actualizada != null) {
            remoteDataSource?.uploadOrder(actualizada.toDto())
        }
    }

    override suspend fun agregarRefaccion(ordenActual: WorkOrderEntity, nombrePieza: String, costo: Double) {
        workOrderDao.addSparePartAndUpdateTotals(
            SparePartEntity(orderId = ordenActual.id, partName = nombrePieza, cost = costo)
        )
        val actualizada = workOrderDao.getOrderById(ordenActual.id)
        if (actualizada != null) {
            remoteDataSource?.uploadOrder(actualizada.toDto())
        }
    }

    override suspend fun actualizarNotasYVinTaller(ordenActual: WorkOrderEntity, recomendaciones: String, nuevoVin: String?) {
        if (!nuevoVin.isNullOrBlank()) {
            workOrderDao.updateMotorcycleVin(ordenActual.motorcycleId, nuevoVin)
        }
        workOrderDao.updateRecommendations(ordenActual.id, recomendaciones)
        val actualizada = workOrderDao.getOrderById(ordenActual.id)
        if (actualizada != null) {
            remoteDataSource?.uploadOrder(actualizada.toDto())
        }
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
        remoteDataSource?.uploadOrder(ordenActualizada.toDto())
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
        val actualizada = workOrderDao.getOrderById(ordenActual.id)
        if (actualizada != null) {
            remoteDataSource?.uploadOrder(actualizada.toDto())
        }
    }

    override suspend fun eliminarOrden(orderId: String) {
        workOrderDao.deleteOrderById(orderId)
        remoteDataSource?.deleteOrder(orderId)
    }

    override suspend fun sincronizarConNube(): Boolean {
        if (remoteDataSource == null) return true

        return try {
            // 1. Subir clientes no sincronizados
            val clientesPendientes = workOrderDao.getUnsyncedClients()
            for (cliente in clientesPendientes) {
                if (remoteDataSource.uploadClient(cliente.toDto())) {
                    workOrderDao.markClientAsSynced(cliente.id)
                }
            }

            // 2. Subir motos no sincronizadas
            val motosPendientes = workOrderDao.getUnsyncedMotorcycles()
            for (moto in motosPendientes) {
                if (remoteDataSource.uploadMotorcycle(moto.toDto())) {
                    workOrderDao.markMotorcycleAsSynced(moto.id)
                }
            }

            // 3. Subir órdenes no sincronizadas
            val ordenesPendientes = workOrderDao.getUnsyncedOrders()
            for (orden in ordenesPendientes) {
                if (remoteDataSource.uploadOrder(orden.toDto())) {
                    workOrderDao.markOrderAsSynced(orden.id)
                }
            }

            // 4. Descargar clientes remotos
            val clientesRemotos = remoteDataSource.fetchClients()
            if (clientesRemotos.isNotEmpty()) {
                workOrderDao.insertClients(clientesRemotos.map { it.toEntity() })
            }

            // 5. Descargar motos remotas
            val motosRemotas = remoteDataSource.fetchMotorcycles()
            if (motosRemotas.isNotEmpty()) {
                workOrderDao.insertMotorcycles(motosRemotas.map { it.toEntity() })
            }

            // 6. Descargar órdenes remotas
            val ordenesRemotas = remoteDataSource.fetchOrders()
            if (ordenesRemotas.isNotEmpty()) {
                workOrderDao.insertOrders(ordenesRemotas.map { it.toEntity() })
            }

            true
        } catch (_: Exception) {
            false
        }
    }
}
