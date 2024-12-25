package com.mrboomdev.awery.utils

import java.io.File

operator fun File.div(child: String) = File(this, child)