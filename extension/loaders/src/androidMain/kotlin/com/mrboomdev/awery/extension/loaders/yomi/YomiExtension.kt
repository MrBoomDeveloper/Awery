package com.mrboomdev.awery.extension.loaders.yomi

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.mrboomdev.awery.android.AndroidUtils
import com.mrboomdev.awery.android.getItems
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.AndroidPreferences
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.sdk.BooleanPreference
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.ExtensionLoadException
import com.mrboomdev.awery.extension.sdk.Image
import com.mrboomdev.awery.extension.sdk.IntPreference
import com.mrboomdev.awery.extension.sdk.LabelPreference
import com.mrboomdev.awery.extension.sdk.Preference
import com.mrboomdev.awery.extension.sdk.PreferenceGroup
import com.mrboomdev.awery.extension.sdk.Preferences
import com.mrboomdev.awery.extension.sdk.StringPreference
import com.mrboomdev.awery.extension.sdk.modules.ManageableModule
import com.mrboomdev.awery.extension.sdk.modules.ManagerModule
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.preferenceKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.asFlow
import ru.solrudev.ackpine.session.Session
import ru.solrudev.ackpine.session.await
import ru.solrudev.ackpine.session.parameters.Confirmation
import ru.solrudev.ackpine.uninstaller.PackageUninstaller
import ru.solrudev.ackpine.uninstaller.UninstallFailure
import ru.solrudev.ackpine.uninstaller.createSession
import java.io.File

/**
 * Represents an Aniyomi/Tachiyomi extension.
 *
 * @property packageInfo The package information of the extension.
 */
open class YomiExtension(
    internal val packageInfo: PackageInfo
): Extension {
    private val sources = YomiLoader.initAllSources(this)
    override val loadException: ExtensionLoadException? = null

    private val lazyName by lazy {
        packageInfo.applicationInfo!!.loadLabel(Awery.context.packageManager)
            .toString()
            .substringAfter("Aniyomi: ").substringAfter("Tachiyomi: ")
    }

    private val lazyIcon by lazy {
        Image(packageInfo.applicationInfo!!.loadIcon(Awery.context.packageManager))
    }

    private val lazyVersion by lazy {
        packageInfo.versionName?.also { return@lazy it }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toString()
        }
    }
    
    private val lazyIsNsfw by lazy { 
        sources.any { it.isNsfw }
    }

    override val name: String
        get() = lazyName

    override val id: String
        get() = "yomi_${packageInfo.applicationInfo!!.packageName}"

    override val version: String
        get() = lazyVersion

    override val webpage: String?
        get() = null
    
    override val lang: String?
        get() = null

    override val isNsfw: Boolean
        get() = lazyIsNsfw
    
    override val icon: Image?
        get() = lazyIcon

    override fun createModules() = listOf(
        object : ManagerModule { 
            override fun getAll() = sources.asFlow() 
        },
        
        object : ManageableModule {
            override fun onSavePreferences(preferences: List<Preference<*>>) {
                val editors = mutableMapOf<String, Preferences>()
                
                for(preference in preferences) {
                    val source = preference.key.substringBefore("_")
                    val key = preference.key.substringAfter("_")
                    
                    preference.save(editors.getOrPut(source) {
                        AndroidPreferences(AndroidUtils.getSharedPreferences("source_$source"))
                    }, key)
                }
            }

            override fun getPreferences() = sources.mapNotNull { childSource ->
                when(childSource) {
                    is AniyomiSource -> childSource.source.let {
                        if(it !is ConfigurableAnimeSource) {
                            return@let null
                        }
                        
                        it.id.toString() to AndroidUtils.createPreferenceScreen("source_${it.id}").apply {
                            it.setupPreferenceScreen(this)
                        }
                    }
                        
                    is TachiyomiSource -> childSource.source.let {
                        if(it !is ConfigurableSource) {
                            return@let null
                        }
                        
                        it.id.toString() to AndroidUtils.createPreferenceScreen("source_${it.id}").apply {
                            it.setupPreferenceScreen(this)
                        }
                    }
                        
                    else -> null
                }
            }.flatMap { (prefix, screen) ->
                screen.getItems().mapNotNull {
                    it.toAweryPreference(prefix + "_")
                }
            }

            override suspend fun uninstall() {
                PackageUninstaller.getInstance(Awery.context)
                    .createSession(packageInfo.packageName) {
                        confirmation = Confirmation.IMMEDIATE
                    }.await().also { state ->
                        if(state !is Session.State.Failed) return@also
                        
                        when(state.failure) {
                            is UninstallFailure.Aborted -> throw IllegalStateException("Aborted!")
                            else -> throw CancellationException()
                        }
                    }

                Extensions.remove(this@YomiExtension)
            }
        }
    )

    /**
     * Represents a Yomi extension loaded from an APK file.
     *
     * @param file The APK file of the extension.
     * @throws NullPointerException If the package archive information cannot be retrieved from the APK file.
     */
    class Apk(file: File): YomiExtension(file.let {
        val path = file.absolutePath

        if(file.canWrite()) {
            // On Android 14+ you cannot dynamically load code from writable files.
            file.setWritable(false)
        }

        Awery.context.packageManager.getPackageArchiveInfo(
            path, PackageManager.GET_META_DATA or PackageManager.GET_CONFIGURATIONS
        )!!.apply {
            applicationInfo!!.apply {
                // On Android 13+ the ApplicationInfo generated by getPackageArchiveInfo doesn't
                // have sourceDir which breaks assets loading (used for getting icon here).
                if(sourceDir == null) sourceDir = path
                if(publicSourceDir == null) publicSourceDir = path
            }
        }
    })
}