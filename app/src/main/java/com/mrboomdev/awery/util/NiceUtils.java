package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MimeTypes;

import com.mrboomdev.awery.util.io.FileUtil;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import java9.util.stream.Stream;
import java9.util.stream.StreamSupport;

/**
 * Just a collection of useful simple utility methods
 * @author MrBoomDev
 */
public class NiceUtils {
	private static final UniqueIdGenerator fileIdGenerator = new UniqueIdGenerator();
	@Deprecated(forRemoval = true)
	private static final String[] REMOVE_LAST_URL_CHARS = { "/", "?", "#", "&", " " };

	public static <T extends Enum<T>> T parseEnum(String string, @NotNull T defaultValue) {
		if(string == null || string.isBlank()) {
			return null;
		}

		try {
			return Enum.valueOf(defaultValue.getDeclaringClass(), string);
		} catch(IllegalArgumentException e) {
			return defaultValue;
		}
	}
	
	public static <T extends Enum<T>> T parseEnum(String string, @NotNull Class<T> clazz) {
		if(string == null || string.isBlank()) {
			return null;
		}
		
		try {
			return Enum.valueOf(clazz, string);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}

	@NonNull
	public static File getTempFile() {
		var root = getAnyContext().getCacheDir();
		var file = new File(root, "temp/" + fileIdGenerator.getLong());

		if(file.exists()) {
			FileUtil.deleteFile(file);
		} else {
			requireNonNull(file.getParentFile()).mkdirs();
		}

		return file;
	}

	@Deprecated(forRemoval = true)
	public interface Callback1<A> {
		void run(A arg);
	}

	@Deprecated(forRemoval = true)
	public static <A> void doIfNotNull(A param, Callback1<A> callback) {
		doIfNotNull(param, callback, null);
	}

	@Deprecated(forRemoval = true)
	public static <A> void doIfNotNull(A param, Callback1<A> callback, Runnable ifNull) {
		if(param != null) {
			if(callback != null) callback.run(param);
		} else if(ifNull != null) {
			ifNull.run();
		}
	}

	public static int compareVersions(@NonNull long[] first, @NonNull long[] second) {
		int i = 0;

		while(true) {
			long firstArg = first.length > i ? first[i] : -1;
			long secondArg = second.length > i ? second[i] : -1;

			if(firstArg == -1 && secondArg == -1) {
				return 0;
			}

			if(firstArg == -1) firstArg = 0;
			if(secondArg == -1) secondArg = 0;

			if(firstArg > secondArg) {
				return 1;
			}

			if(firstArg < secondArg) {
				return -1;
			}

			i++;
		}
	}

	@NonNull
	public static long[] parseVersion(String string) {
		List<Long> list = new ArrayList<>();
		string = string.trim();

		StringBuilder builder = null;
		int nonNumbersCount = 0;

		for(var character : string.toCharArray()) {
			if(Character.isDigit(character)) {
				if(builder == null) {
					builder = new StringBuilder();
				}

				builder.append(character);
				nonNumbersCount = 0;
			} else {
				if(builder != null) {
					list.add(Long.valueOf(builder.toString()));
					builder = null;
				} else {
					if(!list.isEmpty() && ++nonNumbersCount > 1) {
						break;
					}
				}
			}
		}

		if(builder != null) {
			list.add(Long.valueOf(builder.toString()));
		}

		var result = new long[list.size()];

		for(int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}

		return result;
	}

	@Deprecated(forRemoval = true)
	public static boolean isTrue(Object bool) {
		if(bool instanceof String s) {
			return Boolean.TRUE.equals(Boolean.parseBoolean(s));
		}

		return Boolean.TRUE.equals(bool);
	}

	@NonNull
	@Deprecated(forRemoval = true)
	public static String cleanUrl(String url) {
		url = requireArgument(url, "url").trim();

		loop:
		while(true) {
			for(var character : REMOVE_LAST_URL_CHARS) {
				if(url.endsWith(character)) {
					url = url.substring(0, url.length() - 1);
					continue loop;
				}
			}

			break;
		}

		return url;
	}

	@Contract("null -> false")
	@Deprecated(forRemoval = true)
	public static boolean isUrlValid(String url) {
		if(url == null || url.isBlank()) {
			return false;
		}

		try {
			new URL(url).toURI();
			return true;
		} catch(URISyntaxException | MalformedURLException e) {
			return false;
		}
	}
	
	/**
	 * Converts list to unique string.
	 * Used format: ";;;value1;;;value2;;;value3;;;"
	 * @see #uniqueStringToList(String)
	 */
	@NotNull
	public static String listToUniqueString(@NotNull Iterable<String> iterable) {
		var builder = new StringBuilder(";;;");
		
		for(var string : iterable) {
			builder.append(string).append(";;;");
		}
		
		return builder.toString();
	}
	
	/**
	 * Converts unique string to list.
	 * @see #listToUniqueString(Iterable)
	 * @param uniqueString - String of format ";;;value1;;;value2;;;value3;;;";;;
	 */
	@NotNull
	public static @Unmodifiable List<String> uniqueStringToList(@NotNull String uniqueString) {
		if(uniqueString.length() <= 3) return Collections.emptyList();
		return List.of(uniqueString.substring(3, uniqueString.length() - 3).split(";;;"));
	}

	@SuppressLint("Range")
	@Deprecated(forRemoval = true)
	public static String parseMimeType(Object o) {
		String fileName;

		if(o instanceof Uri uri) {
			if(Objects.equals(uri.getScheme(), "content")) {
				try(var cursor = getAnyContext().getContentResolver().query(
						uri, null, null, null, null)
				) {
					if(cursor == null) {
						throw new NullPointerException("Cursor is null!");
					}

					cursor.moveToFirst();
					fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
				}
			} else {
				fileName = uri.getLastPathSegment();

				if(fileName == null) {
					fileName = uri.toString();
					fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
				}
			}
		} else if(o instanceof File file) {
			fileName = file.getName();
		} else {
			fileName = new File(o.toString()).getName();
		}

		if(fileName.contains("#")) {
			fileName = fileName.substring(0, fileName.indexOf("#"));
		}

		if(fileName.contains("?")) {
			fileName = fileName.substring(0, fileName.indexOf("?"));
		}

		if(fileName.contains("/")) {
			fileName = fileName.substring(0, fileName.indexOf("/"));
		}

		var ext = fileName.substring(fileName.lastIndexOf(".") + 1);

		return switch(ext) {
			case "vtt" -> MimeTypes.TEXT_VTT;
			case "srt" -> MimeTypes.APPLICATION_SUBRIP;
			case "scc" -> MimeTypes.APPLICATION_CEA708;
			case "ts" -> MimeTypes.APPLICATION_DVBSUBS;
			case "mka" -> MimeTypes.APPLICATION_MATROSKA;
			case "wvtt" -> MimeTypes.APPLICATION_MP4VTT;
			case "pgs" -> MimeTypes.APPLICATION_PGS;
			case "rtsp" -> MimeTypes.APPLICATION_RTSP;
			case "ass", "ssa" -> MimeTypes.APPLICATION_SS;
			case "ttml", "xml", "dfxp" -> MimeTypes.APPLICATION_TTML;
			case "tx3g" -> MimeTypes.APPLICATION_TX3G;
			case "idx", "sub" -> MimeTypes.APPLICATION_VOBSUB;
			default -> throw new IllegalArgumentException("Unknown mime type! " + fileName);
		};
	}
	
	@NotNull
	public static Date parseDate(String string) {
		try {
			var formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
			formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
			return Objects.requireNonNull(formatter.parse(string));
		} catch(Exception e) {
			throw new IllegalArgumentException("Failed to parse date: " + string, e);
		}
	}
	
	/**
	 * Converts long to string in format: 24:00.
	 */
	@NotNull
	public static String formatClock(long value) {
		if(value < 0) {
			return "00:00";
		}
		
		value /= 1000;
		
		var hours = (int) value / 3600;
		var days = hours / 24;
		
		if(days >= 1) {
			return String.format(Locale.ENGLISH, "%dd %02d:%02d:%02d",
					days, hours % 24, (int) value / 60, (int) value % 60);
		}
		
		if(hours >= 1) {
			return String.format(Locale.ENGLISH, "%02d:%02d:%02d",
					hours, (int) value / 60, (int) value % 60);
		}
		
		return String.format(Locale.ENGLISH, "%02d:%02d",
				(int) value / 60, (int) value % 60);
	}
	
	@NotNull
	public static String formatTimer(long value) {
		if(value <= 0) {
			return "0s";
		}
		
		value /= 1000;
		
		var seconds = (int) value % 60;
		var minutes = (int) value / 60;
		
		if(minutes >= 60) {
			if(seconds == 0) {
				return String.format(Locale.ENGLISH, "%dh", minutes / 60);
			}
			
			return String.format(Locale.ENGLISH, "%dh %02d:%02d",
					minutes / 60, minutes % 60, seconds);
		}
		
		if(minutes >= 1) {
			if(seconds == 0) {
				return String.format(Locale.ENGLISH, "%dm", minutes);
			}
			
			return String.format(Locale.ENGLISH, "%dm %02ds",
					minutes, seconds);
		}
		
		return String.format(Locale.ENGLISH, "%ds", seconds);
	}

	@NonNull
	@Contract(pure = true)
	@Deprecated(forRemoval = true)
	public static String formatFileSize(long size) {
		var BORDER = 1000 / 1024;

		var kb = size / 1024;
		var mb = kb / 1024;
		var gb = mb / 1024;

		if(gb > BORDER) return gb + " gb";
		if(mb > BORDER) return mb + " mb";
		if(kb > BORDER) return kb + " kb";

		return gb + " b";
	}

	@NonNull
	public static String cleanString(@NonNull String string) {
		var builder = new StringBuilder();

		var lines = string.split("\n");
		var iterator = List.of(lines).iterator();

		while(iterator.hasNext()) {
			var line = iterator.next();

			builder.append(line.trim());

			if(iterator.hasNext()) {
				builder.append("\n");
			}
		}

		return builder.toString().trim();
	}

	@Deprecated(forRemoval = true)
	public interface Result1<A, B> {
		A run(B arg);
	}

	@Deprecated(forRemoval = true)
	public static <A> A find(A[] array, @NonNull Result1<Boolean, A> checker) {
		return find(Arrays.asList(array), checker);
	}

	@Nullable
	@Deprecated(forRemoval = true)
	public static <A> Integer findIndex(A[] array, @NonNull Result1<Boolean, A> checker) {
		return findIndex(Arrays.asList(array), checker);
	}

	@Contract(pure = true)
	@Nullable
	@Deprecated(forRemoval = true)
	public static <A> Integer findIndex(@NonNull Collection<A> collection, @NonNull Result1<Boolean, A> checker) {
		int index = -1;

		for(var item : collection) {
			index++;

			if(checker.run(item)) {
				return index;
			}
		}

		return null;
	}

	@Deprecated(forRemoval = true)
	public static <A> A find(Collection<A> collection, @NonNull Result1<Boolean, A> checker) {
		return stream(collection)
				.filter(checker::run)
				.findAny().orElse(null);
	}

	@Deprecated(forRemoval = true)
	public static <A, B> B findMap(Collection<A> collection, @NonNull Result1<B, A> checker) {
		return stream(collection)
				.map(checker::run)
				.filter(Objects::nonNull)
				.findAny().orElse(null);
	}

	@Deprecated(forRemoval = true)
	public static <A> boolean hasAny(Collection<A> collection, Result1<Boolean, A> checker) {
		return find(collection, checker) != null;
	}

	/**
	 * @return The result of the callback
	 * @author MrBoomDev
	 */
	@Deprecated(forRemoval = true)
	public static <A, B> A returnWith(B object, @NonNull Result1<A, B> callback) {
		return callback.run(object);
	}

	@Deprecated(forRemoval = true)
	public interface Result<A> {
		A run();
	}

	@Deprecated(forRemoval = true)
	public static <A> A returnWith(@NonNull Result<A> callback) {
		return callback.run();
	}
	
	@NonNull
	@Contract("null, _ -> fail")
	@Deprecated(forRemoval = true)
	public static <T> T requireArgument(T o, String name) throws NullPointerException {
		if(o == null) {
			throw new NullPointerException("An required argument \"" + name + "\" was not specified!");
		}

		return o;
	}

	@Deprecated(forRemoval = true)
	public static <T> T requireArgument(@NonNull Activity activity, String name, Class<T> clazz) throws NullPointerException {
		return requireArgument(activity.getIntent(), name, clazz);
	}

	@Deprecated(forRemoval = true)
	public static <T> T requireArgument(Intent intent, String name, @NonNull Class<T> clazz) throws NullPointerException {
		return clazz.cast(requireArgument(getArgument(intent, name, clazz), name));
	}

	@Deprecated(forRemoval = true)
	public static <T> T getArgument(Intent intent, String name, Class<T> clazz) {
		if(intent == null) return null;
		Object result;

		if(clazz == String.class) result = intent.getStringExtra(name);
		else if(clazz == Integer.class) result = intent.getIntExtra(name, 0);
		else if(clazz == Boolean.class) result = intent.getBooleanExtra(name, false);
		else if(clazz == Long.class) result = intent.getLongExtra(name, 0);
		else if(clazz == Float.class) result = intent.getFloatExtra(name, 0);
		else result = intent.getSerializableExtra(name);

		return clazz.cast(result);
	}

	@Deprecated(forRemoval = true)
	public static <T> T getArgument(Bundle bundle, String name, Class<T> clazz) {
		if(bundle == null) return null;
		Object result;

		if(clazz == String.class) result = bundle.getString(name);
		else if(clazz == Integer.class) result = bundle.getInt(name, 0);
		else if(clazz == Boolean.class) result = bundle.getBoolean(name, false);
		else if(clazz == Long.class) result = bundle.getLong(name, 0);
		else if(clazz == Float.class) result = bundle.getFloat(name, 0);
		else result = bundle.getSerializable(name);

		return clazz.cast(result);
	}

	@Deprecated(forRemoval = true)
	@NonNull
	public static <T> T requireArgument(
			@NonNull Fragment fragment,
			String name,
			@NonNull Class<T> type
	) throws ClassCastException, NullPointerException {
		var bareObject = fragment.requireArguments().getSerializable(name);
		var castedObject = type.cast(bareObject);

		requireArgument(castedObject, name);
		return castedObject;
	}

	/**
	 * @param <T> Target type
	 * @throws ClassCastException If argument's class doesn't extend an target class
	 * @throws NullPointerException If argument was not found
	 * @author MrBoomDev
	 */
	@NonNull
	@Deprecated(forRemoval = true)
	@SuppressWarnings("unchecked")
	public static <T> T requireArgument(
			@NonNull Fragment fragment,
			String name
	) throws ClassCastException, NullPointerException {
		var o = (T) fragment.requireArguments().getSerializable(name);

		requireArgument(o, name);
		return o;
	}

	@Deprecated(forRemoval = true)
	public static <A> void with(A a, @NonNull Callback1<A> callback) {
		callback.run(a);
	}

	/**
	 * @throws NullPointerException if object is null
	 * @return The object itself
	 * @author MrBoomDev
	 */
	@NonNull
	@Contract("null -> fail; !null -> param1")
	@Deprecated(forRemoval = true)
	public static <T> T requireNonNull(T object) {
		if(object == null) throw new NullPointerException();
		return object;
	}

	/**
	 * @return The first object if it is not null, otherwise the second object
	 * @author MrBoomDev
	 */
	@Deprecated(forRemoval = true)
	public static <T> T requireNonNullElse(T firstObject, T secondObject) {
		return firstObject != null ? firstObject : secondObject;
	}

	/**
	 * @return True if the object is not null
	 */
	@Deprecated(forRemoval = true)
	public static boolean nonNull(Object obj) {
		return obj != null;
	}

	/**
	 * @return A stream from the collection compatible with old Androids
	 * @author MrBoomDev
	 */
	@NonNull
	@Contract("_ -> new")
	@Deprecated(forRemoval = true)
	public static <E> Stream<E> stream(Collection<E> collection) {
		requireArgument(collection, "collection");
		return StreamSupport.stream(collection);
	}

	/**
	 * @return A stream from the array compatible with old Androids
	 * @author MrBoomDev
	 */
	@SafeVarargs
	@NonNull
	@Contract("_ -> new")
	@Deprecated(forRemoval = true)
	public static <E> Stream<E> stream(E... array) {
		requireArgument(array, "array");
		return StreamSupport.stream(Arrays.asList(array));
	}
}