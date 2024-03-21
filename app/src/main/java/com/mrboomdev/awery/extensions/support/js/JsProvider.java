package com.mrboomdev.awery.extensions.support.js;

import com.mrboomdev.awery.extensions.ExtensionProvider;

import org.mozilla.javascript.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JsProvider extends ExtensionProvider {
	private final File file;

	public JsProvider(Context context, File file) throws IOException {
		StringBuilder script = new StringBuilder();
		this.file = file;

		try(var reader = new BufferedReader(new FileReader(file))) {
			String line;

			while((line = reader.readLine()) != null) {
				script.append(line).append("\n");
			}
		}

		var scope = context.initSafeStandardObjects();
		context.evaluateString(scope, script.toString(), file.getName(), 1,null);
	}

	@Override
	public String getName() {
		return "JsExtensionProvider";
	}
}