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
}