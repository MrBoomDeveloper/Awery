package com.mrboomdev.awery.extensions.support.aweryjs;

import com.caoccao.javet.annotations.V8Function;
import com.caoccao.javet.values.V8Value;
import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.app.App;

public class AweryJsGlobalBridge {

	@V8Function
	public void toast(V8Value text, int duration) {
		if(BuildConfig.DEBUG) {
			App.toast(text.toString(), duration);
		}
	}

	@V8Function
	public void toast(V8Value text) {
		toast(text, 0);
	}
}