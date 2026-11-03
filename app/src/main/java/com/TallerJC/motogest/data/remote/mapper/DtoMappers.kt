package com.TallerJC.motogest.data.remote.mapper

import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity
import com.TallerJC.motogest.data.remote.dto.ClientDto
import com.TallerJC.motogest.data.remote.dto.MotorcycleDto
import com.TallerJC.motogest.data.remote.dto.WorkOrderDto

fun WorkOrderEntity.toDto() = WorkOrderDto(
    id = id,
    folio = folio,
    clientId = clientId,
    motorcycleId = motorcycleId,
    receivedByUserId = receivedByUserId,
    status = status,
    serviceType = serviceType,
    reportedFailure = reportedFailure,
    deliveryType = deliveryType,
    deliveryCost = deliveryCost,
    totalLaborCost = totalLaborCost,
    totalPartsCost = totalPartsCost,
    totalAmount = totalAmount,
    recommendations = recommendations,
    createdAt = createdAt,
    startedAt = startedAt,
    completedAt = completedAt,
    deliveredAt = deliveredAt,
    updatedAt = updatedAt
)

fun WorkOrderDto.toEntity() = WorkOrderEntity(
    id = id,
    folio = folio,
    clientId = clientId,
    motorcycleId = motorcycleId,
    receivedByUserId = receivedByUserId,
    status = status,
    serviceType = serviceType,
    reportedFailure = reportedFailure,
    deliveryType = deliveryType,
    deliveryCost = deliveryCost,
    totalLaborCost = totalLaborCost,
    totalPartsCost = totalPartsCost,
    totalAmount = totalAmount,
    recommendations = recommendations,
    createdAt = createdAt,
    startedAt = startedAt,
    completedAt = completedAt,
    deliveredAt = deliveredAt,
    updatedAt = updatedAt,
    isSynced = true
)

fun ClientEntity.toDto() = ClientDto(
    id = id,
    name = name,
    phone = phone,
    address = address,
    defaultDeliveryCost = defaultDeliveryCost,
    createdAt = createdAt
)

fun ClientDto.toEntity() = ClientEntity(
    id = id,
    name = name,
    phone = phone,
    address = address,
    defaultDeliveryCost = defaultDeliveryCost,
    createdAt = createdAt,
    isSynced = true
)

fun MotorcycleEntity.toDto() = MotorcycleDto(
    id = id,
    clientId = clientId,
    brand = brand,
    model = model,
    color = color,
    photoUrl = photoUrl,
    plate = plate,
    year = year,
    vin = vin,
    createdAt = createdAt
)

fun MotorcycleDto.toEntity() = MotorcycleEntity(
    id = id,
    clientId = clientId,
    brand = brand,
    model = model,
    color = color,
    photoUrl = photoUrl,
    plate = plate,
    year = year,
    vin = vin,
    createdAt = createdAt,
    isSynced = true
)
