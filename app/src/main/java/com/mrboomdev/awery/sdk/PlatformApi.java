package com.mrboomdev.awery.sdk;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.BuildConfig;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import java9.util.stream.Stream;
import java9.util.stream.StreamSupport;

public class PlatformApi {

	public static String getVersionName() {
		return BuildConfig.VERSION_NAME;
	}

	@NonNull
	public static String getVersionCode() {
		return String.valueOf(BuildConfig.VERSION_CODE);
	}

	public static boolean isBeta() {
		return BuildConfig.IS_BETA;
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