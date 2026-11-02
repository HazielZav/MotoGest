package com.TallerJC.motogest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.SparePartEntity
import com.TallerJC.motogest.data.local.entity.WorkItemEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkOrderDao {

    @Transaction
    @Query("SELECT * FROM work_orders ORDER BY createdAt DESC")
    fun getAllOrdersWithDetails(): Flow<List<WorkOrderWithDetails>>

    @Transaction
    @Query("SELECT * FROM work_orders WHERE status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatusWithDetails(status: String): Flow<List<WorkOrderWithDetails>>

    @Query("SELECT * FROM work_orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): WorkOrderEntity?

    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :clientId")
    suspend fun getClientById(clientId: String): ClientEntity?

    @Query("SELECT * FROM motorcycles WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getMotorcyclesByClientId(clientId: String): Flow<List<MotorcycleEntity>>

    @Query("SELECT * FROM motorcycles WHERE id = :motoId")
    suspend fun getMotorcycleById(motoId: String): MotorcycleEntity?

    @Transaction
    @Query("SELECT * FROM work_orders WHERE motorcycleId = :motorcycleId ORDER BY createdAt DESC")
    fun getOrdersByMotorcycleId(motorcycleId: String): Flow<List<WorkOrderWithDetails>>

    @Transaction
    @Query("SELECT * FROM work_orders WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getOrdersByClientId(clientId: String): Flow<List<WorkOrderWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: WorkOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<WorkOrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotorcycle(motorcycle: MotorcycleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotorcycles(motorcycles: List<MotorcycleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkItem(item: WorkItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkItems(items: List<WorkItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSparePart(part: SparePartEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpareParts(parts: List<SparePartEntity>)

    @Query("SELECT COUNT(*) FROM work_orders")
    suspend fun countOrders(): Int

    @Query("DELETE FROM work_orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: String)

    @Query("DELETE FROM clients WHERE id = :clientId")
    suspend fun deleteClientById(clientId: String)

    @Query("DELETE FROM motorcycles WHERE id = :motoId")
    suspend fun deleteMotorcycleById(motoId: String)

    @Query("UPDATE motorcycles SET vin = :nuevoVin WHERE id = :motoId")
    suspend fun updateMotorcycleVin(motoId: String, nuevoVin: String)

    @Query("SELECT COALESCE(SUM(cost), 0.0) FROM work_items WHERE orderId = :orderId")
    suspend fun getLaborTotal(orderId: String): Double

    @Query("SELECT COALESCE(SUM(cost), 0.0) FROM spare_parts WHERE orderId = :orderId")
    suspend fun getPartsTotal(orderId: String): Double

    @Query("UPDATE work_orders SET totalLaborCost = :labor, totalPartsCost = :parts, totalAmount = :labor + :parts + deliveryCost, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderFinancialTotals(orderId: String, labor: Double, parts: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE work_orders SET recommendations = :recommendations, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateRecommendations(orderId: String, recommendations: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE work_orders SET status = :status, startedAt = CASE WHEN :startedAt IS NOT NULL AND startedAt IS NULL THEN :startedAt ELSE startedAt END, completedAt = CASE WHEN :completedAt IS NOT NULL THEN :completedAt ELSE completedAt END, deliveredAt = CASE WHEN :deliveredAt IS NOT NULL THEN :deliveredAt ELSE deliveredAt END, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(
        orderId: String,
        status: String,
        startedAt: Long? = null,
        completedAt: Long? = null,
        deliveredAt: Long? = null,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE clients SET defaultDeliveryCost = :cost WHERE id = :clientId")
    suspend fun updateClientDeliveryCost(clientId: String, cost: Double)

    // Sincronización
    @Query("SELECT * FROM work_orders WHERE isSynced = 0")
    suspend fun getUnsyncedOrders(): List<WorkOrderEntity>

    @Query("UPDATE work_orders SET isSynced = 1 WHERE id = :orderId")
    suspend fun markOrderAsSynced(orderId: String)

    @Query("SELECT * FROM clients WHERE isSynced = 0")
    suspend fun getUnsyncedClients(): List<ClientEntity>

    @Query("UPDATE clients SET isSynced = 1 WHERE id = :clientId")
    suspend fun markClientAsSynced(clientId: String)

    @Query("SELECT * FROM motorcycles WHERE isSynced = 0")
    suspend fun getUnsyncedMotorcycles(): List<MotorcycleEntity>

    @Query("UPDATE motorcycles SET isSynced = 1 WHERE id = :motoId")
    suspend fun markMotorcycleAsSynced(motoId: String)

    @Transaction
    suspend fun addWorkItemAndUpdateTotals(item: WorkItemEntity) {
        insertWorkItem(item)
        val labor = getLaborTotal(item.orderId)
        val parts = getPartsTotal(item.orderId)
        updateOrderFinancialTotals(item.orderId, labor, parts)
    }

    @Transaction
    suspend fun addSparePartAndUpdateTotals(part: SparePartEntity) {
        insertSparePart(part)
        val labor = getLaborTotal(part.orderId)
        val parts = getPartsTotal(part.orderId)
        updateOrderFinancialTotals(part.orderId, labor, parts)
    }

    @Transaction
    suspend fun completeDelivery(
        orderId: String,
        clientId: String,
        deliveryType: String,
        deliveryCost: Double,
        deliveredAt: Long = System.currentTimeMillis()
    ) {
        val labor = getLaborTotal(orderId)
        val parts = getPartsTotal(orderId)
        val total = labor + parts + deliveryCost

        val order = getOrderById(orderId)
        if (order != null) {
            val updatedOrder = order.copy(
                status = OrderStatus.ENTREGADA.name,
                deliveryType = deliveryType,
                deliveryCost = deliveryCost,
                totalLaborCost = labor,
                totalPartsCost = parts,
                totalAmount = total,
                deliveredAt = deliveredAt,
                updatedAt = deliveredAt,
                isSynced = false
            )
            insertOrder(updatedOrder)
        }

        if (deliveryType == DeliveryType.DOMICILIO.name && deliveryCost > 0.0) {
            updateClientDeliveryCost(clientId, deliveryCost)
        }
    }

    // Evita el problema por llave foránea
    @Transaction
    suspend fun insertarRecepcionCompleta(
        cliente: ClientEntity,
        moto: MotorcycleEntity,
        orden: WorkOrderEntity
    ) {
        insertClient(cliente)
        insertMotorcycle(moto)
        insertOrder(orden)
    }

    @Transaction
    suspend fun insertarLoteOrdenesCompletas(
        clientes: List<ClientEntity>,
        motos: List<MotorcycleEntity>,
        ordenes: List<WorkOrderEntity>,
        items: List<WorkItemEntity>,
        piezas: List<SparePartEntity>
    ) {
        insertClients(clientes)
        insertMotorcycles(motos)
        insertOrders(ordenes)
        insertWorkItems(items)
        insertSpareParts(piezas)
    }

    @Transaction
    suspend fun actualizarEntregaYCliente(
        orden: WorkOrderEntity,
        cliente: ClientEntity
    ) {
        insertOrder(orden)
        insertClient(cliente)
    }
}
