package com.TallerJC.motogest.ui.util

import androidx.annotation.StringRes
import com.TallerJC.motogest.R
import com.TallerJC.motogest.domain.model.enums.DeliveryType
import com.TallerJC.motogest.domain.model.enums.OrderStatus
import com.TallerJC.motogest.domain.model.enums.ServiceType
import com.TallerJC.motogest.ui.gerencia.PeriodoFiltro

@StringRes
fun OrderStatus.getLabelRes(plural: Boolean = false): Int = when (this) {
    OrderStatus.RECIBIDA -> if (plural) R.string.status_recibida_plural else R.string.status_recibida
    OrderStatus.EN_PROCESO -> R.string.status_en_proceso
    OrderStatus.ESPERANDO_REFACCIONES -> R.string.status_esperando_refacciones
    OrderStatus.EN_PRUEBAS -> R.string.status_en_pruebas
    OrderStatus.LISTA_ENTREGA -> if (plural) R.string.status_lista_entrega_plural else R.string.status_lista_entrega
    OrderStatus.EN_CAMINO -> R.string.status_en_camino
    OrderStatus.ENTREGADA -> if (plural) R.string.status_entregadas_plural else R.string.status_entregada
    OrderStatus.CANCELADA -> if (plural) R.string.status_canceladas_plural else R.string.status_cancelada
}

@StringRes
fun ServiceType.getLabelRes(): Int = when (this) {
    ServiceType.SERVICIO_GENERAL -> R.string.service_servicio_general
    ServiceType.FRENOS -> R.string.service_frenos
    ServiceType.REPARACION -> R.string.service_reparacion
    ServiceType.AJUSTE_MOTOR -> R.string.service_ajuste_motor
    ServiceType.OTRO -> R.string.service_otro
}

@StringRes
fun DeliveryType.getLabelRes(): Int = when (this) {
    DeliveryType.TALLER -> R.string.delivery_taller
    DeliveryType.DOMICILIO -> R.string.delivery_domicilio
}

@StringRes
fun PeriodoFiltro.getLabelRes(): Int = when (this) {
    PeriodoFiltro.HOY -> R.string.period_hoy
    PeriodoFiltro.ESTA_SEMANA -> R.string.period_esta_semana
    PeriodoFiltro.ESTE_MES -> R.string.period_este_mes
    PeriodoFiltro.PERSONALIZADO -> R.string.period_personalizado
}
