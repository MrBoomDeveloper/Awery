package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.fromJs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MimeTypes;

import com.mrboomdev.awery.sdk.util.Callbacks;

import org.jetbrains.annotations.Contract;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java9.util.stream.Stream;
import java9.util.stream.StreamSupport;

/**
 * Just a collection of useful simple utility methods
 * @author MrBoomDev
 */
public class NiceUtils {
	private static final String[] REMOVE_LAST_URL_CHARS = { "/", "?", "#", "&", " " };

	/**
	 * @return The result of the callback if the param is not null
	 * @author MrBoomDev
	 */
	public static <A, B> A returnIfNotNull(B param, Callbacks.Result1<A, B> callback) {
		return param == null ? null : callback.run(param);
	}

	public static <A> void doIfNotNull(A param, Callbacks.Callback1<A> callback) {
		if(param != null) {
			callback.run(param);
		}
	}

	public static boolean isTrue(Object bool) {
		if(bool instanceof String s) {
			return Boolean.TRUE.equals(Boolean.parseBoolean(s));
		}

		return Boolean.TRUE.equals(bool);
	}

	@NonNull
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

	public static <A> A findRoot(A item, @NonNull Callbacks.Result1<A, A> callback) {
		var parent = callback.run(item);
		if(parent == null) return item;

		return findRoot(parent, callback);
	}

	@SuppressLint("Range")
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

	@Nullable
	public static Object invokeMethod(String className, String methodName) {
		try {
			var clazz = Class.forName(className);
			var method = clazz.getMethod(methodName);
			method.setAccessible(true);
			return method.invoke(null);
		} catch(ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			return null;
		}
	}

	@Nullable
	public static Object getField(String className, String fieldName) {
		try {
			var clazz = Class.forName(className);
			var field = clazz.getField(fieldName);
			field.setAccessible(true);
			return field.get(null);
		} catch(ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
			return null;
		}
	}

	@NonNull
	@Contract(pure = true)
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

	@Nullable
	public static Object getField(Object target, String className, String fieldName) {
		try {
			var clazz = Class.forName(className);
			var field = clazz.getField(fieldName);
			field.setAccessible(true);
			return field.get(target);
		} catch(ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
			return null;
		}
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

	public static <A> A find(A[] array, @NonNull Callbacks.Result1<Boolean, A> checker) {
		return find(Arrays.asList(array), checker);
	}

	@Nullable
	public static <A> Integer findIndex(A[] array, @NonNull Callbacks.Result1<Boolean, A> checker) {
		return findIndex(Arrays.asList(array), checker);
	}

	@Contract(pure = true)
	@Nullable
	public static <A> Integer findIndex(@NonNull Collection<A> collection, @NonNull Callbacks.Result1<Boolean, A> checker) {
		int index = -1;

		for(var item : collection) {
			index++;

			if(checker.run(item)) {
				return index;
			}
		}

		return null;
	}

	public static <A> A find(Collection<A> collection, @NonNull Callbacks.Result1<Boolean, A> checker) {
		return stream(collection)
				.filter(checker::run)
				.findAny().orElse(null);
	}

	public static <A, B> B findMap(Collection<A> collection, @NonNull Callbacks.Result1<B, A> checker) {
		return stream(collection)
				.map(checker::run)
				.filter(Objects::nonNull)
				.findAny().orElse(null);
	}

	public static <A> boolean hasAny(Collection<A> collection, Callbacks.Result1<Boolean, A> checker) {
		return find(collection, checker) != null;
	}

	/**
	 * @return The result of the callback
	 * @author MrBoomDev
	 */
	public static <A, B> A returnWith(B object, @NonNull Callbacks.Result1<A, B> callback) {
		return callback.run(object);
	}

	public static <A> A returnWith(@NonNull Callbacks.Result<A> callback) {
		return callback.run();
	}

	@NonNull
	@Contract("null, _ -> fail")
	public static <T> T requireArgument(T o, String name) {
		if(o == null) {
			throw new NullPointerException("An required argument \"" + name + "\" was not specified!");
		}

		return o;
	}

	public static <T> T requireArgument(Intent intent, String name, Class<T> clazz) {
		Object result;

		if(clazz == String.class) result = intent.getStringExtra(name);
		else if(clazz == Integer.class) result = intent.getIntExtra(name, 0);
		else if(clazz == Boolean.class) result = intent.getBooleanExtra(name, false);
		else if(clazz == Long.class) result = intent.getLongExtra(name, 0);
		else if(clazz == Float.class) result = intent.getFloatExtra(name, 0);
		else result = intent.getSerializableExtra("name");

		return clazz.cast(requireArgument(result, name));
	}

	public static <T> T requireArgument(@NonNull ScriptableObject o, String name, Class<T> type) {
		var val = fromJs(o.get(name, o), type);
		requireArgument(val, name);
		return val;
	}

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
	@SuppressWarnings("unchecked")
	public static <T> T requireArgument(
			@NonNull Fragment fragment,
			String name
	) throws ClassCastException, NullPointerException {
		var o = (T) fragment.requireArguments().getSerializable(name);

		requireArgument(o, name);
		return o;
	}

	@NonNull
	public static String formatNumber(Number number) {
		if(number == null) return "";

		if(number.floatValue() == number.longValue()) {
			return String.valueOf(number.longValue());
		}

		return String.valueOf(number);
	}

	public static <A> void with(A a, @NonNull Callbacks.Callback1<A> callback) {
		callback.run(a);
	}

	/**
	 * @throws NullPointerException if object is null
	 * @return The object itself
	 * @author MrBoomDev
	 */
	@NonNull
	@Contract("null -> fail; !null -> param1")
	public static <T> T requireNonNull(T object) {
		if(object == null) throw new NullPointerException();
		return object;
	}

	/**
	 * @return The first object if it is not null, otherwise the second object
	 * @author MrBoomDev
	 */
	public static <T> T requireNonNullElse(T firstObject, T secondObject) {
		return firstObject != null ? firstObject : secondObject;
	}

	/**
	 * @return True if the object is not null
	 */
	public static boolean nonNull(Object obj) {
		return obj != null;
	}

	/**
	 * @return A stream from the collection compatible with old Androids
	 * @author MrBoomDev
	 */
	@NonNull
	@Contract("_ -> new")
	public static <E> Stream<E> stream(Collection<E> e) {
		if(e == null) throw new NullPointerException("Collection cannot be null!");
		return StreamSupport.stream(e);
	}

	/**
	 * @return A stream from the array compatible with old Androids
	 * @author MrBoomDev
	 */
	@SafeVarargs
	@NonNull
	@Contract("_ -> new")
	public static <E> Stream<E> stream(E... e) {
		return StreamSupport.stream(Arrays.asList(e));
	}

	/**
	 * @return A stream from map entries set compatible with old Androids
	 * @author MrBoomDev
	 */
	@NonNull
	@Contract("_ -> new")
	public static <K, V> Stream<Map.Entry<K,V>> stream(@NonNull Map<K, V> map) {
		return StreamSupport.stream(map.entrySet());
	}
}