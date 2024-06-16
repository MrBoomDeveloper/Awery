package com.mrboomdev.awery.util.io;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.util.NiceUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {
	private static final int BUFFER_SIZE = 1024 * 5;

	public static List<File> getFiles(@NonNull File parent) {
		if(parent.isDirectory()) {
			var children = parent.listFiles();

			if(children != null) {
				if(children.length == 0) {
					return Collections.emptyList();
				}

				return stream(children)
						.map(FileUtil::getFiles)
						.flatMap(NiceUtils::stream)
						.toList();
			}
		}

		return Collections.singletonList(parent);
	}

	/**
	 * @param inputDir All files inside this directory will be added into zip file.
	 */
	public static void zip(@NonNull File inputDir, File outputFile) throws IOException {
		zip(inputDir.getPath(), getFiles(inputDir).toArray(new File[0]), outputFile);
	}

	/**
	 * @param inputFiles All these files will be added into zip file.
	 */
	public static void zip(@NonNull File[] inputFiles, File outputFile) throws IOException {
		zip(null, inputFiles, outputFile);
	}

	private static void zip(String relativePath, @NonNull File[] inputFiles, File outputFile) throws IOException {
		try(var out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
			var data = new byte[BUFFER_SIZE];

			for(var file : inputFiles) {
				try(var is = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)) {
					var zipFileName = relativePath == null ? file.getName() :
							file.getPath().substring(relativePath.length());

					var zipEntry = new ZipEntry(zipFileName);
					out.putNextEntry(zipEntry);
					int read;

					while((read = is.read(data, 0, BUFFER_SIZE)) != -1) {
						out.write(data, 0, read);
					}
				}
			}
		}
	}

	public static void unzip(File input, @NonNull File output) throws IOException {
		var buffer = new byte[BUFFER_SIZE];
		output.mkdirs();

		try(var zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(input), BUFFER_SIZE))) {
			ZipEntry ze;

			while((ze = zin.getNextEntry()) != null) {
				var path = new File(output, ze.getName());

				if(ze.isDirectory() && !path.isDirectory()) {
					path.mkdirs();
				} else {
					var parent = path.getParentFile();

					if(parent != null) {
						parent.mkdirs();
					}

					try(var fout = new BufferedOutputStream(new FileOutputStream(path, false), BUFFER_SIZE)) {
						for(int c = zin.read(); c != -1; c = zin.read()) {
							fout.write(c);
						}
					}
				}
			}
		}
	}

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
		return readAssets(file.getAbsolutePath().substring(1));
	}

	@NonNull
	public static String readAssets(String path) throws IOException {
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