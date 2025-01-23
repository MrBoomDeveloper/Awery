package com.mrboomdev.awery.data

enum class FileType(val extension: String) {
    DANTOTSU_BACKUP(".ani"),
    AWERY_BACKUP(".awerybck"),
    YOMI_BACKUP(".tachibk"),
    APK(".apk");

    companion object {
        fun test(fileName: String): FileType? {
            for(type in entries) {
                if(fileName.endsWith(type.extension)) {
                    return type
                }
            }

            return null
        }
    }

}

enum class ContentType(val mimeType: String) {
    JSON("application/json"),
    APK("application/vnd.android.package-archive"),
    ANY("*/*");

    override fun toString(): String {
        return mimeType
    }
}