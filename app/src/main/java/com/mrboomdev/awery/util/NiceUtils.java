package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.Lifecycle.getAnyContext;
import static com.mrboomdev.awery.util.ArgUtils.requireArgument;
import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MimeTypes;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.IV8ValueArray;
import com.caoccao.javet.values.reference.V8ValueArray;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.mrboomdev.awery.sdk.util.Callbacks;
import com.mrboomdev.awery.sdk.util.UniqueIdGenerator;
import com.mrboomdev.awery.util.io.FileUtil;

import org.jetbrains.annotations.Contract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
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
	public static final Object EMPTY_OBJECT = new Object();
	private static final UniqueIdGenerator fileIdGenerator = new UniqueIdGenerator();
	private static final String[] REMOVE_LAST_URL_CHARS = { "/", "?", "#", "&", " " };

	public interface TryRunnable<T> {
		T get() throws Throwable;
	}

	public interface CatchRunnable<T> {
		T get(Throwable t);
	}

	public interface ThrowableCreator<T extends Throwable> {
		T create();
	}

	@Contract("_ -> fail")
	public static <T extends Throwable, E> E throwThrowable(@NonNull ThrowableCreator<T> creator) throws T {
		throw creator.create();
	}

	public static <T> T returnTryCatch(TryRunnable<T> tryRunnable, CatchRunnable<T> catchRunnable) {
		try {
			return tryRunnable.get();
		} catch(Throwable t) {
			return catchRunnable.get(t);
		}
	}

	public static String serialize(Serializable serializable) throws IOException {
		try(var byteOs = new ByteArrayOutputStream(); var objectOs = new ObjectOutputStream(byteOs)) {
			objectOs.writeObject(serializable);
			return Base64.encodeToString(byteOs.toByteArray(), 0);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T deserialize(String s) throws IOException, ClassNotFoundException {
		var data = Base64.decode(s, 0);

		try(var objectIs = new ObjectInputStream(new ByteArrayInputStream(data))) {
			return (T) objectIs.readObject();
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

	/**
	 * @return The result of the callback if the param is not null
	 * @author MrBoomDev
	 */
	public static <A, B> A returnIfNotNull(B param, Callbacks.Result1<A, B> callback) {
		return param == null ? null : callback.run(param);
	}

	public static <A> void doIfNotNull(A param, Callbacks.Callback1<A> callback) {
		doIfNotNull(param, callback, null);
	}

	public static <A> void doIfNotNull(A param, @Nullable Callbacks.Callback1<A> callback, Runnable ifNull) {
		if(param != null && !(param instanceof V8Value v8Value && v8Value.isNullOrUndefined())) {
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

	public static boolean isTrue(Object bool) {
		if(bool instanceof String s) {
			return Boolean.TRUE.equals(Boolean.parseBoolean(s));
		}

		return Boolean.TRUE.equals(bool);
	}

	public static Calendar getCalendar(long millis) {
		var calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar;
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

	@Contract("null -> false")
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
	@Deprecated(forRemoval = true)
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
	@Deprecated(forRemoval = true)
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

	/**
	 * Please, just don't do this.
	 */
	@Deprecated
	public static Integer asInteger(Integer o) {
		return o;
	}

	/**
	 * Please, just don't do this.
	 */
	@Deprecated
	public static Float asFloat(Float o) {
		return o;
	}

	/**
	 * Please, just don't do this.
	 */
	@Deprecated
	public static Long asLong(Long o) {
		return o;
	}

	public static Integer asInteger(Object o) {
		return o instanceof Number number ? number.intValue() : null;
	}

	public static Float asFloat(Object o) {
		return o instanceof Number number ? number.floatValue() : null;
	}

	public static RuntimeException asRuntimeException(Throwable t) {
		return t instanceof RuntimeException e ? e : new RuntimeException(t);
	}

	public static Long asLong(Object o) {
		return o instanceof Number number ? number.longValue() : null;
	}

	public static int getPhraseCount(@NonNull String sentence, String word) {
		int result = 0;

		while(true) {
			var index = sentence.indexOf(word);

			if(index == -1) {
				return result;
			}

			result++;
			sentence = sentence.substring(index + word.length());
		}
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
	 * @return The first object if it is not null, otherwise the second object
	 * @author MrBoomDev
	 */
	public static <T> T nonNullElse(T firstObject, T secondObject) {
		return firstObject != null ? firstObject : secondObject;
	}

	/**
	 * @return A stream from the collection compatible with old Androids
	 * @author MrBoomDev
	 */
	@NonNull
	@Contract("_ -> new")
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
	public static <E> Stream<E> stream(E... array) {
		requireArgument(array, "array");
		return StreamSupport.stream(Arrays.asList(array));
	}

	@NonNull
	@Contract("_ -> new")
	public static <E> Iterable<E> iterable(Stream<E> stream) {
		return new Iterable<>() {
			@NonNull
			@Override
			public Iterator<E> iterator() {
				return stream.iterator();
			}
		};
	}

	@NonNull
	@Contract("_ -> new")
	public static Stream<V8Value> stream(V8ValueArray array) throws JavetException {
		return stream((IV8ValueArray) array);
	}

	@NonNull
	@Contract("_ -> new")
	public static Stream<V8Value> stream(IV8ValueArray array) throws JavetException {
		requireArgument(array, "array");

		var size = array.getLength();
		var list = new ArrayList<V8Value>(size);

		for(int i = 0; i < size; i++) {
			list.set(i, array.get(i));
		}

		return StreamSupport.stream(list);
	}

	@NonNull
	public static <E> Stream<E> stream(Iterator<E> iterator) {
		requireArgument(iterator, "iterator");
		var list = new ArrayList<E>();

		while(iterator.hasNext()) {
			list.add(iterator.next());
		}

		return stream(list);
	}

	@NonNull
	public static <E> Stream<E> stream(Iterable<E> iterable) {
		requireArgument(iterable, "iterable");
		return stream(iterable.iterator());
	}

	/**
	 * @return A stream from map entries set compatible with old Androids
	 * @author MrBoomDev
	 */
	@NonNull
	@Contract("_ -> new")
	public static <K, V> Stream<Map.Entry<K,V>> stream(@NonNull Map<K, V> map) {
		requireArgument(map, "map");
		return StreamSupport.stream(map.entrySet());
	}

	@NonNull
	@Contract("_ -> new")
	public static Stream<Map.Entry<V8Value, V8Value>> stream(@NonNull V8ValueObject v8valueObject) throws JavetException {
		requireArgument(v8valueObject, "v8valueObject");

		var list = new ArrayList<Map.Entry<V8Value, V8Value>>();

		for(var key : iterable(stream(v8valueObject.getPropertyNames()))) {
			list.add(Map.entry(key, v8valueObject.get(key)));
		}

		return stream(list);
	}
}