package com.TallerJC.motogest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: WorkOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<WorkOrderEntity>)
}
