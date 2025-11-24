package com.mrboomdev.awery.extension.loaders.yomi

import com.mrboomdev.awery.core.utils.deserialize
import com.mrboomdev.awery.core.utils.serialize
import com.mrboomdev.awery.core.utils.tryOr
import com.mrboomdev.awery.extension.sdk.Media
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SerializableSAnime
import eu.kanade.tachiyomi.animesource.model.toSAnime
import eu.kanade.tachiyomi.animesource.model.toSerializable
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import eu.kanade.tachiyomi.source.MangaSource
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun SAnime.toMedia(
    source: AnimeSource,
    isNsfw: Boolean,
    ogAnime: SAnime? = null
): Media {
    // Some extensions do leave these fields as uninitialized after update -_-
    tryOr({ url }, { url = ogAnime!!.url })
    tryOr({ title }, { title = ogAnime!!.title })
    
    return Media(
        id = url,
        title = title,
        description = description,
        poster = thumbnail_url,
        tags = getGenres(),
        url = (source as? AnimeHttpSource)?.let { concatYomiUrl(it.baseUrl, url) },
        ageRating = if(isNsfw) "NSFW" else null,
        extras = mapOf("SAnime" to Json.encodeToString(toSerializable()))
    )
}

@OptIn(ExperimentalEncodingApi::class)
fun Media.toSAnime(): SAnime {
    extras["SAnime"]?.also { anime ->
        // If this media came from this source, then we will be able
        // to restore an original form without any problem!
        return Json.decodeFromString<SerializableSAnime>(anime).toSAnime()
    }

    return SAnime.create().apply {
        url = this@toSAnime.id
        title = this@toSAnime.title
        thumbnail_url = poster ?: banner
        description = this@toSAnime.description
        genre = this@toSAnime.tags?.joinToString(", ")
        initialized = true
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun SManga.toMedia(
    source: MangaSource,
    isNsfw: Boolean,
    ogManga: SManga? = null
): Media {
    // Some extensions do leave these fields as uninitialized after update -_-
    tryOr({ url }, { url = ogManga!!.url })
    tryOr({ title }, { title = ogManga!!.title })
    
    return Media(
        id = url,
        title = title,
        description = description,
        poster = thumbnail_url,
        tags = getGenres(),
        url = (source as? HttpSource)?.let { concatYomiUrl(it.baseUrl, url) },
        ageRating = if(isNsfw) "NSFW" else null,
        extras = mapOf("SManga" to Base64.encode(serialize()))
    )
}

@OptIn(ExperimentalEncodingApi::class)
fun Media.toSManga(): SManga {
    extras["SManga"]?.also { manga ->
        try {
            // If this media came from this source, then we will be able
            // to restore an original form without any problem!
            return Base64.decode(manga).deserialize() as SManga
        } catch(_: Throwable) {}
    }
    
    return SManga.create().apply {
        url = this@toSManga.id
        title = this@toSManga.title
        thumbnail_url = poster ?: banner
        description = this@toSManga.description
        genre = this@toSManga.tags?.joinToString(", ")
        initialized = true
    }
}

private fun concatYomiUrl(baseUrl: String, path: String): String = buildString {
    append(baseUrl.trimEnd('/'))
    append("/")
    append(path.trimStart('/'))
}