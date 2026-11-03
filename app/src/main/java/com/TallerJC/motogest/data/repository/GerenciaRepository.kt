package com.TallerJC.motogest.data.repository

import com.TallerJC.motogest.data.local.dao.WorkOrderDao
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.SparePartEntity
import com.TallerJC.motogest.data.local.entity.WorkItemEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.util.concurrent.TimeUnit

interface GerenciaRepository {
    fun obtenerOrdenesEntregadas(): Flow<List<WorkOrderWithDetails>>
    fun obtenerTodasLasOrdenes(): Flow<List<WorkOrderWithDetails>>
    fun obtenerHistorialMoto(motorcycleId: String): Flow<List<WorkOrderWithDetails>>
    fun obtenerHistorialCliente(clientId: String): Flow<List<WorkOrderWithDetails>>
    suspend fun insertarDatosDemo()
}

class GerenciaRepositoryImpl(
    private val workOrderDao: WorkOrderDao
) : GerenciaRepository {

    override fun obtenerOrdenesEntregadas(): Flow<List<WorkOrderWithDetails>> {
        return workOrderDao.getOrdersByStatusWithDetails(OrderStatus.ENTREGADA.name)
    }

    override fun obtenerTodasLasOrdenes(): Flow<List<WorkOrderWithDetails>> {
        return workOrderDao.getAllOrdersWithDetails()
    }

    override fun obtenerHistorialMoto(motorcycleId: String): Flow<List<WorkOrderWithDetails>> {
        return workOrderDao.getOrdersByMotorcycleId(motorcycleId)
    }

    override fun obtenerHistorialCliente(clientId: String): Flow<List<WorkOrderWithDetails>> {
        return workOrderDao.getOrdersByClientId(clientId)
    }

    override suspend fun insertarDatosDemo() {
        val now = System.currentTimeMillis()
        // Aseguramos que haya órdenes en "HOY", "ESTA SEMANA" y "ESTE MES"
        val twoHoursAgo = now - TimeUnit.HOURS.toMillis(2)
        val fourHoursAgo = now - TimeUnit.HOURS.toMillis(4)
        val oneDayAgo = now - TimeUnit.DAYS.toMillis(1)
        val twoDaysAgo = now - TimeUnit.DAYS.toMillis(2)
        val threeDaysAgo = now - TimeUnit.DAYS.toMillis(3)
        val fourDaysAgo = now - TimeUnit.DAYS.toMillis(4)

        val idC1 = "demo_c1_${UUID.randomUUID().toString().take(6)}"
        val idM1 = "demo_m1_${UUID.randomUUID().toString().take(6)}"
        val idO1 = "demo_o1_${UUID.randomUUID().toString().take(6)}"

        val idC2 = "demo_c2_${UUID.randomUUID().toString().take(6)}"
        val idM2 = "demo_m2_${UUID.randomUUID().toString().take(6)}"
        val idO2 = "demo_o2_${UUID.randomUUID().toString().take(6)}"

        val idC3 = "demo_c3_${UUID.randomUUID().toString().take(6)}"
        val idM3 = "demo_m3_${UUID.randomUUID().toString().take(6)}"
        val idO3 = "demo_o3_${UUID.randomUUID().toString().take(6)}"

        val idC4 = "demo_c4_${UUID.randomUUID().toString().take(6)}"
        val idM4 = "demo_m4_${UUID.randomUUID().toString().take(6)}"
        val idO4 = "demo_o4_${UUID.randomUUID().toString().take(6)}"

        val idC5 = "demo_c5_${UUID.randomUUID().toString().take(6)}"
        val idM5 = "demo_m5_${UUID.randomUUID().toString().take(6)}"
        val idO5 = "demo_o5_${UUID.randomUUID().toString().take(6)}"

        val idC6 = "demo_c6_${UUID.randomUUID().toString().take(6)}"
        val idM6 = "demo_m6_${UUID.randomUUID().toString().take(6)}"
        val idO6 = "demo_o6_${UUID.randomUUID().toString().take(6)}"

        val clientes = listOf(
            ClientEntity(id = idC1, name = "Juan Carlos Pérez", phone = "4451234567", address = "Col. Centro, Calle Juárez 45"),
            ClientEntity(id = idC2, name = "María Elena Rangel", phone = "4459876543", address = "Av. Hidalgo 102"),
            ClientEntity(id = idC3, name = "Roberto Gómez", phone = "4455551212", address = "Fracc. Las Palmas 12"),
            ClientEntity(id = idC4, name = "Luis Fernando Morales", phone = "4453338899", address = "Calle Morelos 304"),
            ClientEntity(id = idC5, name = "Guillermo Mendoza", phone = "4457774411", address = "Calle Libertad 89"),
            ClientEntity(id = idC6, name = "Pedro Ramírez", phone = "4456667788", address = "Calle Zaragoza 512")
        )

        val motos = listOf(
            MotorcycleEntity(id = idM1, clientId = idC1, brand = "Italika", model = "FT150 GTS", color = "Rojo/Negro", photoUrl = "", plate = "ABC-12"),
            MotorcycleEntity(id = idM2, clientId = idC2, brand = "Yamaha", model = "FZ 2.0", color = "Azul Marino", photoUrl = "", plate = "XYZ-89"),
            MotorcycleEntity(id = idM3, clientId = idC3, brand = "Bajaj", model = "Pulsar NS200", color = "Negro", photoUrl = "", plate = "PUL-01"),
            MotorcycleEntity(id = idM4, clientId = idC4, brand = "Honda", model = "XR 150L", color = "Blanco/Rojo", photoUrl = "", plate = "HND-77"),
            MotorcycleEntity(id = idM5, clientId = idC5, brand = "Vento", model = "Nitrox 250", color = "Naranja", photoUrl = "", plate = "VNT-55"),
            MotorcycleEntity(id = idM6, clientId = idC6, brand = "Suzuki", model = "Gixxer 150", color = "Negro Mate", photoUrl = "", plate = "SZK-23")
        )

        val ordenes = listOf(
            WorkOrderEntity(
                id = idO1, folio = "ORD-001", clientId = idC1, motorcycleId = idM1, receivedByUserId = "u1",
                status = OrderStatus.ENTREGADA.name, serviceType = ServiceType.SERVICIO_GENERAL.name,
                reportedFailure = "Mantenimiento preventivo", deliveryType = DeliveryType.TALLER.name,
                totalLaborCost = 350.0, totalPartsCost = 450.0, totalAmount = 800.0,
                createdAt = twoHoursAgo - TimeUnit.HOURS.toMillis(4), completedAt = twoHoursAgo, deliveredAt = twoHoursAgo
            ),
            WorkOrderEntity(
                id = idO2, folio = "ORD-002", clientId = idC2, motorcycleId = idM2, receivedByUserId = "u1",
                status = OrderStatus.ENTREGADA.name, serviceType = ServiceType.FRENOS.name,
                reportedFailure = "Balatas desgastadas", deliveryType = DeliveryType.DOMICILIO.name,
                deliveryCost = 50.0, totalLaborCost = 200.0, totalPartsCost = 380.0, totalAmount = 630.0,
                createdAt = fourHoursAgo - TimeUnit.HOURS.toMillis(3), completedAt = fourHoursAgo, deliveredAt = fourHoursAgo
            ),
            WorkOrderEntity(
                id = idO3, folio = "ORD-003", clientId = idC3, motorcycleId = idM3, receivedByUserId = "u1",
                status = OrderStatus.ENTREGADA.name, serviceType = ServiceType.AJUSTE_MOTOR.name,
                reportedFailure = "Calibración de punterías", deliveryType = DeliveryType.TALLER.name,
                totalLaborCost = 750.0, totalPartsCost = 600.0, totalAmount = 1350.0,
                createdAt = oneDayAgo - TimeUnit.HOURS.toMillis(6), completedAt = oneDayAgo, deliveredAt = oneDayAgo
            ),
            WorkOrderEntity(
                id = idO4, folio = "ORD-004", clientId = idC4, motorcycleId = idM4, receivedByUserId = "u1",
                status = OrderStatus.ENTREGADA.name, serviceType = ServiceType.REPARACION.name,
                reportedFailure = "Reparación chicote de embrague", deliveryType = DeliveryType.TALLER.name,
                totalLaborCost = 450.0, totalPartsCost = 850.0, totalAmount = 1300.0,
                createdAt = twoDaysAgo - TimeUnit.HOURS.toMillis(5), completedAt = twoDaysAgo, deliveredAt = twoDaysAgo
            ),
            WorkOrderEntity(
                id = idO5, folio = "ORD-005", clientId = idC5, motorcycleId = idM5, receivedByUserId = "u1",
                status = OrderStatus.ENTREGADA.name, serviceType = ServiceType.OTRO.name,
                reportedFailure = "Instalación de exploradoras LED", deliveryType = DeliveryType.TALLER.name,
                totalLaborCost = 300.0, totalPartsCost = 700.0, totalAmount = 1000.0,
                createdAt = threeDaysAgo - TimeUnit.HOURS.toMillis(2), completedAt = threeDaysAgo, deliveredAt = threeDaysAgo
            ),
            WorkOrderEntity(
                id = idO6, folio = "ORD-006", clientId = idC6, motorcycleId = idM6, receivedByUserId = "u1",
                status = OrderStatus.ENTREGADA.name, serviceType = ServiceType.SERVICIO_GENERAL.name,
                reportedFailure = "Afinación completa", deliveryType = DeliveryType.DOMICILIO.name,
                deliveryCost = 60.0, totalLaborCost = 400.0, totalPartsCost = 520.0, totalAmount = 980.0,
                createdAt = fourDaysAgo - TimeUnit.HOURS.toMillis(4), completedAt = fourDaysAgo, deliveredAt = fourDaysAgo
            )
        )

        val workItems = listOf(
            WorkItemEntity(orderId = idO1, description = "Servicio General", cost = 350.0),
            WorkItemEntity(orderId = idO2, description = "Cambio de Balatas", cost = 200.0),
            WorkItemEntity(orderId = idO3, description = "Ajuste de Motor", cost = 750.0),
            WorkItemEntity(orderId = idO4, description = "Reparación Chicote", cost = 450.0),
            WorkItemEntity(orderId = idO5, description = "Instalación Eléctrica", cost = 300.0),
            WorkItemEntity(orderId = idO6, description = "Afinación Mayor", cost = 400.0)
        )

        val spareParts = listOf(
            SparePartEntity(orderId = idO1, partName = "Aceite Motul + Filtro", cost = 450.0),
            SparePartEntity(orderId = idO2, partName = "Juego Balatas Delanteras", cost = 380.0),
            SparePartEntity(orderId = idO3, partName = "Cadena y Empaques", cost = 600.0),
            SparePartEntity(orderId = idO4, partName = "Estator y Chicote", cost = 850.0),
            SparePartEntity(orderId = idO5, partName = "Kit Exploradoras LED", cost = 700.0),
            SparePartEntity(orderId = idO6, partName = "Aceite y Bujía Iridium", cost = 520.0)
        )

        workOrderDao.insertarLoteOrdenesCompletas(clientes, motos, ordenes, workItems, spareParts)
    }
}
