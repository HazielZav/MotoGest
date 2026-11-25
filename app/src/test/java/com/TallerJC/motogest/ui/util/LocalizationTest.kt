package com.TallerJC.motogest.ui.util

import com.TallerJC.motogest.R
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.domain.model.enums.UserRole
import com.TallerJC.motogest.ui.gerencia.PeriodoFiltro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalizationTest {

    @Test
    fun testAllEnumsHaveValidStringResources() {
        OrderStatus.values().forEach { status ->
            assertTrue(status.getLabelRes(plural = false) != 0)
            assertTrue(status.getLabelRes(plural = true) != 0)
        }
        ServiceType.values().forEach { service ->
            assertTrue(service.getLabelRes() != 0)
        }
        DeliveryType.values().forEach { delivery ->
            assertTrue(delivery.getLabelRes() != 0)
        }
        PeriodoFiltro.values().forEach { periodo ->
            assertTrue(periodo.getLabelRes() != 0)
        }
        UserRole.values().forEach { role ->
            assertTrue(role.getLabelRes() != 0)
        }
    }

    @Test
    fun testUiTextStringResourceEquality() {
        val a = UiText.StringResource(R.string.entregas_exito_en_camino, "ORD-01")
        val b = UiText.StringResource(R.string.entregas_exito_en_camino, "ORD-01")
        val c = UiText.StringResource(R.string.entregas_exito_en_camino, "ORD-02")

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
    }
}
