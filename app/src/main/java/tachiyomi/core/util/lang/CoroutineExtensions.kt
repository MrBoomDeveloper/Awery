package tachiyomi.core.util.lang

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun <T> withIOContext(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.IO, block)

@OptIn(DelicateCoroutinesApi::class)
fun launchUI(block: suspend CoroutineScope.() -> Unit) = GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT, block)