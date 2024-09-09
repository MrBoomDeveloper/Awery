package com.mrboomdev.awery.extensions.support.aweryjs;

import static java.util.Objects.requireNonNull;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.caoccao.javet.annotations.V8Function;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.utils.V8ValueUtils;
import com.caoccao.javet.values.V8Value;

public class AweryJsConsole {
	private static final String TAG = "AweryJsConsole";
	@Nullable
	private final AweryJsBridge bridge;

	public AweryJsConsole() {
		this.bridge = null;
	}

	public AweryJsConsole(@Nullable AweryJsBridge bridge) {
		this.bridge = bridge;
	}

	@NonNull
	private String concat(V8Value... v8Values) throws JavetException {
		return "[ " + getPrefix() + " ] " + V8ValueUtils.concat(" ", v8Values);
	}

	@NonNull
	private String getPrefix() throws JavetException {
		if(bridge == null || bridge.jsManifest == null) {
			return "Global Context";
		}

		var title = bridge.jsManifest.getString("title");
		var id = bridge.jsManifest.getString("id");

		if(title != null) {
			return title + ":" + id;
		}

		return requireNonNull(id);
	}

	@V8Function
	public void debug(V8Value... v8Values) throws JavetException {
		Log.d(TAG, concat(v8Values));
	}

	@V8Function
	public void error(V8Value... v8Values) throws JavetException {
		Log.e(TAG, concat(v8Values));
	}

	@V8Function
	public void info(V8Value... v8Values) throws JavetException {
		Log.i(TAG, concat(v8Values));
	}

	@V8Function
	public void log(V8Value... v8Values) throws JavetException {
		Log.v(TAG, concat(v8Values));
	}

	@V8Function
	public void trace(V8Value... v8Values) throws JavetException {
		Log.d(TAG, concat(v8Values));
	}

	@V8Function
	public void warn(V8Value... v8Values) throws JavetException {
		Log.w(TAG, concat(v8Values));
	}
}