package com.TallerJC.motogest.fake

import com.TallerJC.motogest.data.local.dao.MediaDao
import com.TallerJC.motogest.data.local.entity.MediaEntity
import com.TallerJC.motogest.domain.model.enums.MediaSyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMediaDao : MediaDao {
    val items = mutableListOf<MediaEntity>()
    private val itemsFlow = MutableStateFlow<List<MediaEntity>>(emptyList())

    private fun emit() {
        itemsFlow.value = items.toList()
    }

    override fun getMediaByOrderId(orderId: String): Flow<List<MediaEntity>> =
        itemsFlow.map { list -> list.filter { it.orderId == orderId }.sortedBy { it.createdAt } }

    override fun getMediaByMotorcycleId(motorcycleId: String): Flow<List<MediaEntity>> =
        itemsFlow.map { list -> list.filter { it.motorcycleId == motorcycleId }.sortedByDescending { it.createdAt } }

    override suspend fun getMediaById(id: String): MediaEntity? =
        items.firstOrNull { it.id == id }

    override suspend fun insertMedia(media: MediaEntity) {
        items.removeAll { it.id == media.id }
        items.add(media)
        emit()
    }

    override suspend fun insertMediaList(mediaList: List<MediaEntity>) {
        mediaList.forEach { media ->
            items.removeAll { it.id == media.id }
            items.add(media)
        }
        emit()
    }

    override suspend fun deleteMedia(id: String) {
        items.removeAll { it.id == id }
        emit()
    }

    override suspend fun deleteMediaByOrderId(orderId: String) {
        items.removeAll { it.orderId == orderId }
        emit()
    }

    override suspend fun getPendingSyncMedia(): List<MediaEntity> =
        items.filter {
            it.syncStatus == MediaSyncStatus.PENDING.name || it.syncStatus == MediaSyncStatus.FAILED.name
        }.sortedBy { it.createdAt }

    override fun observePendingSyncCount(): Flow<Int> =
        itemsFlow.map { list ->
            list.count {
                it.syncStatus == MediaSyncStatus.PENDING.name || it.syncStatus == MediaSyncStatus.FAILED.name
            }
        }

    override suspend fun updateSyncStatus(id: String, syncStatus: String, updatedAt: Long) {
        val idx = items.indexOfFirst { it.id == id }
        if (idx != -1) {
            items[idx] = items[idx].copy(syncStatus = syncStatus, updatedAt = updatedAt)
            emit()
        }
    }

    override suspend fun markAsSynced(id: String, remoteUrl: String, syncStatus: String, updatedAt: Long) {
        val idx = items.indexOfFirst { it.id == id }
        if (idx != -1) {
            items[idx] = items[idx].copy(
                syncStatus = syncStatus,
                remoteUrl = remoteUrl,
                updatedAt = updatedAt
            )
            emit()
        }
    }
}
