package com.TallerJC.motogest.data.repository

import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import kotlinx.coroutines.flow.Flow

interface WorkOrderRepository {
    fun obtenerOrdenesPorEstado(estado: String): Flow<List<WorkOrderWithDetails>>

    suspend fun guardarOrdenCompleta(
        cliente: ClientEntity,
        moto: MotorcycleEntity,
        orden: WorkOrderEntity
    )

    suspend fun actualizarEstadoOrden(ordenId: String, nuevoEstado: String)
}