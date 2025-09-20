package com.mrboomdev.awery.app

import io.github.vinceglb.filekit.FileKit

internal actual fun platformInit() {
    FileKit.init("com.mrboomdev.awery")
}