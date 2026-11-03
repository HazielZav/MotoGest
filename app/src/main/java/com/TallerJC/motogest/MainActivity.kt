package com.TallerJC.motogest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.TallerJC.motogest.ui.MotoGestApp
import com.TallerJC.motogest.ui.theme.MotoGestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as MotoGestApplication).container
        setContent {
            MotoGestTheme {
                MotoGestApp(appContainer = appContainer)
            }
        }
    }
}
