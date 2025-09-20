package eu.kanade.tachiyomi.animesource.online

import com.mrboomdev.awery.core.utils.PlatformSdk
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import eu.kanade.tachiyomi.animesource.model.Hoster
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * A simple implementation for sources from a website using Jsoup, an HTML parser.
 */
@PlatformSdk
abstract class ParsedAnimeHttpSource : AnimeHttpSource() {

    /**
     * Parses the response from the site and returns a [AnimesPage] object.
     *
     * @param response the response from the site.
     */
    @PlatformSdk
    override fun popularAnimeParse(response: Response): AnimesPage {
        val document = response.asJsoup()

        val animes = document.select(popularAnimeSelector()).map { element ->
            popularAnimeFromElement(element)
        }

        val hasNextPage = popularAnimeNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return AnimesPage(animes, hasNextPage)
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each anime.
     */
    @PlatformSdk
    protected abstract fun popularAnimeSelector(): String

    /**
     * Returns an anime from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [popularAnimeSelector].
     */
    @PlatformSdk
    protected abstract fun popularAnimeFromElement(element: Element): SAnime

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    @PlatformSdk
    protected abstract fun popularAnimeNextPageSelector(): String?

    /**
     * Parses the response from the site and returns a [AnimesPage] object.
     *
     * @param response the response from the site.
     */
    @PlatformSdk
    override fun searchAnimeParse(response: Response): AnimesPage {
        val document = response.asJsoup()

        val animes = document.select(searchAnimeSelector()).map { element ->
            searchAnimeFromElement(element)
        }

        val hasNextPage = searchAnimeNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return AnimesPage(animes, hasNextPage)
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each anime.
     */
    @PlatformSdk
    protected abstract fun searchAnimeSelector(): String

    /**
     * Returns an anime from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [searchAnimeSelector].
     */
    @PlatformSdk
    protected abstract fun searchAnimeFromElement(element: Element): SAnime

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    @PlatformSdk
    protected abstract fun searchAnimeNextPageSelector(): String?

    /**
     * Parses the response from the site and returns a [AnimesPage] object.
     *
     * @param response the response from the site.
     */
    @PlatformSdk
    override fun latestUpdatesParse(response: Response): AnimesPage {
        val document = response.asJsoup()

        val animes = document.select(latestUpdatesSelector()).map { element ->
            latestUpdatesFromElement(element)
        }

        val hasNextPage = latestUpdatesNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return AnimesPage(animes, hasNextPage)
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each anime.
     */
    @PlatformSdk
    protected abstract fun latestUpdatesSelector(): String

    /**
     * Returns an anime from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [latestUpdatesSelector].
     */
    @PlatformSdk
    protected abstract fun latestUpdatesFromElement(element: Element): SAnime

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    @PlatformSdk
    protected abstract fun latestUpdatesNextPageSelector(): String?

    /**
     * Parses the response from the site and returns the details of an anime.
     *
     * @param response the response from the site.
     */
    @PlatformSdk
    override fun animeDetailsParse(response: Response): SAnime {
        return animeDetailsParse(response.asJsoup())
    }

    /**
     * Returns the details of the anime from the given [document].
     *
     * @param document the parsed document.
     */
    @PlatformSdk
    protected abstract fun animeDetailsParse(document: Document): SAnime

    /**
     * Parses the response from the site and returns a list of related animes.
     * Normally it's not needed to override this method.
     *
     * @since komikku/extensions-lib 1.6
     * @param response the response from the site.
     */
    @PlatformSdk
    override fun relatedAnimeListParse(response: Response): List<SAnime> {
        return response.asJsoup()
            .select(relatedAnimeListSelector()).map { relatedAnimeFromElement(it) }
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each related animes.
     *
     * @since komikku/extensions-lib 1.6
     */
    @PlatformSdk
    protected open fun relatedAnimeListSelector(): String = popularAnimeSelector()

    /**
     * Returns a anime from the given element.
     *
     * @since komikku/extensions-lib 1.6
     * @param element an element obtained from [relatedAnimeListSelector].
     */
    @PlatformSdk
    protected open fun relatedAnimeFromElement(element: Element): SAnime = 
        popularAnimeFromElement(element)

    /**
     * Parses the response from the site and returns a list of episodes.
     *
     * @param response the response from the site.
     */
    @PlatformSdk
    override fun episodeListParse(response: Response): List<SEpisode> {
        val document = response.asJsoup()
        return document.select(episodeListSelector()).map { episodeFromElement(it) }
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each episode.
     */
    @PlatformSdk
    protected abstract fun episodeListSelector(): String

    /**
     * Returns an episode from the given element.
     *
     * @param element an element obtained from [episodeListSelector].
     */
    @PlatformSdk
    protected abstract fun episodeFromElement(element: Element): SEpisode

    /**
     * Parses the response from the site and returns the hoster list.
     *
     * @since extensions-lib 16
     * @param response the response from the site.
     * @return the list of hosters.
     */
    override fun hosterListParse(response: Response): List<Hoster> {
        val document = response.asJsoup()
        return document.select(hosterListSelector()).map(::hosterFromElement)
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each hoster.
     *
     * @since extensions-lib 16
     */
    protected abstract fun hosterListSelector(): String

    /**
     * Returns a hoster from the given element.
     *
     * @since extensions-lib 16
     * @param element an element obtained from [hosterListSelector].
     */
    protected abstract fun hosterFromElement(element: Element): Hoster

    /**
     * Parses the response from the site and returns the page list.
     *
     * @param response the response from the site.
     */
    @PlatformSdk
    override fun videoListParse(response: Response): List<Video> {
        val document = response.asJsoup()
        return document.select(videoListSelector()).map { videoFromElement(it) }
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each video.
     */
    @PlatformSdk
    protected abstract fun videoListSelector(): String

    /**
     * Returns a video from the given element.
     *
     * @param element an element obtained from [videoListSelector].
     */
    @PlatformSdk
    protected abstract fun videoFromElement(element: Element): Video

    /**
     * Parse the response from the site and returns the absolute url to the source video.
     *
     * @param response the response from the site.
     */
    @PlatformSdk
    override fun videoUrlParse(response: Response): String {
        return videoUrlParse(response.asJsoup())
    }

    /**
     * Returns the absolute url to the source image from the document.
     *
     * @param document the parsed document.
     */
    @PlatformSdk
    protected abstract fun videoUrlParse(document: Document): String
}