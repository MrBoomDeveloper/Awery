package com.mrboomdev.awery.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class ContextUtil {

	@ColorInt
	public static int getTextColor(@NonNull Context context, @AttrRes int id) {
		var typedValue = new TypedValue();
		context.getTheme().resolveAttribute(id, typedValue, true);

		var arr = context.obtainStyledAttributes(typedValue.data, new int[]{ id });
		var color = arr.getColor(0, -1);
		arr.recycle();
		return color;
	}

	public static int dpPx(int dp) {
		return Math.round(dp * Resources.getSystem().getDisplayMetrics().density);
	}

	public static int spPx(int sp) {
		return Math.round(sp * Resources.getSystem().getDisplayMetrics().scaledDensity);
	}
}