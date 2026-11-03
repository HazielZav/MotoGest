package com.TallerJC.motogest.fake

import com.TallerJC.motogest.data.local.dao.WorkOrderDao
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.SparePartEntity
import com.TallerJC.motogest.data.local.entity.WorkItemEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeWorkOrderDao(
    initialOrders: List<WorkOrderWithDetails> = emptyList(),
    initialClients: List<ClientEntity> = emptyList(),
    initialMotorcycles: List<MotorcycleEntity> = emptyList()
) : WorkOrderDao {

    val ordersList = mutableListOf<WorkOrderWithDetails>().apply { addAll(initialOrders) }
    val clientsList = mutableListOf<ClientEntity>().apply { addAll(initialClients) }
    val motorcyclesList = mutableListOf<MotorcycleEntity>().apply { addAll(initialMotorcycles) }
    val workItemsList = mutableListOf<WorkItemEntity>()
    val sparePartsList = mutableListOf<SparePartEntity>()

    private val _ordersFlow = MutableStateFlow<List<WorkOrderWithDetails>>(ordersList.toList())
    private val _clientsFlow = MutableStateFlow<List<ClientEntity>>(clientsList.toList())
    private val _motorcyclesFlow = MutableStateFlow<List<MotorcycleEntity>>(motorcyclesList.toList())

    private fun emitState() {
        _ordersFlow.value = ordersList.toList()
        _clientsFlow.value = clientsList.toList()
        _motorcyclesFlow.value = motorcyclesList.toList()
    }

    override fun getAllOrdersWithDetails(): Flow<List<WorkOrderWithDetails>> = _ordersFlow.asStateFlow()

    override fun getOrdersByStatusWithDetails(status: String): Flow<List<WorkOrderWithDetails>> {
        return _ordersFlow.map { list -> list.filter { it.order.status == status } }
    }

    override suspend fun getOrderById(orderId: String): WorkOrderEntity? {
        return ordersList.find { it.order.id == orderId }?.order
    }

    override fun getAllClients(): Flow<List<ClientEntity>> = _clientsFlow.asStateFlow()

    override suspend fun getClientById(clientId: String): ClientEntity? {
        return clientsList.find { it.id == clientId }
    }

    override fun getMotorcyclesByClientId(clientId: String): Flow<List<MotorcycleEntity>> {
        return _motorcyclesFlow.map { list -> list.filter { it.clientId == clientId } }
    }

    override suspend fun getMotorcycleById(motoId: String): MotorcycleEntity? {
        return motorcyclesList.find { it.id == motoId }
    }

    override fun getOrdersByMotorcycleId(motorcycleId: String): Flow<List<WorkOrderWithDetails>> {
        return _ordersFlow.map { list -> list.filter { it.motorcycle.id == motorcycleId } }
    }

    override fun getOrdersByClientId(clientId: String): Flow<List<WorkOrderWithDetails>> {
        return _ordersFlow.map { list -> list.filter { it.client.id == clientId } }
    }

    override suspend fun insertOrder(order: WorkOrderEntity) {
        val index = ordersList.indexOfFirst { it.order.id == order.id }
        if (index != -1) {
            ordersList[index] = ordersList[index].copy(order = order)
        } else {
            val client = clientsList.find { it.id == order.clientId } ?: ClientEntity(id = order.clientId, name = "Client", phone = "000")
            val moto = motorcyclesList.find { it.id == order.motorcycleId } ?: MotorcycleEntity(id = order.motorcycleId, clientId = client.id, brand = "B", model = "M", color = "C", photoUrl = "")
            ordersList.add(WorkOrderWithDetails(order, client, moto, emptyList(), emptyList()))
        }
        emitState()
    }

    override suspend fun insertOrders(orders: List<WorkOrderEntity>) {
        orders.forEach { insertOrder(it) }
    }

    override suspend fun insertClient(client: ClientEntity) {
        val index = clientsList.indexOfFirst { it.id == client.id }
        if (index != -1) clientsList[index] = client else clientsList.add(client)
        emitState()
    }

    override suspend fun insertClients(clients: List<ClientEntity>) {
        clients.forEach { insertClient(it) }
    }

    override suspend fun insertMotorcycle(motorcycle: MotorcycleEntity) {
        val index = motorcyclesList.indexOfFirst { it.id == motorcycle.id }
        if (index != -1) motorcyclesList[index] = motorcycle else motorcyclesList.add(motorcycle)
        emitState()
    }

    override suspend fun insertMotorcycles(motorcycles: List<MotorcycleEntity>) {
        motorcycles.forEach { insertMotorcycle(it) }
    }

    override suspend fun insertWorkItem(item: WorkItemEntity) {
        workItemsList.add(item)
    }

    override suspend fun insertWorkItems(items: List<WorkItemEntity>) {
        workItemsList.addAll(items)
    }

    override suspend fun insertSparePart(part: SparePartEntity) {
        sparePartsList.add(part)
    }

    override suspend fun insertSpareParts(parts: List<SparePartEntity>) {
        sparePartsList.addAll(parts)
    }

    override suspend fun countOrders(): Int = ordersList.size

    override suspend fun updateMotorcycleVin(motoId: String, nuevoVin: String) {
        val index = motorcyclesList.indexOfFirst { it.id == motoId }
        if (index != -1) {
            motorcyclesList[index] = motorcyclesList[index].copy(vin = nuevoVin)
            emitState()
        }
    }

    override suspend fun getLaborTotal(orderId: String): Double {
        return workItemsList.filter { it.orderId == orderId }.sumOf { it.cost }
    }

    override suspend fun getPartsTotal(orderId: String): Double {
        return sparePartsList.filter { it.orderId == orderId }.sumOf { it.cost }
    }

    override suspend fun updateOrderFinancialTotals(orderId: String, labor: Double, parts: Double, updatedAt: Long) {
        val index = ordersList.indexOfFirst { it.order.id == orderId }
        if (index != -1) {
            val curOrder = ordersList[index].order
            val newTotal = labor + parts + curOrder.deliveryCost
            ordersList[index] = ordersList[index].copy(
                order = curOrder.copy(totalLaborCost = labor, totalPartsCost = parts, totalAmount = newTotal, updatedAt = updatedAt)
            )
            emitState()
        }
    }

    override suspend fun updateRecommendations(orderId: String, recommendations: String, updatedAt: Long) {
        val index = ordersList.indexOfFirst { it.order.id == orderId }
        if (index != -1) {
            val curOrder = ordersList[index].order
            ordersList[index] = ordersList[index].copy(
                order = curOrder.copy(recommendations = recommendations, updatedAt = updatedAt)
            )
            emitState()
        }
    }

    override suspend fun updateOrderStatus(
        orderId: String,
        status: String,
        startedAt: Long?,
        completedAt: Long?,
        deliveredAt: Long?,
        updatedAt: Long
    ) {
        val index = ordersList.indexOfFirst { it.order.id == orderId }
        if (index != -1) {
            val curOrder = ordersList[index].order
            ordersList[index] = ordersList[index].copy(
                order = curOrder.copy(
                    status = status,
                    startedAt = startedAt ?: curOrder.startedAt,
                    completedAt = completedAt ?: curOrder.completedAt,
                    deliveredAt = deliveredAt ?: curOrder.deliveredAt,
                    updatedAt = updatedAt
                )
            )
            emitState()
        }
    }

    override suspend fun updateClientDeliveryCost(clientId: String, cost: Double) {
        val index = clientsList.indexOfFirst { it.id == clientId }
        if (index != -1) {
            clientsList[index] = clientsList[index].copy(defaultDeliveryCost = cost)
            emitState()
        }
    }

    override suspend fun deleteOrderById(orderId: String) {
        ordersList.removeAll { it.order.id == orderId }
        emitState()
    }

    override suspend fun deleteClientById(clientId: String) {
        clientsList.removeAll { it.id == clientId }
        emitState()
    }

    override suspend fun deleteMotorcycleById(motoId: String) {
        motorcyclesList.removeAll { it.id == motoId }
        emitState()
    }

    override suspend fun getUnsyncedOrders(): List<WorkOrderEntity> {
        return ordersList.map { it.order }.filter { !it.isSynced }
    }

    override suspend fun markOrderAsSynced(orderId: String) {
        val index = ordersList.indexOfFirst { it.order.id == orderId }
        if (index != -1) {
            val curOrder = ordersList[index].order
            ordersList[index] = ordersList[index].copy(order = curOrder.copy(isSynced = true))
            emitState()
        }
    }

    override suspend fun getUnsyncedClients(): List<ClientEntity> {
        return clientsList.filter { !it.isSynced }
    }

    override suspend fun markClientAsSynced(clientId: String) {
        val index = clientsList.indexOfFirst { it.id == clientId }
        if (index != -1) {
            clientsList[index] = clientsList[index].copy(isSynced = true)
            emitState()
        }
    }

    override suspend fun getUnsyncedMotorcycles(): List<MotorcycleEntity> {
        return motorcyclesList.filter { !it.isSynced }
    }

    override suspend fun markMotorcycleAsSynced(motoId: String) {
        val index = motorcyclesList.indexOfFirst { it.id == motoId }
        if (index != -1) {
            motorcyclesList[index] = motorcyclesList[index].copy(isSynced = true)
            emitState()
        }
    }
}
