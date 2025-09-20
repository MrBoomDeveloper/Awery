package com.mrboomdev.awery.resources

import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Reads the content of an asset file as a String.
 *
 * This function is an extension for the [Res] class, providing a convenient way
 * to read text-based assets. It assumes the asset is located within the "files/"
 * directory of your resources and decodes the bytes using UTF-8.
 *
 * @param path The relative path to the asset file within the "files/" directory (e.g., "my_text_file.txt").
 * @return The content of the asset file as a String.
 * @throws org.jetbrains.compose.resources.MissingResourceException if the specified asset is not found.
 */
@OptIn(ExperimentalResourceApi::class)
suspend fun Res.readAsset(path: String) =
    readBytes("files/$path").toString(Charsets.UTF_8)