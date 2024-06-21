package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MimeTypes;

import com.mrboomdev.awery.sdk.util.Callbacks;

import org.jetbrains.annotations.Contract;

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

	public static <A> A find(Collection<A> collection, @NonNull Callbacks.Result1<Boolean, A> checker) {
		return stream(collection)
				.filter(checker::run)
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