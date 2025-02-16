package eu.kanade.tachiyomi.source.model

import com.mrboomdev.awery.utils.ExtensionSdk
import java.io.Serializable

@ExtensionSdk
interface SChapter : Serializable {
    var url: String
    var name: String
    var date_upload: Long
    var chapter_number: Float
    var scanlator: String?

    companion object {
        @ExtensionSdk
        fun create(): SChapter = object : SChapter {
            override lateinit var url: String
            override lateinit var name: String
            override var date_upload: Long = 0
            override var chapter_number: Float = -1f
            override var scanlator: String? = null
        }
    }
}