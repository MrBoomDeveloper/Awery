package ani.awery

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import ani.awery.aniyomi.anime.custom.AppModule
import ani.awery.aniyomi.anime.custom.PreferenceModule
import com.mrboomdev.awery.app.AweryApp
import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.LogcatLogger
import uy.kohesive.injekt.Injekt

@SuppressLint("StaticFieldLeak")
open class App : Application() {
    init {
        instance = this
    }

    val mFTActivityLifecycleCallbacks = FTActivityLifecycleCallbacks()

    override fun onCreate() {
        super.onCreate()

        if(AweryApp.USE_KT_APP_INIT) {
            registerActivityLifecycleCallbacks(mFTActivityLifecycleCallbacks)

            Injekt.importModule(AppModule(this))
            Injekt.importModule(PreferenceModule(this))

            initializeNetwork()

            if(!LogcatLogger.isInstalled) {
                LogcatLogger.install(AndroidLogcatLogger(LogPriority.VERBOSE))
            }
        }
    }

    inner class FTActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
        var currentActivity: Activity? = null
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
        override fun onActivityStarted(p0: Activity) {
            currentActivity = p0
        }

        override fun onActivityResumed(p0: Activity) {
            currentActivity = p0
        }

        override fun onActivityPaused(p0: Activity) {}
        override fun onActivityStopped(p0: Activity) {}
        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
        override fun onActivityDestroyed(p0: Activity) {}
    }

    companion object {
        private var instance: App? = null
        var context: Context? = null

        fun currentContext(): Context? {
            return instance?.mFTActivityLifecycleCallbacks?.currentActivity ?: context
        }

        fun currentActivity(): Activity? {
            return instance?.mFTActivityLifecycleCallbacks?.currentActivity
        }
    }
}