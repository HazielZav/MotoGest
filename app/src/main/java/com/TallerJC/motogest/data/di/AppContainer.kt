package com.TallerJC.motogest.data.di

import android.content.Context
import com.TallerJC.motogest.data.local.AppDatabase
import com.TallerJC.motogest.data.local.dao.MediaDao
import com.TallerJC.motogest.data.local.dao.NotificationDao
import com.TallerJC.motogest.data.local.dao.UserDao
import com.TallerJC.motogest.data.local.dao.WorkOrderDao
import com.TallerJC.motogest.data.local.session.SessionManager
import com.TallerJC.motogest.data.local.session.SharedPreferencesSessionManager
import com.TallerJC.motogest.data.remote.api.RetrofitClient
import com.TallerJC.motogest.data.remote.datasource.RemoteWorkOrderDataSource
import com.TallerJC.motogest.data.remote.datasource.RemoteWorkOrderDataSourceImpl
import com.TallerJC.motogest.data.repository.AuthRepository
import com.TallerJC.motogest.data.repository.AuthRepositoryImpl
import com.TallerJC.motogest.data.repository.GerenciaRepository
import com.TallerJC.motogest.data.repository.GerenciaRepositoryImpl
import com.TallerJC.motogest.data.repository.OperacionesRepository

interface AppContainer {
    val database: AppDatabase
    val workOrderDao: WorkOrderDao
    val notificationDao: NotificationDao
    val mediaDao: MediaDao
    val userDao: UserDao
    val sessionManager: SessionManager
    val remoteDataSource: RemoteWorkOrderDataSource
    val authRepository: AuthRepository
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

    override val userDao: UserDao by lazy {
        database.userDao()
    }

    override val sessionManager: SessionManager by lazy {
        SharedPreferencesSessionManager(context)
    }

    override val remoteDataSource: RemoteWorkOrderDataSource by lazy {
        RemoteWorkOrderDataSourceImpl(RetrofitClient.apiService)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(userDao, sessionManager, remoteDataSource)
    }

    override val operacionesRepository: OperacionesRepository by lazy {
        OperacionesRepository(workOrderDao, remoteDataSource)
    }

    override val gerenciaRepository: GerenciaRepository by lazy {
        GerenciaRepositoryImpl(workOrderDao, userDao, remoteDataSource)
    }
}
