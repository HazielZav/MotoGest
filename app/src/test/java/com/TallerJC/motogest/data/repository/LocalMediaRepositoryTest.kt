package com.TallerJC.motogest.data.repository

import android.net.Uri
import com.TallerJC.motogest.data.local.entity.MediaEntity
import com.TallerJC.motogest.data.local.storage.LocalImageStorage
import com.TallerJC.motogest.data.local.storage.StoredImageFile
import com.TallerJC.motogest.domain.model.enums.MediaSyncStatus
import com.TallerJC.motogest.domain.model.enums.MediaTag
import com.TallerJC.motogest.fake.FakeMediaDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocalMediaRepositoryTest {

    private lateinit var fakeDao: FakeMediaDao
    private lateinit var fakeStorage: TrackingFakeImageStorage
    private lateinit var repository: LocalMediaRepository

    private class TrackingFakeImageStorage : LocalImageStorage {
        val deletedPaths = mutableListOf<String>()

        override fun createCameraImageDestination(prefix: String): StoredImageFile {
            return StoredImageFile(
                localUri = "content://com.TallerJC.motogest.fileprovider/motogest_images/${prefix}_1.jpg",
                localPath = "/data/user/0/com.TallerJC.motogest/files/images/${prefix}_1.jpg",
                fileName = "${prefix}_1.jpg",
                mimeType = "image/jpeg",
                fileSizeBytes = 0L
            )
        }

        override fun finalizeCameraImage(destination: StoredImageFile): StoredImageFile =
            destination.copy(fileSizeBytes = 1024L)

        override suspend fun copyPickedImageToInternalStorage(sourceUri: Uri, prefix: String): StoredImageFile =
            createCameraImageDestination(prefix).copy(fileSizeBytes = 2048L)

        override fun deleteLocalFile(localPath: String): Boolean {
            deletedPaths.add(localPath)
            return true
        }
    }

    @Before
    fun setUp() {
        fakeDao = FakeMediaDao()
        fakeStorage = TrackingFakeImageStorage()
        repository = LocalMediaRepository(fakeDao, fakeStorage)
    }

    @Test
    fun testValetKeySyncLifecycle_PendingUploadingSyncedAndFailed() = runBlocking {
        val media1 = MediaEntity(
            id = "m1",
            orderId = "ord-1",
            motorcycleId = "moto-1",
            localUri = "content://com.TallerJC.motogest.fileprovider/motogest_images/ev_1.jpg",
            localPath = "/data/user/0/com.TallerJC.motogest/files/images/ev_1.jpg",
            fileName = "ev_1.jpg",
            fileSizeBytes = 4096L,
            tag = MediaTag.INTAKE_EVIDENCE.name
        )
        val media2 = MediaEntity(
            id = "m2",
            orderId = null,
            motorcycleId = "moto-1",
            localUri = "content://com.TallerJC.motogest.fileprovider/motogest_images/moto_1.jpg",
            localPath = "/data/user/0/com.TallerJC.motogest/files/images/moto_1.jpg",
            fileName = "moto_1.jpg",
            fileSizeBytes = 2048L,
            tag = MediaTag.MOTO_PROFILE.name
        )

        repository.saveImagesMetadata(listOf(media1, media2))

        // Ambos nacen PENDING y sin remoteUrl
        assertEquals(2, repository.getPendingUploads().size)
        assertEquals(2, repository.observePendingUploadsCount().first())
        assertNull(fakeDao.getMediaById("m1")?.remoteUrl)

        // Equipo de nube marca m1 en UPLOADING -> sale de pendientes para evitar duplicados
        repository.markAsUploading("m1")
        assertEquals(1, repository.getPendingUploads().size)
        assertEquals(MediaSyncStatus.UPLOADING.name, fakeDao.getMediaById("m1")?.syncStatus)

        // Subida exitosa con remoteUrl
        val urlRemota = "https://storage.example.com/motogest/ev_1.jpg"
        repository.markAsSynced("m1", urlRemota)
        val synced = fakeDao.getMediaById("m1")
        assertEquals(MediaSyncStatus.SYNCED.name, synced?.syncStatus)
        assertEquals(urlRemota, synced?.remoteUrl)

        // m2 falla -> sigue siendo elegible para reintento en getPendingUploads()
        repository.markAsFailed("m2")
        val pendingAfterFailure = repository.getPendingUploads()
        assertEquals(1, pendingAfterFailure.size)
        assertEquals("m2", pendingAfterFailure.first().id)
        assertEquals(MediaSyncStatus.FAILED.name, pendingAfterFailure.first().syncStatus)
    }

    @Test
    fun testDeleteImageRemovesDatabaseRowAndPhysicalFile() = runBlocking {
        val media = MediaEntity(
            id = "m_del",
            orderId = "ord-9",
            localUri = "content://com.TallerJC.motogest.fileprovider/motogest_images/del.jpg",
            localPath = "/data/user/0/com.TallerJC.motogest/files/images/del.jpg",
            fileName = "del.jpg"
        )
        repository.saveImageMetadata(media)

        repository.deleteImage("m_del")

        assertNull(fakeDao.getMediaById("m_del"))
        assertTrue(fakeStorage.deletedPaths.contains(media.localPath))
    }
}
