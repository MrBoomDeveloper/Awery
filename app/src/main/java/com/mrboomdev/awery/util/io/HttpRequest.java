package com.mrboomdev.awery.util.io;

import static com.mrboomdev.awery.util.NiceUtils.isUrlValid;

import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;

public class HttpRequest {
	private FormBody.Builder form;
	private MediaType mediaType;
	private Map<String, String> headers;
	private HttpCacheMode cacheMode;
	private HttpMethod method;
	private String url, body;
	private int cacheTime;

	public HttpRequest(String url) {
		this.url = url;
	}

	public HttpRequest() {}

	public HttpRequest setMethod(HttpMethod method) {
		this.method = method;
		return this;
	}

	public HttpRequest setUrl(String url) {
		this.url = url;
		return this;
	}

	public HttpRequest setCache(HttpCacheMode cacheMode, int duration) {
		this.cacheTime = duration;
		this.cacheMode = cacheMode;
		return this;
	}

	public HttpRequest setForm(Map<String, String> map) {
		if(map == null) {
			this.form = null;
			return this;
		}

		this.form = new FormBody.Builder();

		for(var entry : map.entrySet()) {
			form.add(entry.getKey(), entry.getValue());
		}

		return this;
	}

	public HttpRequest addFormField(String key, String value) {
		if(form == null) {
			form = new FormBody.Builder();
		}

		form.add(key, value);
		return this;
	}

	public HttpRequest setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public HttpRequest addHeader(String key, String value) {
		if(headers == null) {
			headers = new HashMap<>();
		}

		headers.put(key, value);
		return this;
	}

	public HttpRequest setBody(String body, String contentType) {
		if(body == null) {
			this.body = null;
			this.mediaType = null;
			return this;
		}

		this.body = body;
		this.mediaType = MediaType.parse(contentType);
		return this;
	}

	public String getBody() {
		return body;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getUrl() {
		return url;
	}

	public HttpCacheMode getCacheMode() {
		return cacheMode;
	}

	public int getCacheDuration() {
		return cacheTime;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public FormBody.Builder getForm() {
		return form;
	}

	/**
	 * This method will be ran before any requests to check if all params are valid.
	 */
	protected void checkFields() {
		if(url == null) {
			throw new IllegalArgumentException("Url must be set!");
		}

		if(!isUrlValid(url)) {
			throw new IllegalArgumentException("Invalid url! " + url);
		}

		if(method == null) {
			method = (body == null) ? HttpMethod.GET : HttpMethod.POST;
		}

		if(form == null && body == null && method.doSendData()) {
			throw new IllegalArgumentException("Body or form must be set for method " + method.name() +  "!");
		}

		if(form != null && body != null) {
			throw new IllegalArgumentException("Body and form cannot be set at the same time!");
		}
	}
}