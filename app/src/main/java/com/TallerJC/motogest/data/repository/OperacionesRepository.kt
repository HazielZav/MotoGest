package com.TallerJC.motogest.data.repository

import com.TallerJC.motogest.data.local.dao.WorkOrderDao
import com.TallerJC.motogest.data.local.entity.*
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import kotlinx.coroutines.flow.Flow

class OperacionesRepository(private val workOrderDao: WorkOrderDao) {

    fun obtenerTodasLasOrdenes(): Flow<List<WorkOrderWithDetails>> =
        workOrderDao.getAllOrdersWithDetails()

    fun obtenerOrdenesPorEstado(status: OrderStatus): Flow<List<WorkOrderWithDetails>> =
        workOrderDao.getOrdersByStatusWithDetails(status.name)

    suspend fun registrarNuevaOrden(
        cliente: ClientEntity,
        moto: MotorcycleEntity,
        orden: WorkOrderEntity
    ) {
        workOrderDao.insertarRecepcionCompleta(cliente, moto, orden)
    }

    suspend fun cambiarEstadoOrden(ordenActual: WorkOrderEntity, nuevoEstado: OrderStatus) {
        val ahora = System.currentTimeMillis()
        val ordenActualizada = ordenActual.copy(
            status = nuevoEstado.name,
            startedAt = if (nuevoEstado == OrderStatus.EN_PROCESO && ordenActual.startedAt == null) ahora else ordenActual.startedAt,
            completedAt = if (nuevoEstado == OrderStatus.LISTA_ENTREGA) ahora else ordenActual.completedAt,
            deliveredAt = if (nuevoEstado == OrderStatus.ENTREGADA) ahora else ordenActual.deliveredAt,
            updatedAt = ahora,
            isSynced = false
        )
        workOrderDao.insertOrder(ordenActualizada)
    }

    suspend fun agregarManoDeObra(ordenActual: WorkOrderEntity, descripcion: String, costo: Double) {
        val ahora = System.currentTimeMillis()
        workOrderDao.insertWorkItem(
            WorkItemEntity(orderId = ordenActual.id, description = descripcion, cost = costo)
        )
        val nuevoLabor = ordenActual.totalLaborCost + costo
        val nuevoTotal = nuevoLabor + ordenActual.totalPartsCost + ordenActual.deliveryCost
        workOrderDao.insertOrder(
            ordenActual.copy(totalLaborCost = nuevoLabor, totalAmount = nuevoTotal, updatedAt = ahora)
        )
    }

    suspend fun agregarRefaccion(ordenActual: WorkOrderEntity, nombrePieza: String, costo: Double) {
        val ahora = System.currentTimeMillis()
        workOrderDao.insertSparePart(
            SparePartEntity(orderId = ordenActual.id, partName = nombrePieza, cost = costo)
        )
        val nuevoParts = ordenActual.totalPartsCost + costo
        val nuevoTotal = ordenActual.totalLaborCost + nuevoParts + ordenActual.deliveryCost
        workOrderDao.insertOrder(
            ordenActual.copy(totalPartsCost = nuevoParts, totalAmount = nuevoTotal, updatedAt = ahora)
        )
    }

    suspend fun actualizarNotasYVinTaller(ordenActual: WorkOrderEntity, recomendaciones: String, nuevoVin: String?) {
        if (!nuevoVin.isNullOrBlank()) {
            workOrderDao.updateMotorcycleVin(ordenActual.motorcycleId, nuevoVin)
        }
        workOrderDao.insertOrder(
            ordenActual.copy(
                recommendations = recomendaciones,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun actualizarCostoEnvio(ordenActual: WorkOrderEntity, nuevoCostoEnvio: Double) {
        val ahora = System.currentTimeMillis()
        val nuevoTotal = ordenActual.totalLaborCost + ordenActual.totalPartsCost + nuevoCostoEnvio
        val ordenActualizada = ordenActual.copy(
            deliveryCost = nuevoCostoEnvio,
            totalAmount = nuevoTotal,
            updatedAt = ahora,
            isSynced = false
        )
        workOrderDao.insertOrder(ordenActualizada)
    }

    suspend fun marcarOrdenEntregada(
        ordenActual: WorkOrderEntity,
        clienteActual: ClientEntity,
        costoEnvioFinal: Double? = null
    ) {
        val ahora = System.currentTimeMillis()
        val costoEnvio = costoEnvioFinal ?: ordenActual.deliveryCost
        val nuevoTotal = ordenActual.totalLaborCost + ordenActual.totalPartsCost + costoEnvio

        val ordenActualizada = ordenActual.copy(
            status = OrderStatus.ENTREGADA.name,
            deliveryCost = costoEnvio,
            totalAmount = nuevoTotal,
            deliveredAt = ahora,
            updatedAt = ahora,
            isSynced = false
        )

        // Actualizar el costo de envío habitual en la ficha del cliente en Room
        val clienteActualizado = clienteActual.copy(
            defaultDeliveryCost = costoEnvio,
            isSynced = false
        )

        workOrderDao.actualizarEntregaYCliente(ordenActualizada, clienteActualizado)
    }
}
