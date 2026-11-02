package com.TallerJC.motogest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderId: String? = null,
    val title: String,
    val message: String,
    val type: String = "INFO",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isSynced: Boolean = false
)
