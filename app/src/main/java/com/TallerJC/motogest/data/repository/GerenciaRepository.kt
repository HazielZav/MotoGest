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
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit

interface GerenciaRepository {
    fun obtenerOrdenesEntregadas(): Flow<List<WorkOrderWithDetails>>
}

class GerenciaRepositoryImpl(
    private val workOrderDao: WorkOrderDao? = null
) : GerenciaRepository {

    override fun obtenerOrdenesEntregadas(): Flow<List<WorkOrderWithDetails>> = flow {
        if (workOrderDao != null) {
            workOrderDao.getOrdersByStatusWithDetails(OrderStatus.ENTREGADA.name).collect { dbOrders ->
                if (dbOrders.isNotEmpty()) {
                    emit(dbOrders)
                } else {
                    emit(generarDatosSemilla())
                }
            }
        } else {
            // Datos operativos de inicio para demostración y entorno sin DB inicializada
            emit(generarDatosSemilla())
        }
    }

    private fun generarDatosSemilla(): List<WorkOrderWithDetails> {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - TimeUnit.HOURS.toMillis(2)
        val oneDayAgo = now - TimeUnit.DAYS.toMillis(1)
        val threeDaysAgo = now - TimeUnit.DAYS.toMillis(3)
        val fiveDaysAgo = now - TimeUnit.DAYS.toMillis(5)
        val tenDaysAgo = now - TimeUnit.DAYS.toMillis(10)
        val twentyDaysAgo = now - TimeUnit.DAYS.toMillis(20)

        // Cliente 1
        val cliente1 = ClientEntity(
            id = "c1",
            name = "Juan Carlos Pérez",
            phone = "4451234567",
            address = "Col. Centro, Calle Juárez 45"
        )
        val moto1 = MotorcycleEntity(
            id = "m1",
            clientId = "c1",
            brand = "Italika",
            model = "FT150 GTS",
            color = "Rojo/Negro",
            photoUrl = "",
            plate = "ABC-12"
        )
        val orden1 = WorkOrderEntity(
            id = "o1",
            folio = "ORD-001",
            clientId = "c1",
            motorcycleId = "m1",
            receivedByUserId = "u1",
            status = OrderStatus.ENTREGADA.name,
            serviceType = ServiceType.SERVICIO_GENERAL.name,
            reportedFailure = "Mantenimiento preventivo de los 6,000 km",
            deliveryType = DeliveryType.TALLER.name,
            totalLaborCost = 350.0,
            totalPartsCost = 450.0,
            totalAmount = 800.0,
            createdAt = oneHourAgo - TimeUnit.HOURS.toMillis(4),
            completedAt = oneHourAgo,
            deliveredAt = oneHourAgo
        )

        // Cliente 2
        val cliente2 = ClientEntity(
            id = "c2",
            name = "María Elena Rangel",
            phone = "4459876543",
            address = "Av. Hidalgo 102"
        )
        val moto2 = MotorcycleEntity(
            id = "m2",
            clientId = "c2",
            brand = "Yamaha",
            model = "FZ 2.0",
            color = "Azul Marino",
            photoUrl = "",
            plate = "XYZ-89"
        )
        val orden2 = WorkOrderEntity(
            id = "o2",
            folio = "ORD-002",
            clientId = "c2",
            motorcycleId = "m2",
            receivedByUserId = "u1",
            status = OrderStatus.ENTREGADA.name,
            serviceType = ServiceType.FRENOS.name,
            reportedFailure = "Balatas desgastadas y purga de líquido de frenos",
            deliveryType = DeliveryType.DOMICILIO.name,
            deliveryCost = 50.0,
            totalLaborCost = 200.0,
            totalPartsCost = 380.0,
            totalAmount = 630.0,
            createdAt = oneDayAgo - TimeUnit.HOURS.toMillis(3),
            completedAt = oneDayAgo,
            deliveredAt = oneDayAgo
        )

        // Cliente 3
        val cliente3 = ClientEntity(
            id = "c3",
            name = "Roberto Gómez",
            phone = "4455551212",
            address = "Fracc. Las Palmas 12"
        )
        val moto3 = MotorcycleEntity(
            id = "m3",
            clientId = "c3",
            brand = "Bajaj",
            model = "Pulsar NS200",
            color = "Negro",
            photoUrl = "",
            plate = "PUL-01"
        )
        val orden3 = WorkOrderEntity(
            id = "o3",
            folio = "ORD-003",
            clientId = "c3",
            motorcycleId = "m3",
            receivedByUserId = "u1",
            status = OrderStatus.ENTREGADA.name,
            serviceType = ServiceType.AJUSTE_MOTOR.name,
            reportedFailure = "Calibración de punterías y ajuste de cadena de distribución",
            deliveryType = DeliveryType.TALLER.name,
            totalLaborCost = 750.0,
            totalPartsCost = 600.0,
            totalAmount = 1350.0,
            createdAt = threeDaysAgo - TimeUnit.HOURS.toMillis(6),
            completedAt = threeDaysAgo,
            deliveredAt = threeDaysAgo
        )

        // Cliente 4
        val cliente4 = ClientEntity(
            id = "c4",
            name = "Luis Fernando Morales",
            phone = "4453338899",
            address = "Calle Morelos 304"
        )
        val moto4 = MotorcycleEntity(
            id = "m4",
            clientId = "c4",
            brand = "Honda",
            model = "XR 150L",
            color = "Blanco/Rojo",
            photoUrl = "",
            plate = "HND-77"
        )
        val orden4 = WorkOrderEntity(
            id = "o4",
            folio = "ORD-004",
            clientId = "c4",
            motorcycleId = "m4",
            receivedByUserId = "u1",
            status = OrderStatus.ENTREGADA.name,
            serviceType = ServiceType.REPARACION.name,
            reportedFailure = "Reparación de chicote de embrague y cambio de estator",
            deliveryType = DeliveryType.TALLER.name,
            totalLaborCost = 450.0,
            totalPartsCost = 850.0,
            totalAmount = 1300.0,
            createdAt = fiveDaysAgo - TimeUnit.HOURS.toMillis(5),
            completedAt = fiveDaysAgo,
            deliveredAt = fiveDaysAgo
        )

        // Cliente 5
        val cliente5 = ClientEntity(
            id = "c5",
            name = "Guillermo Mendoza",
            phone = "4457774411",
            address = "Calle Libertad 89"
        )
        val moto5 = MotorcycleEntity(
            id = "m5",
            clientId = "c5",
            brand = "Vento",
            model = "Nitrox 250",
            color = "Naranja",
            photoUrl = "",
            plate = "VNT-55"
        )
        val orden5 = WorkOrderEntity(
            id = "o5",
            folio = "ORD-005",
            clientId = "c5",
            motorcycleId = "m5",
            receivedByUserId = "u1",
            status = OrderStatus.ENTREGADA.name,
            serviceType = ServiceType.OTRO.name,
            reportedFailure = "Instalación de exploradoras LED y relevador",
            deliveryType = DeliveryType.TALLER.name,
            totalLaborCost = 300.0,
            totalPartsCost = 700.0,
            totalAmount = 1000.0,
            createdAt = tenDaysAgo - TimeUnit.HOURS.toMillis(2),
            completedAt = tenDaysAgo,
            deliveredAt = tenDaysAgo
        )

        // Cliente 6
        val cliente6 = ClientEntity(
            id = "c6",
            name = "Pedro Ramírez",
            phone = "4456667788",
            address = "Calle Zaragoza 512"
        )
        val moto6 = MotorcycleEntity(
            id = "m6",
            clientId = "c6",
            brand = "Suzuki",
            model = "Gixxer 150",
            color = "Negro Mate",
            photoUrl = "",
            plate = "SZK-23"
        )
        val orden6 = WorkOrderEntity(
            id = "o6",
            folio = "ORD-006",
            clientId = "c6",
            motorcycleId = "m6",
            receivedByUserId = "u1",
            status = OrderStatus.ENTREGADA.name,
            serviceType = ServiceType.SERVICIO_GENERAL.name,
            reportedFailure = "Afinación completa con aceite sintético",
            deliveryType = DeliveryType.DOMICILIO.name,
            deliveryCost = 60.0,
            totalLaborCost = 400.0,
            totalPartsCost = 520.0,
            totalAmount = 980.0,
            createdAt = twentyDaysAgo - TimeUnit.HOURS.toMillis(4),
            completedAt = twentyDaysAgo,
            deliveredAt = twentyDaysAgo
        )

        return listOf(
            WorkOrderWithDetails(
                order = orden1,
                client = cliente1,
                motorcycle = moto1,
                workItems = listOf(WorkItemEntity(orderId = "o1", description = "Servicio General", cost = 350.0)),
                spareParts = listOf(SparePartEntity(orderId = "o1", partName = "Aceite Motul + Filtro", cost = 450.0))
            ),
            WorkOrderWithDetails(
                order = orden2,
                client = cliente2,
                motorcycle = moto2,
                workItems = listOf(WorkItemEntity(orderId = "o2", description = "Cambio de Balatas", cost = 200.0)),
                spareParts = listOf(SparePartEntity(orderId = "o2", partName = "Juego Balatas Delanteras", cost = 380.0))
            ),
            WorkOrderWithDetails(
                order = orden3,
                client = cliente3,
                motorcycle = moto3,
                workItems = listOf(WorkItemEntity(orderId = "o3", description = "Ajuste de Motor", cost = 750.0)),
                spareParts = listOf(SparePartEntity(orderId = "o3", partName = "Cadena y Empaques", cost = 600.0))
            ),
            WorkOrderWithDetails(
                order = orden4,
                client = cliente4,
                motorcycle = moto4,
                workItems = listOf(WorkItemEntity(orderId = "o4", description = "Reparación Sistema Eléctrico", cost = 450.0)),
                spareParts = listOf(SparePartEntity(orderId = "o4", partName = "Estator y Chicote", cost = 850.0))
            ),
            WorkOrderWithDetails(
                order = orden5,
                client = cliente5,
                motorcycle = moto5,
                workItems = listOf(WorkItemEntity(orderId = "o5", description = "Instalación Eléctrica", cost = 300.0)),
                spareParts = listOf(SparePartEntity(orderId = "o5", partName = "Kit Exploradoras LED", cost = 700.0))
            ),
            WorkOrderWithDetails(
                order = orden6,
                client = cliente6,
                motorcycle = moto6,
                workItems = listOf(WorkItemEntity(orderId = "o6", description = "Afinación Mayor", cost = 400.0)),
                spareParts = listOf(SparePartEntity(orderId = "o6", partName = "Aceite y Bujía Iridium", cost = 520.0))
            )
        )
    }
}
