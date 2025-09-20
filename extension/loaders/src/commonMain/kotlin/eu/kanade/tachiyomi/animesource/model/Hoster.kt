package eu.kanade.tachiyomi.animesource.model

import kotlin.concurrent.Volatile

open class Hoster(
    val hosterUrl: String = "",
    val hosterName: String = "",
    val videoList: List<Video>? = null,
    val internalData: String = ""
) {
    @Transient
    @Volatile
    var status: State = State.IDLE

    enum class State {
        IDLE,
        LOADING,
        READY,
        ERROR
    }

    fun copy(
        hosterUrl: String = this.hosterUrl,
        hosterName: String = this.hosterName,
        videoList: List<Video>? = this.videoList,
        internalData: String = this.internalData
    ) = Hoster(hosterUrl, hosterName, videoList, internalData)

    companion object {
        const val NO_HOSTER_LIST = "no_hoster_list"

        fun List<Video>.toHosterList() = listOf(Hoster(
            hosterUrl = "",
            hosterName = NO_HOSTER_LIST,
            videoList = this
        ))
    }
}