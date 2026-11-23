package com.TallerJC.motogest

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.TallerJC.motogest.data.di.AppContainer
import com.TallerJC.motogest.data.di.DefaultAppContainer

class MotoGestApplication : Application() {
    lateinit var container: AppContainer

    private val observadorCicloVida = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            container.syncScheduler.programarSincronizacionEnPrimerPlano()
        }
    }

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        container.syncScheduler.programarSincronizacionPeriodica()
        ProcessLifecycleOwner.get().lifecycle.addObserver(observadorCicloVida)
    }
}
