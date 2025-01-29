package eu.kanade.tachiyomi.animesource

interface AnimeSourceFactory {
	fun createSources(): List<AnimeSource>
}