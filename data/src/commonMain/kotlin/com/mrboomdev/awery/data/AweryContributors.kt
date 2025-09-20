package com.mrboomdev.awery.data

import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.http
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.bodyAsJson
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val EXCLUDE_GITHUB_IDS = arrayOf(
    87634197L,  // rebelonion
    1607653L,   // weblate
    99584765L,  // aayush2622
    99601717L,  // Sadwhy
    149729762L, // WaiWhat
    114061880L, // tinotendamha
    72887534L,  // asvintheguy
    175734244L, // SaarGirl
    171004872L, // Goko1
    119158637L, // Runkandel
    22217419L,  // rezaalmanda
    49699333,   // dependabot[bot]
    27347476    // dependabot
)

data class Contributor(
    val name: String,
    val role: String? = null,
    val url: String,
    val avatar: String
)

@Serializable
private class GitHubContributor(
    val login: String,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("html_url") val htmlUrl: String,
    val id: Long
)

object AweryContributors {
    /**
     * Returns lists with both remote and local contributors.
     * Locals are first, because they do have more info.
     */
    fun getAll() = flow {
        val local = getLocal()
        emit(local)

        val localUsernames = local.map {
            it.name.lowercase()
        }.toSet() + "mrboomdeveloper"

        try {
            getRemote().filter {
                it.id !in EXCLUDE_GITHUB_IDS && it.login.lowercase() !in localUsernames
            }.map {
                Contributor(
                    name = it.login,
                    url = it.htmlUrl,
                    avatar = it.avatarUrl
                )
            }.also { fetched ->
                emit(local + fetched)
            }
        } catch(e: Exception) {
            Log.e("AweryContributors", "Failed to fetch GitHub contributors!", e)
        }
    }

    /**
     * Fetches remote contributors from an GitHub repository.
     */
    private suspend fun getRemote() = Awery.http.get(
        "https://api.github.com/repos/MrBoomDeveloper/Awery/contributors?per_page=100&page=0"
    ).bodyAsJson<List<GitHubContributor>>()

    /**
     * Returns static list of contributors.
     *
     */
    private fun getLocal() = listOf(
        Contributor(
            name = "MrBoomDev",
            role = "Main Developer",
            url = "https://github.com/MrBoomDeveloper",
            avatar = "https://cdn.discordapp.com/avatars/1034891767822176357/3420c6a4d16fe513a69c85d86cb206c2.png?size=4096"
        ),

        Contributor(
            name = "Itsmechinmoy",
            role = "Contributor, Discord and Telegram Admin",
            url = "https://github.com/itsmechinmoy",
            avatar = "https://avatars.githubusercontent.com/u/167056923?v=4"
        ),

        Contributor(
            name = "Shebyyy",
            role = "Contributor, Discord and Telegram Moderator",
            url = "https://github.com/Shebyyy",
            avatar = "https://avatars.githubusercontent.com/u/83452219?v=4"
        ),

        Contributor(
            name = "MiRU",
            role = "Discord and Telegram Moderator",
            url = "https://github.com/Mutsukikamishiro",
            avatar = "https://avatars.githubusercontent.com/u/108610034?v=4"
        ),

        Contributor(
            name = "Ichiro",
            role = "Icon Designer",
            url = "https://discord.com/channels/@me/1262060731981889536",
            avatar = "https://cdn.discordapp.com/avatars/778503249619058689/5e1cd37e9473c7bc8ca164fe4f985e87.webp?size=4096"
        )
    )
}