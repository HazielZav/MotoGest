package com.TallerJC.motogest.data.repository

import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import kotlinx.coroutines.flow.Flow

interface WorkOrderRepository {
    fun obtenerTodasLasOrdenes(): Flow<List<WorkOrderWithDetails>>

    fun obtenerOrdenesPorEstado(status: OrderStatus): Flow<List<WorkOrderWithDetails>>

    fun obtenerClientes(): Flow<List<ClientEntity>>

    fun obtenerMotosDeCliente(clientId: String): Flow<List<MotorcycleEntity>>

    fun obtenerHistorialMoto(motorcycleId: String): Flow<List<WorkOrderWithDetails>>

    suspend fun registrarNuevaOrden(
        cliente: ClientEntity,
        moto: MotorcycleEntity,
        orden: WorkOrderEntity
    )

    suspend fun registrarCliente(cliente: ClientEntity)

    suspend fun registrarMoto(moto: MotorcycleEntity)

    suspend fun cambiarEstadoOrden(ordenActual: WorkOrderEntity, nuevoEstado: OrderStatus)

    suspend fun agregarManoDeObra(ordenActual: WorkOrderEntity, descripcion: String, costo: Double)

    suspend fun agregarRefaccion(ordenActual: WorkOrderEntity, nombrePieza: String, costo: Double)

    suspend fun actualizarNotasYVinTaller(ordenActual: WorkOrderEntity, recomendaciones: String, nuevoVin: String?)

    suspend fun actualizarCostoEnvio(ordenActual: WorkOrderEntity, nuevoCostoEnvio: Double)

    suspend fun marcarOrdenEntregada(
        ordenActual: WorkOrderEntity,
        clienteActual: ClientEntity,
        costoEnvioFinal: Double? = null,
        modalidadFinal: DeliveryType? = null
    )

    suspend fun eliminarOrden(orderId: String)

    suspend fun sincronizarConNube(): Boolean
}
