package com.mrboomdev.awery.util;

import android.content.ContextWrapper;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AndroidContext extends ContextWrapper implements com.mrboomdev.awery.sdk.util.Context {

	public AndroidContext(android.content.Context base) {
		super(base);
	}

	@Override
	public InputStream openInputStream(@NonNull File file) throws IOException {
		return openFileInput(file.getAbsolutePath());
	}

	@Override
	public OutputStream openOutputStream(@NonNull File file) throws IOException {
		return openFileOutput(file.getAbsolutePath(), 0);
	}
}