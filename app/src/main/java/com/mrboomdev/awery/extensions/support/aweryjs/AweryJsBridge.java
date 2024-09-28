package com.mrboomdev.awery.extensions.support.aweryjs;

import static com.mrboomdev.awery.util.NiceUtils.requireArgument;

import androidx.annotation.NonNull;

import com.caoccao.javet.annotations.V8Function;
import com.caoccao.javet.annotations.V8Property;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.caoccao.javet.values.reference.V8ValuePromise;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.io.HttpClient;
import com.mrboomdev.awery.util.io.HttpMethod;
import com.mrboomdev.awery.util.io.HttpRequest;
import com.mrboomdev.awery.util.io.HttpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class AweryJsBridge {
	protected final List<AweryJsProvider> providers = new ArrayList<>();
	protected Map<String, ?> jsManifest;
	protected boolean done;
	private final AweryJsManager manager;
	private Extension extension;

	public AweryJsBridge(AweryJsManager manager) {
		this.manager = manager;
	}

	@V8Function
	public void setManifest(Map<String, ?> manifest) {
		requireArgument(manifest, "manifest");

		if(jsManifest != null) {
			throw new IllegalStateException("You've already set an manifest! It can't be changed!");
		}

		this.jsManifest = manifest;
	}

	@V8Function
	public void registerProvider(Map<String, ?> provider) {
		requireArgument(provider, "provider");

		if(jsManifest == null) {
			throw new IllegalStateException("You have to set an manifest before registering any providers!");
		}

		if(done) {
			throw new IllegalStateException("You can't add more providers after initialization finished!");
		}

		providers.add(new AweryJsProvider(manager, provider));
	}

	@V8Function
	public V8ValuePromise fetch(String url, @NonNull V8ValueObject fetchParams) throws JavetException {
		requireArgument(url, "url");

		try(var promise = manager.getJsRuntime().createV8ValuePromise()) {
			var request = new HttpRequest(url);

			var method = fetchParams.get("method").asString();
			request.setMethod(method != null ? HttpMethod.valueOf(method) : HttpMethod.GET);

			request.setForm(fetchParams.getObject("form"));
			request.setHeaders(fetchParams.getObject("headers"));

			HttpClient.fetch(request).addCallback(new AsyncFuture.Callback<>() {
				@Override
				public void onSuccess(HttpResponse result) {
					try {
						promise.resolve(result);
					} catch(JavetException e) {
						onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable t) {
					try {
						promise.reject(t);
					} catch(JavetException e) {
						throw new RuntimeException(e);
					}
				}
			});

			return promise;
		}
	}

	@V8Property
	public String getAdultMode() {
		var mode = AwerySettings.ADULT_MODE.getValue();

		if(mode == null) {
			mode = AwerySettings.AdultMode_Values.SAFE;
		}

		return mode.name();
	}
}