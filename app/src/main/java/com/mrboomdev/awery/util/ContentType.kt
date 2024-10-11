package com.mrboomdev.awery.util

enum class ContentType(val mimeType: String) {
    JSON("application/json"),
    APK("application/vnd.android.package-archive"),
    ANY("*/*");

    override fun toString(): String {
        return mimeType
    }
}