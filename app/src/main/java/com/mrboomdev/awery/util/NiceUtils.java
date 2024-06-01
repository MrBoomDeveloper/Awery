package com.mrboomdev.awery.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.sdk.util.Callbacks;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

	/**
	 * @return The first item in the collection that matches the query
	 * @author MrBoomDev
	 */
	public static <A> A findIn(@NonNull Callbacks.Result1<Boolean, A> checker, Collection<A> collection) {
		return stream(collection)
				.filter(checker::run)
				.findAny().orElse(null);
	}

	/**
	 * @return The result of the callback
	 * @author MrBoomDev
	 */
	public static <A, B> A returnWith(B object, @NonNull Callbacks.Result1<A, B> callback) {
		return callback.run(object);
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

	public static boolean clearDirectory(File file) {
		if(file == null) return false;

		if(file.isDirectory()) {
			var children = file.listFiles();
			if(children == null) return false;

			for(var child : children) {
				clearDirectory(child);
			}
		}

		return true;
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