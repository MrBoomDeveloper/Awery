package com.mrboomdev.awery.util;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.sdk.util.Callbacks;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import java9.util.stream.Stream;
import java9.util.stream.StreamSupport;

/**
 * Just a collection of useful simple utility methods
 * @author MrBoomDev
 */
public class NiceUtils {

	public static <A, B> A returnIfNotNull(B object, Callbacks.Result1<A, B> callback) {
		return object == null ? null : callback.run(object);
	}

	@NonNull
	@Contract("null -> fail; !null -> param1")
	public static <T> T requireNonNull(T obj) {
		if(obj == null) throw new NullPointerException();
		return obj;
	}

	public static <T> T requireNonNullElse(T obj, T elseObj) {
		return obj != null ? obj : elseObj;
	}

	public static boolean nonNull(Object obj) {
		return obj != null;
	}

	@NonNull
	@Contract("_ -> new")
	public static <E> Stream<E> stream(Collection<E> e) {
		return StreamSupport.stream(e);
	}

	@SafeVarargs
	@NonNull
	@Contract("_ -> new")
	public static <E> Stream<E> stream(E... e) {
		return StreamSupport.stream(Arrays.asList(e));
	}

	@NonNull
	@Contract("_ -> new")
	public static <K, V> Stream<Map.Entry<K,V>> stream(@NonNull Map<K, V> map) {
		return StreamSupport.stream(map.entrySet());
	}
}