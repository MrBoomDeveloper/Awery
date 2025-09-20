@file:Suppress("INVISIBLE_REFERENCE")

package com.mrboomdev.awery.resources

import android.R.attr.path
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.Density
import org.jetbrains.compose.resources.*
import org.jetbrains.compose.resources.FontResource
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
actual fun FontCompat(
    resource: FontResource,
    weight: FontWeight,
    style: FontStyle,
    variationSettings: FontVariation.Settings
): Font {
    return if(Build.VERSION.SDK_INT >= 26) {
        Font(resource, weight, style, variationSettings)
    } else {
        // Everything breaks at AndroidPreloadedFont.android.kt AndroidAssetFont.doLoad
        FontCompatImpl(resource, weight, style, variationSettings)
    }
}

@OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)
@Composable
private fun FontCompatImpl(
    resource: FontResource,
    weight: FontWeight,
    style: FontStyle,
    variationSettings: FontVariation.Settings
): Font {
    val resourceReader = LocalResourceReader.currentOrPreview
    val fontFile by rememberResourceState(resource, weight, style, variationSettings, { defaultEmptyFont }) { env ->
        val path = resource.getResourceItemByEnvironment(env).path
        val key = "$path:$weight:$style:${variationSettings.getCacheKey()}"
        fontCache.getOrLoad(key) {
            val fontBytes = resourceReader.read(path)
            Font(key, fontBytes, weight, style, variationSettings)
        }
    }
    return fontFile
}

private const val emptyFontBase64 =
    "T1RUTwAJAIAAAwAQQ0ZGIML7MfIAAAQIAAAA2U9TLzJmMV8PAAABAAAAAGBjbWFwANUAVwAAA6QAAABEaGVhZCMuU7" +
            "IAAACcAAAANmhoZWECvgAmAAAA1AAAACRobXR4Az4AAAAABOQAAAAQbWF4cAAEUAAAAAD4AAAABm5hbWUpw3nbAAABYAAAAkNwb3N0AAMA" +
            "AAAAA+gAAAAgAAEAAAABAADs7nftXw889QADA+gAAAAA4WWJaQAAAADhZYlpAAAAAAFNAAAAAAADAAIAAAAAAAAAAQAAArz+1AAAAU0AAA" +
            "AAAAAAAQAAAAAAAAAAAAAAAAAAAAQAAFAAAAQAAAADAHwB9AAFAAACigK7AAAAjAKKArsAAAHfADEBAgAAAAAAAAAAAAAAAAAAAAEAAAAA" +
            "AAAAAAAAAABYWFhYAEAAIABfArz+1AAAAAAAAAAAAAEAAAAAAV4AAAAgACAAAAAAACIBngABAAAAAAAAAAIAbwABAAAAAAABAAUAAAABAA" +
            "AAAAACAAcADwABAAAAAAADABAAdQABAAAAAAAEAA0AJAABAAAAAAAFAAIAbwABAAAAAAAGAAwASwABAAAAAAAHAAIAbwABAAAAAAAIAAIA" +
            "bwABAAAAAAAJAAIAbwABAAAAAAAKAAIAbwABAAAAAAALAAIAbwABAAAAAAAMAAIAbwABAAAAAAANAAIAbwABAAAAAAAOAAIAbwABAAAAAA" +
            "AQAAUAAAABAAAAAAARAAcADwADAAEECQAAAAQAcQADAAEECQABAAoABQADAAEECQACAA4AFgADAAEECQADACAAhQADAAEECQAEABoAMQAD" +
            "AAEECQAFAAQAcQADAAEECQAGABgAVwADAAEECQAHAAQAcQADAAEECQAIAAQAcQADAAEECQAJAAQAcQADAAEECQAKAAQAcQADAAEECQALAA" +
            "QAcQADAAEECQAMAAQAcQADAAEECQANAAQAcQADAAEECQAOAAQAcQADAAEECQAQAAoABQADAAEECQARAA4AFmVtcHR5AGUAbQBwAHQAeVJl" +
            "Z3VsYXIAUgBlAGcAdQBsAGEAcmVtcHR5IFJlZ3VsYXIAZQBtAHAAdAB5ACAAUgBlAGcAdQBsAGEAcmVtcHR5UmVndWxhcgBlAG0AcAB0AH" +
            "kAUgBlAGcAdQBsAGEAciIiACIAIiIiOmVtcHR5IFJlZ3VsYXIAIgAiADoAZQBtAHAAdAB5ACAAUgBlAGcAdQBsAGEAcgAAAAABAAMAAQAA" +
            "AAwABAA4AAAACgAIAAIAAgAAACAAQQBf//8AAAAAACAAQQBf//8AAP/h/8H/pAABAAAAAAAAAAAAAAADAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAQAEAQABAQENZW1wdHlSZWd1bGFyAAEBASf4GwD4HAL4HQP4HgSLi/lQ9+EFHQAAAHgPHQAAAH8Rix0AAADZEgAHAQED" +
            "EBUcISIsIiJlbXB0eSBSZWd1bGFyZW1wdHlSZWd1bGFyc3BhY2VBdW5kZXJzY29yZQAAAAGLAYwBjQAEAQFMT1FT+F2f+TcVi4uL/TeLiw" +
            "iLi/g1i4uLCIuLi/k3i4sIi4v8NYuLiwi7/QcVi4uL+NeLiwiLi/fUi4uLCIuLi/zXi4sIi4v71IuLiwgO9+EOnw6fDgAAAAHJAAABTQAA" +
            "ABQAAAAUAAA="

@OptIn(ExperimentalEncodingApi::class)
private val defaultEmptyFont by lazy { Font("org.jetbrains.compose.emptyFont", Base64.decode(emptyFontBase64)) }

private val fontCache = AsyncCache<String, Font>()

private fun Font(
    identity: String,
    data: ByteArray,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
    variationSettings: FontVariation.Settings = FontVariation.Settings(weight, style)
): Font = Font(
    identity = identity,
    getData = { data },
    weight = weight,
    style = style,
    variationSettings = variationSettings,
)

private fun Font(
    identity: String,
    getData: () -> ByteArray,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
    variationSettings: FontVariation.Settings = FontVariation.Settings(weight, style)
): Font = LoadedFont(identity, getData, weight, style, variationSettings)

private fun Font(
    identity: String,
    data: ByteArray,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal
): Font = Font(
    identity = identity,
    getData = { data },
    weight = weight,
    style = style
)

private fun Font(
    identity: String,
    getData: () -> ByteArray,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal
): Font = LoadedFont(identity, getData, weight, style)

private class LoadedFont(
    override val identity: String,
    internal val getData: () -> ByteArray,
    override val weight: FontWeight,
    override val style: FontStyle,
    override val variationSettings: FontVariation.Settings = FontVariation.Settings(weight, style),
) : PlatformFont() {

    constructor(
        identity: String,
        getData: () -> ByteArray,
        weight: FontWeight,
        style: FontStyle
    ) : this(identity, getData, weight, style, FontVariation.Settings())

    @ExperimentalTextApi
    override val loadingStrategy: FontLoadingStrategy = FontLoadingStrategy.Blocking

    val data: ByteArray get() = getData()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoadedFont) return false
        if (identity != other.identity) return false
        if (weight != other.weight) return false
        if (style != other.style) return false
        return variationSettings.settings == other.variationSettings.settings
    }

    override fun hashCode(): Int {
        var result = identity.hashCode()
        result = 31 * result + weight.hashCode()
        result = 31 * result + style.hashCode()
        result = 31 * result + variationSettings.settings.hashCode()
        return result
    }

    override fun toString(): String {
        return "LoadedFont(identity='$identity', weight=$weight, style=$style, variationSettings=${variationSettings.settings})"
    }
}

internal fun FontVariation.Settings.getCacheKey(): String {
    val defaultDensity = Density(1f)
    return settings
        .map { "${it::class.simpleName}(${it.axisName},${it.toVariationValue(defaultDensity)})" }
        .sorted()
        .joinToString(",")
}

private sealed class PlatformFont : Font {
    abstract val identity: String
    abstract val variationSettings: FontVariation.Settings
    val cacheKey: String
        get() = "${this::class.qualifiedName}|$identity|weight=${weight.weight}|style=$style"
}