package com.mrboomdev.awery.util.ui;

import android.content.res.Resources;
import android.util.TypedValue;
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

	public static void setMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.setMargins(margin, margin, margin, margin);
		view.setLayoutParams(margins);
	}

	public static void setPadding(@NonNull View view, int padding) {
		view.setPadding(padding, padding, padding, padding);
	}

	public static void setRightMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.rightMargin = margin;
		view.setLayoutParams(margins);
	}

	public static void setStartMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.setMarginStart(margin);
		view.setLayoutParams(margins);
	}

	public static void setHorizontalMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.leftMargin = margin;
		margins.rightMargin = margin;
		view.setLayoutParams(margins);
	}

	public static void setLeftPadding(@NonNull View view, int padding) {
		view.setPadding(padding, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
	}

	public static void setRightPadding(@NonNull View view, int padding) {
		view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), padding, view.getPaddingBottom());
	}

	public static void setTopPadding(@NonNull View view, int padding) {
		view.setPadding(view.getPaddingLeft(), padding, view.getPaddingRight(), view.getPaddingBottom());
	}

	public static void setBottomPadding(@NonNull View view, int padding) {
		setBottomPadding(view, padding, true);
	}

	public static void setBottomPadding(@NonNull View view, int padding, boolean getOther) {
		view.setPadding(
				getOther ? view.getPaddingLeft() : 0,
				getOther ? view.getPaddingTop() : 0,
				getOther ? view.getPaddingRight() : 0,
				padding);
	}

	public static void setHorizontalPadding(@NonNull View view, int padding) {
		view.setPadding(padding, view.getPaddingTop(), padding, view.getPaddingBottom());
	}

	public static void setEndMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.setMarginEnd(margin);
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
	public static void setOnApplyInsetsListener(View view, InsetsUpdateListener<WindowInsetsCompat> listener) {
		ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
			listener.updated(insets);
			return WindowInsetsCompat.CONSUMED;
		});
	}

	public static void setOnApplyUiInsetsListener(
			View view,
			InsetsUpdateListener<Insets> listener,
			View parentView
	) {
		if(parentView == null) {
			setOnApplyUiInsetsListener(view, listener);
			return;
		}

		setOnApplyUiInsetsListener(view, listener, parentView.getRootWindowInsets());
	}

	public static void setOnApplyUiInsetsListener(
			View view,
			InsetsUpdateListener<Insets> listener,
			WindowInsets rootInsets
	) {
		if(rootInsets == null && view.getParent() instanceof View parent) {
			rootInsets = parent.getRootWindowInsets();
		}

		if(rootInsets != null) {
			var uiInsets = WindowInsetsCompat.toWindowInsetsCompat(rootInsets).getInsets(UI_INSETS);
			listener.updated(uiInsets);
		}

		ViewCompat.setOnApplyWindowInsetsListener(view, (_view, insets) -> {
			var uiInsets = insets.getInsets(UI_INSETS);
			listener.updated(uiInsets);
			return WindowInsetsCompat.CONSUMED;
		});
	}

	public static void setOnApplyUiInsetsListener(View view, InsetsUpdateListener<Insets> listener) {
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

	public static int dpPx(int dp) {
		var metrics = Resources.getSystem().getDisplayMetrics();
		return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics));
	}

	public static int spPx(int sp) {
		var metrics = Resources.getSystem().getDisplayMetrics();
		return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics));
	}

	public interface InsetsUpdateListener<I> {
		void updated(I insets);
	}
}