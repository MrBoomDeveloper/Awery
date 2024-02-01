package com.mrboomdev.awery.util;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;

public class ViewUtil {
	public static final int UI_INSETS = WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();

	public static void setLeftMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.leftMargin = margin;
		view.setLayoutParams(margins);
	}

	public static void setRightMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.rightMargin = margin;
		view.setLayoutParams(margins);
	}

	public static void setTopMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.topMargin = margin;
		view.setLayoutParams(margins);
	}

	public static void setBottomMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.bottomMargin = margin;
		view.setLayoutParams(margins);
	}

	/**
	 * Automatically calls a listener.
	 * Provided insets will be used on the first call
	 */
	public static <V extends View> void setOnApplyInsetsListener(V view, InsetsUpdateListener<V, WindowInsetsCompat> listener) {
		ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
			listener.updated(view, insets);
			return WindowInsetsCompat.CONSUMED;
		});
	}

	public static <V extends View> void setOnApplyUiInsetsListener(V view, InsetsUpdateListener<V, Insets> listener, WindowInsets rootInsets) {
		if(rootInsets != null) {
			var uiInsets = WindowInsetsCompat.toWindowInsetsCompat(rootInsets).getInsets(UI_INSETS);
			listener.updated(view, uiInsets);
		}

		ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
			var uiInsets = insets.getInsets(UI_INSETS);
			listener.updated(view, uiInsets);
			return WindowInsetsCompat.CONSUMED;
		});
	}

	public static <V extends View> void setOnApplyUiInsetsListener(V view, InsetsUpdateListener<V, Insets> listener) {
		setOnApplyUiInsetsListener(view, listener, view.getRootWindowInsets());
	}

	@Nullable
	public static ViewGroup.MarginLayoutParams getMargins(@NonNull View view) {
		var params = view.getLayoutParams();

		if(params instanceof ViewGroup.MarginLayoutParams marginLayoutParams) {
			return marginLayoutParams;
		}

		return null;
	}

	public interface InsetsUpdateListener<V, I> {
		void updated(V view, I insets);
	}
}