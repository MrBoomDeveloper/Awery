package eu.kanade.tachiyomi.animesource.model

import com.mrboomdev.awery.utils.ExtensionSdk
import java.io.Serializable

@Suppress("PropertyName")
@ExtensionSdk
interface SEpisode : Serializable {
    var url: String
    var name: String
    var date_upload: Long
    var episode_number: Float
    var scanlator: String?

    companion object {
        @ExtensionSdk
        fun create(): SEpisode = object : SEpisode {
            override lateinit var url: String
            override lateinit var name: String
            override var date_upload: Long = 0
            override var episode_number: Float = -1F
            override var scanlator: String? = null
        }
    }
}
