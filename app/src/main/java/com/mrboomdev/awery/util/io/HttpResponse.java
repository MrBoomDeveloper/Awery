package com.mrboomdev.awery.util.io;

import androidx.annotation.NonNull;

import com.caoccao.javet.annotations.V8Function;
import com.caoccao.javet.annotations.V8Property;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueFunction;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.support.aweryjs.AweryJsManager;

import java.io.IOException;

import kotlin.UninitializedPropertyAccessException;
import okhttp3.Response;

public class HttpResponse {
	private final int code;
	private final boolean ok;
	private RuntimeException e;
	private String text;

	protected HttpResponse(@NonNull Response response) {
		this.code = response.code();
		this.ok = response.isSuccessful();

		try {
			this.text = response.body().string();
		} catch(IOException e) {
			this.e = new RuntimeException(e);
		}
	}

	@V8Property(name = "ok")
	public boolean isOk() {
		return ok;
	}

	/**
	 * Please, don't use directly in the Java project!
	 */
	@SuppressWarnings("unused")
	@V8Function(name = "json")
	public V8Value __parseJson() throws JavetException {
		var factory = ExtensionsFactory.getInstanceNow();

		if(factory == null) {
			throw new UninitializedPropertyAccessException("ExtensionsFactory isn't initialized!");
		}

		var runtime = factory.getManager(AweryJsManager.class).jsRuntime.get();
		var parser = (V8ValueFunction) runtime.getExecutor("JSON.parse").execute();
		return parser.call(null, getText().trim());
	}

	@V8Function(name = "text")
	public String getText() {
		if(e != null) {
			throw e;
		}

		return text;
	}

	@V8Property(name = "status")
	public int getStatusCode() {
		return code;
	}

	@NonNull
	@Override
	public String toString() {
		return """
				{
					"text": "__TEXT__",
					"status": __STATUS_CODE__
				}
				"""
				.replace("__TEXT__", getText())
				.replace("__STATUS_CODE__", String.valueOf(getStatusCode()));
	}
}