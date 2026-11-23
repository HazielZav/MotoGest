package com.TallerJC.motogest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_deletions")
data class PendingDeletionEntity(
    @PrimaryKey
    val entityId: String,
    val entityType: String = TYPE_WORK_ORDER,
    val deletedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_WORK_ORDER = "WORK_ORDER"
    }
}
