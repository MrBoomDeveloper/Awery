package com.mrboomdev.awery.extension.loaders.yomi

import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.ExtensionLoadException
import com.mrboomdev.awery.extension.loaders.FailedExtension
import com.mrboomdev.awery.extension.loaders.yomi.AniyomiLoader.maxVersion
import com.mrboomdev.awery.extension.loaders.yomi.AniyomiLoader.minVersion
import dalvik.system.PathClassLoader
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.AnimeSourceFactory

object AniyomiLoader: YomiLoader {
    override val namePrefix = "Aniyomi: "
    override val nsfwMeta = "tachiyomi.animeextension.nsfw"
    override val mainClassMeta = "tachiyomi.animeextension.class"
    override val requiredFeature = "tachiyomi.animeextension"
    override val minVersion = 12.0
    override val maxVersion = 15.0

    override fun loadMain(
        parentExtension: Extension,
        classLoader: ClassLoader,
        main: String,
        isNsfw: Boolean
    ): Collection<Extension> = buildList {
        try {
            when(val instance = Class.forName(main, true, classLoader).getConstructor().newInstance()) {
                is AnimeSource -> add(AniyomiSource(parentExtension, instance, isNsfw))
                
                is AnimeSourceFactory -> addAll(instance.createSources().map {
                    AniyomiSource(parentExtension, it, isNsfw)
                })
                
                else -> throw ExtensionLoadException.IllegalArchitecture("Unknown main class type!")
            }
        } catch(t: Throwable) {
            FailedExtension(
                parentExtension = parentExtension,
                id = "${parentExtension.id}_$main",
                loadException = ExtensionLoadException("Failed to create an instance of the extension!", t)
            )
        }
    }
}

interface YomiLoader {
    val mainClassMeta: String?
    val nsfwMeta: String?
    val namePrefix: String
    val requiredFeature: String
    val minVersion: Double
    val maxVersion: Double

    fun loadMain(
        parentExtension: Extension,
        classLoader: ClassLoader,
        main: String,
        isNsfw: Boolean
    ): Collection<Extension>

    companion object {
        private val loaders = listOf(AniyomiLoader, /*TachiyomiLoader*/)

        fun initAllSources(extension: YomiExtension) = buildList<Extension> {
            val classLoader = PathClassLoader(
                extension.packageInfo.applicationInfo!!.sourceDir,
                null,
                YomiLoader::class.java.classLoader
            )

            for(loader in loaders) {
                if(extension.packageInfo.reqFeatures?.any { it.name == loader.requiredFeature } != true) continue

                val e = runCatching {
                    checkSupportedVersionBounds(extension.packageInfo.versionName!!, minVersion, maxVersion)
                }.exceptionOrNull() as ExtensionLoadException?

                if(e != null) {
                    add(FailedExtension(
                        parentExtension = extension,
                        id = "${extension.id}_${loader::class.simpleName}",
                        loadException = e
                    ))

                    continue
                }
                
                val isNsfw = extension.packageInfo.applicationInfo?.metaData?.getInt(loader.nsfwMeta) == 1

                extension.packageInfo.applicationInfo?.metaData?.getString(loader.mainClassMeta)?.split(";")?.map { main ->
                    if(main.startsWith(".")) (extension.packageInfo.packageName + main) else main
                }?.forEach { main ->
                    addAll(loader.loadMain(extension, classLoader, main, isNsfw))
                }
            }
        }
    }
}

/**
 * @throws ExtensionLoadException.UnsupportedLibVersion if the library version is not supported
 */
internal fun checkSupportedVersionBounds(
    versionName: String,
    minVersion: Double,
    maxVersion: Double
) {
    var versionName = versionName
    val secondDotIndex = versionName.indexOf(".", versionName.indexOf(".") + 1)

    if(secondDotIndex != -1) {
        versionName = versionName.substring(0, secondDotIndex)
    }

    val version = versionName.toDouble()

    if(version < minVersion) {
        throw ExtensionLoadException.UnsupportedLibVersion(
            version = versionName,
            minVersion = minVersion.toString(),
            maxVersion = maxVersion.toString(),
            message = "Unsupported deprecated library version!"
        )
    }

    if(version > maxVersion) {
        throw ExtensionLoadException.UnsupportedLibVersion(
            version = versionName,
            minVersion = minVersion.toString(),
            maxVersion = maxVersion.toString(),
            message = "Unsupported new library version!"
        )
    }
}