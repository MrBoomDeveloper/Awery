package com.mrboomdev.awery.util;

import static java.util.Objects.requireNonNull;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.Contract;

public class ArgUtils {

	public static String getStringExtra(@NonNull Activity activity, String name) {
		return getExtra(activity.getIntent(), name, String.class);
	}

	public static Long getLongExtra(@NonNull Activity activity, String name) {
		return getExtra(activity.getIntent(), name, Long.class);
	}

	@NonNull
	@Contract("null, _ -> fail")
	public static <T> T requireArgument(T o, String name) throws NullPointerException {
		if(o == null) {
			throw new NullPointerException("An required argument \"" + name + "\" was not specified!");
		}

		return o;
	}

	@NonNull
	public static <T> T requireExtra(@NonNull Activity activity, String name, Class<T> clazz) throws NullPointerException {
		return requireExtra(activity.getIntent(), name, clazz);
	}

	@NonNull
	public static <T> T requireExtra(Intent intent, String name, @NonNull Class<T> clazz) throws NullPointerException {
		return requireNonNull(clazz.cast(requireArgument(getExtra(intent, name, clazz), name)));
	}

	public static <T> T getExtra(Intent intent, String name, Class<T> clazz) {
		if(intent == null || !intent.hasExtra(name)) return null;
		Object result;

		if(clazz == String.class) result = intent.getStringExtra(name);
		else if(clazz == Integer.class) result = intent.getIntExtra(name, 0);
		else if(clazz == Boolean.class) result = intent.getBooleanExtra(name, false);
		else if(clazz == Long.class) result = intent.getLongExtra(name, 0);
		else if(clazz == Float.class) result = intent.getFloatExtra(name, 0);
		else result = intent.getSerializableExtra(name);

		return clazz.cast(result);
	}

	public static <T> T getExtra(Bundle bundle, String name, Class<T> clazz) {
		if(bundle == null || !bundle.containsKey(name)) return null;
		Object result;

		if(clazz == String.class) result = bundle.getString(name);
		else if(clazz == Integer.class) result = bundle.getInt(name, 0);
		else if(clazz == Boolean.class) result = bundle.getBoolean(name, false);
		else if(clazz == Long.class) result = bundle.getLong(name, 0);
		else if(clazz == Float.class) result = bundle.getFloat(name, 0);
		else result = bundle.getSerializable(name);

		return clazz.cast(result);
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
}