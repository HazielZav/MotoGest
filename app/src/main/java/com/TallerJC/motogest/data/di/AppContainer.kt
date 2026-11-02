package com.TallerJC.motogest.data.di

import android.content.Context
import com.TallerJC.motogest.data.local.AppDatabase
import com.TallerJC.motogest.data.local.dao.MediaDao
import com.TallerJC.motogest.data.local.dao.NotificationDao
import com.TallerJC.motogest.data.local.dao.WorkOrderDao
import com.TallerJC.motogest.data.remote.api.RetrofitClient
import com.TallerJC.motogest.data.remote.datasource.RemoteWorkOrderDataSource
import com.TallerJC.motogest.data.remote.datasource.RemoteWorkOrderDataSourceImpl
import com.TallerJC.motogest.data.repository.GerenciaRepository
import com.TallerJC.motogest.data.repository.GerenciaRepositoryImpl
import com.TallerJC.motogest.data.repository.OperacionesRepository

interface AppContainer {
    val database: AppDatabase
    val workOrderDao: WorkOrderDao
    val notificationDao: NotificationDao
    val mediaDao: MediaDao
    val remoteDataSource: RemoteWorkOrderDataSource
    val operacionesRepository: OperacionesRepository
    val gerenciaRepository: GerenciaRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val workOrderDao: WorkOrderDao by lazy {
        database.workOrderDao()
    }

    override val notificationDao: NotificationDao by lazy {
        database.notificationDao()
    }

    override val mediaDao: MediaDao by lazy {
        database.mediaDao()
    }

    override val remoteDataSource: RemoteWorkOrderDataSource by lazy {
        RemoteWorkOrderDataSourceImpl(RetrofitClient.apiService)
    }

    override val operacionesRepository: OperacionesRepository by lazy {
        OperacionesRepository(workOrderDao, remoteDataSource)
    }

    override val gerenciaRepository: GerenciaRepository by lazy {
        GerenciaRepositoryImpl(workOrderDao)
    }
}
