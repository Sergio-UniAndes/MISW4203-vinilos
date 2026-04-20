package com.misw4203.vinilos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class nchMainActivity : ComponentActivity() {
    private val appContainer by lazy { AppContainer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VinilosApp(appContainer = appContainer)
        }
    }
}

