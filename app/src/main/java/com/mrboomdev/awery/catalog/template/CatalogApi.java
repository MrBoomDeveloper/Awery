package com.mrboomdev.awery.catalog.template;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.KotlinBridge;

import java.util.HashMap;
import java.util.Map;

public abstract class CatalogApi {

	public String getDomain() {
		return null;
	}

	protected Map<String, String> getBaseHeaders() {
		return new HashMap<>() {{
			put("Content-Type", "application/json");
			put("Accept", "application/json");
		}};
	}

	public <T> void executeMethod(@NonNull CatalogMethod<T> query, CatalogMethod.ResponseCallback<T> callback) {

	}

	public <T> void executeQuery(@NonNull CatalogQuery<T> query, CatalogQuery.ResponseCallback<T> callback) {
		query.executeQuery(callback);
	}

	protected void executeMethodImplementation(CatalogMethod<?> method, CatalogMethod.ResponseCallback<String> callback) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("This method isn't implemented!");
	}

	protected void executeQueryImplementation(@NonNull CatalogQuery<?> query, CatalogQuery.ResponseCallback<String> callback) {
		var data = new HashMap<String, String>() {{
			put("query", query.getQuery());
			put("variables", query.getVariables());
		}};

		var headers = getBaseHeaders();

		KotlinBridge.clientPost(getDomain(), headers, data, 10, response ->
				callback.onResponse(response.getText()));
	}
}