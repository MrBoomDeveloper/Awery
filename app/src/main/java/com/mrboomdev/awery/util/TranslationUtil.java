package com.mrboomdev.awery.util;

import android.content.Context;

import androidx.annotation.NonNull;

import ani.awery.R;

public class TranslationUtil {

	public static String getTranslatedLangName(Context context, @NonNull String input) {
		return switch(input.toLowerCase()) {
			case "en", "eng", "english" -> context.getString(R.string.english);
			case "zh", "chs", "chinese" -> context.getString(R.string.chinese);
			case "ja", "jp", "japanese" -> context.getString(R.string.japanese);
			case "ru", "rus", "russian" -> context.getString(R.string.russian);
			/*case "de", "deu", "german" -> context.getString(R.string.german);
			case "ko", "kor", "korean" -> context.getString(R.string.korean);
			case "fr", "fra", "french" -> context.getString(R.string.french);
			case "es", "spa", "spanish" -> context.getString(R.string.spanish);
			case "it", "ita", "italian" -> context.getString(R.string.italian);
			case "pt", "por", "portuguese" -> context.getString(R.string.portuguese);
			case "pl", "pol", "polish" -> context.getString(R.string.polish);
			case "tr", "tur", "turkish" -> context.getString(R.string.turkish);
			case "hi", "hin", "hindi" -> context.getString(R.string.hindi);
			case "ar", "ara", "arabic" -> context.getString(R.string.arabic);
			case "uk", "ukr", "ukrainian" -> context.getString(R.string.ukrainian);
			case "ro", "ron", "romanian" -> context.getString(R.string.romanian);
			case "sv", "swe", "swedish" -> context.getString(R.string.swedish);*/
			default -> input;
		};
	}
}