package com.TallerJC.motogest.data.local

import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.SparePartEntity
import com.TallerJC.motogest.data.local.entity.UserEntity
import com.TallerJC.motogest.data.local.entity.WorkItemEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.local.model.WorkOrderWithDetails
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.domain.model.enums.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelEntitiesTest {

    @Test
    fun testDefaultValuesInEntities() {
        // Verificar que los UUID y valores por defecto se generan correctamente
        val client = ClientEntity(
            name = "Juan Perez",
            phone = "4451234567"
        )
        assertNotNull(client.id)
        assertTrue(client.id.isNotEmpty())
        assertEquals(0.0, client.defaultDeliveryCost, 0.001)
        assertFalse(client.isSynced)
        assertTrue(client.createdAt > 0)

        val user = UserEntity(
            id = "firebase_uid_123",
            fullName = "Carlos Mecanico",
            email = "carlos@tallerjc.com",
            roles = listOf(UserRole.MECANICO.name)
        )
        assertTrue(user.isActive)
        assertEquals(1, user.roles.size)
    }

    @Test
    fun testWorkOrderWithDetailsRelationStructure() {
        val client = ClientEntity(
            name = "Maria Lopez",
            phone = "4459876543",
            address = "Calle Hidalgo 123"
        )

        val motorcycle = MotorcycleEntity(
            clientId = client.id,
            brand = "Italika",
            model = "FT150",
            color = "Negro",
            photoUrl = "/storage/emulated/0/DCIM/moto1.jpg"
        )

        val order = WorkOrderEntity(
            folio = "ORD-001",
            clientId = client.id,
            motorcycleId = motorcycle.id,
            receivedByUserId = "usr_001",
            status = OrderStatus.RECIBIDA.name,
            serviceType = ServiceType.SERVICIO_GENERAL.name,
            reportedFailure = "Falla al encender en frío",
            deliveryType = DeliveryType.TALLER.name,
            totalLaborCost = 250.0,
            totalPartsCost = 150.0,
            totalAmount = 400.0
        )

        val workItem = WorkItemEntity(
            orderId = order.id,
            description = "Limpieza y ajuste de carburador",
            cost = 250.0
        )

        val sparePart = SparePartEntity(
            orderId = order.id,
            partName = "Bujía NGK",
            cost = 150.0
        )

        // Combinar en el POJO relacional
        val orderWithDetails = WorkOrderWithDetails(
            order = order,
            client = client,
            motorcycle = motorcycle,
            workItems = listOf(workItem),
            spareParts = listOf(sparePart)
        )

        assertEquals("ORD-001", orderWithDetails.order.folio)
        assertEquals("Maria Lopez", orderWithDetails.client.name)
        assertEquals("Italika", orderWithDetails.motorcycle.brand)
        assertEquals(1, orderWithDetails.workItems.size)
        assertEquals(1, orderWithDetails.spareParts.size)
        assertEquals(400.0, orderWithDetails.order.totalAmount, 0.001)
    }

    @Test
    fun testEnumsValues() {
        assertEquals("RECIBIDA", OrderStatus.RECIBIDA.name)
        assertEquals("EN_PROCESO", OrderStatus.EN_PROCESO.name)
        assertEquals("REPARACION", ServiceType.REPARACION.name)
        assertEquals("DOMICILIO", DeliveryType.DOMICILIO.name)
        assertEquals("RECEPCIONISTA", UserRole.RECEPCIONISTA.name)
    }
}
