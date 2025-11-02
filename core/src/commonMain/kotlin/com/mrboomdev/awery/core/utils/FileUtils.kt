package com.mrboomdev.awery.core.utils

import io.github.vinceglb.filekit.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest

/**
 * Converts this [PlatformFile] to a [File].
 *
 * @return A [File] representing this file.
 */
expect fun PlatformFile.toJavaFile(): File

/**
 * Converts this [File] to a [PlatformFile].
 *
 * @return A [PlatformFile] representing this file.
 */
fun File.toPlatformFile() = PlatformFile(absolutePath)

/**
 * Opens an input stream to this file.
 *
 * If this file does not exist, or if an IO exception occurs while opening the file,
 * an IOException is thrown.
 *
 * @return An input stream to this file.
 * @throws IOException If this file does not exist, or if an IO exception occurs while opening the file.
 */
expect fun PlatformFile.openInputStream(): InputStream

/**
 * Deletes this file or directory recursively.
 *
 * If this file does not exist, this operation does nothing.
 * If this file is a regular file, it is deleted.
 * If this file is a directory, all of its files and subdirectories are deleted recursively.
 *
 * Note that this operation blocks the calling thread until it is complete.
 *
 * @throws IOException If an IO exception occurs while deleting files or directories.
 */
suspend fun PlatformFile.deleteRecursively() {
    if(!exists()) return
    
    if(isDirectory()) {
        list().forEach {
            it.deleteRecursively()
        }
    }
    
    try { delete() } catch(_: IOException) {}
}

/**
 * Recursively computes the total size of this file or directory.
 *
 * If this file does not exist, returns 0L.
 * If this file is a regular file, returns the size of this file.
 * If this file is a directory, computes the total size of all files and subdirectories within it.
 *
 * @return The total size of this file or directory, in bytes.
 */
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

/**
 * Computes the SHA-256 hash of the contents of this file
 *
 * @param buffer the size of the buffer to use when reading the file
 * @return the SHA-256 hash of the contents of this file as a hexadecimal string
 */
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