package com.mrboomdev.awery.ui.utils

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember

private val requests = mutableStateMapOf<ResultReceiverRequest<*>, MutableList<ResultRequester<*>>>()

class ResultReceiverRequest<T>

interface ResultRequester<T> {
	var result: T?
	val request: ResultReceiverRequest<T>
	fun request()
}

@Composable
fun <T> rememberResultRequests(request: ResultReceiverRequest<T>): List<ResultRequester<T>> {
	return derivedStateOf {
		@Suppress("UNCHECKED_CAST")
		requests[request] as List<ResultRequester<T>>?
	}.value ?: emptyList()
}

@Composable
fun <T> rememberResultRequester(request: ResultReceiverRequest<T>): ResultRequester<T> {
	val requester = remember(request) {
		object : ResultRequester<T> {
			override var result: T? = null
			override val request = request
			
			override fun request() {
				requests[request]?.add(this)
			}
		}
	}
	
	DisposableEffect(request) {
		onDispose { 
			requests[request]?.remove(requester)
		}
	}
	
	return requester
}

private val sampleScreenRequest = ResultReceiverRequest<String>()

@Composable
private fun SampleScreenA() {
	val resultRequester = rememberResultRequester(sampleScreenRequest)
	
	Button(onClick = {
		resultRequester.request()
	}) {
		Text("Request result")
	}
	
	resultRequester.result?.let {
		Text("Result is $it")
	}
}

@Composable
private fun SampleScreenB() {
	val requests = rememberResultRequests(sampleScreenRequest)
	
	Button(onClick = {
		for(request in requests) {
			request.result = "An result!"
		}
	}) {
		Text("Send result")
	}
}