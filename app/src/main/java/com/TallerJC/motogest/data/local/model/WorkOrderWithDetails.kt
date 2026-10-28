package com.TallerJC.motogest.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.TallerJC.motogest.data.local.entity.ClientEntity
import com.TallerJC.motogest.data.local.entity.MotorcycleEntity
import com.TallerJC.motogest.data.local.entity.SparePartEntity
import com.TallerJC.motogest.data.local.entity.WorkItemEntity
import com.TallerJC.motogest.data.local.entity.WorkOrderEntity

data class WorkOrderWithDetails(
    @Embedded
    val order: WorkOrderEntity,

    @Relation(
        parentColumn = "clientId",
        entityColumn = "id"
    )
    val client: ClientEntity,

    @Relation(
        parentColumn = "motorcycleId",
        entityColumn = "id"
    )
    val motorcycle: MotorcycleEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val workItems: List<WorkItemEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val spareParts: List<SparePartEntity>
)
