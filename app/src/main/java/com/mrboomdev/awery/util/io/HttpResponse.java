package com.mrboomdev.awery.util.io;

import androidx.annotation.NonNull;

public abstract class HttpResponse {
	public abstract String getText();
	public abstract int getStatusCode();

	@NonNull
	@Override
	public String toString() {
		return """
				{
					"text": "__TEXT__",
					"statusCode": __STATUS_CODE__
				}
				"""
				.replace("__TEXT__", getText())
				.replace("__STATUS_CODE__", String.valueOf(getStatusCode()));
	}
}