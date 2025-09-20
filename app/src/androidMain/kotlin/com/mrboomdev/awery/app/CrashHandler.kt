package com.mrboomdev.awery.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.os.Process
import android.util.TypedValue
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.ScrollView
import android.widget.Space
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.core.util.TypedValueCompat
import androidx.core.view.setPadding
import com.google.android.material.color.DynamicColors
import com.google.android.material.textview.MaterialTextView
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.deserialize
import com.mrboomdev.awery.core.utils.serialize
import com.mrboomdev.awery.ui.screens.CrashScreen
import java.io.NotSerializableException
import kotlin.system.exitProcess

private const val THROWABLE_KEY = "THROWABLE"
private const val THROWABLE_SIMPLE_KEY = "THROWABLE_SIMPLE"

internal fun setupCrashHandler() {
    Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
        Log.e("CrashHandler", "Awery has crashed!", throwable)

        with(Awery.context) {
            startActivity(Intent(this, CrashActivity::class.java).apply {
                try {
                    putExtra(THROWABLE_KEY, throwable.serialize())
                } catch(_: NotSerializableException) {
                    putExtra(THROWABLE_SIMPLE_KEY, throwable.stackTraceToString())
                }
                
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })

            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }
}

class CrashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val throwable = intent.getByteArrayExtra(
            THROWABLE_KEY
        )?.deserialize() as Throwable?
        
        val simpleThrowable = intent.getStringExtra(THROWABLE_SIMPLE_KEY)
        
        setContent { 
            CrashScreen(
                throwable = throwable,
                simpleThrowable = simpleThrowable
            ) 
        }
    }
}