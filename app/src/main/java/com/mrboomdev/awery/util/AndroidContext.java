package com.mrboomdev.awery.util;

import android.content.ContextWrapper;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.sdk.util.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AndroidContext extends ContextWrapper implements Context {

	public AndroidContext(android.content.Context base) {
		super(base);
	}

	@Override
	public String resolveString(String string) {
		try {
			var field = R.string.class.getDeclaredField(string);
			return (String) field.get(field);
		} catch(NoSuchFieldException | IllegalAccessException e) {
			return null;
		}
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