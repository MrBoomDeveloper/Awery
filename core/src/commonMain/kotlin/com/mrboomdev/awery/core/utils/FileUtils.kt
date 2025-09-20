package com.mrboomdev.awery.core.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.size
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest

expect fun PlatformFile.toJavaFile(): File

fun File.toPlatformFile() = PlatformFile(absolutePath)

expect fun PlatformFile.openInputStream(): InputStream

suspend fun PlatformFile.deleteRecursively() {
    if(!exists()) return
    
    if(isDirectory()) {
        list().forEach {
            it.deleteRecursively()
        }
    }
    
    try { delete() } catch(_: IOException) {}
}

fun PlatformFile.sizeRecursively(): Long {
    if(!exists()) return 0L
    var size = if(isRegularFile()) size() else 0L
    
    if(isDirectory()) {
        list().forEach {
            size += it.sizeRecursively()
        } 
    }
    
    return size
}

fun File.sha256Hash(buffer: Int = 1024): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(buffer)

    FileInputStream(this).use { fis ->
        var bytesRead: Int
        while (fis.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
    }
    
    return digest.digest().joinToString("") { "%02x".format(it) }
}