package com.mrboomdev.awery.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.activity
import com.mrboomdev.awery.ui.App
import com.mrboomdev.awery.ui.Routes
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

class MainActivity: AppCompatActivity() {
    init { 
        Awery.activity = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        installSplashScreen()
        enableEdgeToEdge()
        setContent { App() }
    }
}