package com.TallerJC.motogest.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "media_records",
    foreignKeys = [
        ForeignKey(
            entity = WorkOrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["orderId"])
    ]
)
data class MediaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderId: String,
    val uri: String,
    val mediaType: String = "IMAGE",
    val tag: String = "EVIDENCE",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
