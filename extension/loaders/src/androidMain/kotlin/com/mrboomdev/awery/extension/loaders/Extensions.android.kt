package com.mrboomdev.awery.extension.loaders

import android.app.Application
import android.content.pm.PackageManager
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context
import com.mrboomdev.awery.extension.loaders.awery.AweryExtensionConstants
import com.mrboomdev.awery.extension.loaders.awery.ResolvedExtensionParent
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.loaders.yomi.AniyomiLoader
import com.mrboomdev.awery.extension.loaders.yomi.YomiExtension
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.resolve
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.launch
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton

val Extensions.PACKAGE_MANAGER_FLAGS get() = 
    PackageManager.GET_META_DATA or PackageManager.GET_CONFIGURATIONS

actual suspend fun ProducerScope<Extension>.loadAllImpl() {
    Injekt.addSingleton<Application>(Awery.context.applicationContext as Application)

    AweryExtensionConstants.installedDirectory.let { 
        if(it.exists()) it else null
    }?.list()?.forEach {
        if(it.isRegularFile()) {
            return@forEach
        }
        
        launch {
            send(ResolvedExtensionParent(it))
        }
    }

    Awery.context.packageManager.getInstalledPackages(
        Extensions.PACKAGE_MANAGER_FLAGS
    ).forEach { pkg ->
        pkg.reqFeatures?.forEach { feature ->
            when(feature.name) {
                AniyomiLoader.requiredFeature, /*TachiyomiLoader.requiredFeature*/ -> {
                    launch {
                        send(YomiExtension(pkg))
                    }
                }
            }
        }
    }
}