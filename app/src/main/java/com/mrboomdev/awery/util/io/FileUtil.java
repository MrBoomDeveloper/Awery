package com.mrboomdev.awery.util.io;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileUtil {

	public static boolean deleteFile(File file) {
		if(file == null) return false;

		if(file.isDirectory()) {
			var children = file.listFiles();
			if(children == null) return false;

			for(var child : children) {
				deleteFile(child);
			}
		}

		file.delete();
		return true;
	}

	public static long getFileSize(@NonNull File file) {
		if(file.isDirectory()) {
			var children = file.listFiles();
			if(children == null) return 0;

			long totalSize = 0;

			for(var child : children) {
				totalSize += getFileSize(child);
			}

			return totalSize;
		}

		return file.length();
	}

	@NonNull
	public static String readAssets(@NonNull File file) throws IOException {
		var path = file.getAbsolutePath().substring(1);

		try(var reader = new BufferedReader(new InputStreamReader(
				getAnyContext().getAssets().open(path), StandardCharsets.UTF_8))
		) {
			var builder = new StringBuilder();
			String line;

			while((line = reader.readLine()) != null) {
				builder.append(line);
			}

			return builder.toString();
		}
	}
}