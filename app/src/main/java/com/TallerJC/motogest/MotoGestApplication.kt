package com.TallerJC.motogest

import android.app.Application
import com.TallerJC.motogest.data.di.AppContainer
import com.TallerJC.motogest.data.di.DefaultAppContainer

class MotoGestApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
