package com.mrboomdev.awery.util.exceptions;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.extensions.Extension;
import com.mrboomdev.awery.catalog.extensions.ExtensionsFactory;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.Json;
import com.squareup.moshi.ToJson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ExceptionDetails {
	@Json(name = "installed_extensions")
	private final List<Extension> installedExtensions;
	private final Throwable exception;
	@Json(ignore = true)
	private final Context context;
	@Json(name = "did_checked")
	private boolean didChecked;

	public ExceptionDetails(Context context, Throwable exception) {
		List<Extension> extensions;
		this.context = context;
		this.exception = exception;

		try {
			var extensionsCollection = ExtensionsFactory.getExtensions(Extension.FLAG_ANY_STATUS);
			extensions = new ArrayList<>(extensionsCollection);
		} catch(Throwable e) {
			extensions = List.of(new Extension("null",
					"Failed to retrieve a list of installed extensions", "0"));
		}

		this.installedExtensions = extensions;
	}

	public boolean didChecked() {
		return didChecked;
	}

	public Throwable getException() {
		return exception;
	}

	public void setDidChecked(boolean didChecked) {
		this.didChecked = didChecked;
	}

	public static class Adapter {

		@ToJson
		public String toJson(Throwable serializable) throws IOException {
			try(var arrayStream = new ByteArrayOutputStream(); var outputStream = new ObjectOutputStream(arrayStream)) {
				outputStream.writeObject(serializable);
				return Base64.encodeToString(arrayStream.toByteArray(), Base64.DEFAULT);
			}
		}

		@FromJson
		public Throwable fromJson(@NonNull String string) throws IOException, ClassNotFoundException {
			var data = Base64.decode(string, Base64.DEFAULT);

			try(var stream = new ObjectInputStream(new ByteArrayInputStream(data))) {
				return (Throwable) stream.readObject();
			}
		}
	}
}