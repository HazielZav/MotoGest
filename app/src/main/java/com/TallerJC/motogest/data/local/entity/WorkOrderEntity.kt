package com.TallerJC.motogest.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "work_orders",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MotorcycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["motorcycleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["clientId"]),
        Index(value = ["motorcycleId"])
    ]
)
data class WorkOrderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val folio: String,
    val clientId: String,
    val motorcycleId: String,
    val receivedByUserId: String,
    val mechanicUserId: String? = null,
    val status: String,
    val serviceType: String,
    val reportedFailure: String,
    val intakeDamageNotes: String? = null,
    val intakePhotos: List<String> = emptyList(),
    val deliveryType: String,
    val deliveryCost: Double = 0.0,
    val totalLaborCost: Double = 0.0,
    val totalPartsCost: Double = 0.0,
    val totalAmount: Double = 0.0,
    val recommendations: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val deliveredAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
