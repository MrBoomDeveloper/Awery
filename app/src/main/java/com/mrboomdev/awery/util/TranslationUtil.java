package com.mrboomdev.awery.util;

import android.content.Context;

import androidx.annotation.NonNull;

import ani.awery.R;

public class TranslationUtil {

	public static String getTranslatedLangName(Context context, @NonNull String input) {
		return switch(input) {
			case "en" -> context.getString(R.string.english);
			case "zh" -> context.getString(R.string.chinese);
			case "ja" -> context.getString(R.string.japanese);
			case "ru" -> context.getString(R.string.russian);
			default -> input;
		};
	}
}