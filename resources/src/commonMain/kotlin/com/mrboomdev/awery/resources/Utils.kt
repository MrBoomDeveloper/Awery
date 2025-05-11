package com.mrboomdev.awery.resources

import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
suspend fun readAsset(path: String) =
    Res.readBytes("files/app_settings.json").toString(Charsets.UTF_8)