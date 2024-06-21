package com.mrboomdev.awery.util.ui;

import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.WindowInsets;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.Contract;

public class ViewUtil {
	public static final int UI_INSETS = WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();
	public static final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
	public static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
	public static final int MATCH_CONSTRAINT = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
	private static final String TAG = "ViewUtil";

	public static boolean setLeftMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return false;

		margins.leftMargin = margin;
		view.setLayoutParams(margins);
		return true;
	}

	@NonNull
	@Contract("_, _ -> new")
	public static LinearLayout.LayoutParams createLinearParams(int width, int height) {
		return new LinearLayout.LayoutParams(width, height);
	}

	@NonNull
	public static LinearLayoutCompat.LayoutParams createLinearParams(int width, int height, int weight) {
		var params = new LinearLayoutCompat.LayoutParams(width, height);
		params.weight = weight;
		return params;
	}

	@NonNull
	@Contract("_, _ -> new")
	public static ViewGroup.MarginLayoutParams createMarginParams(int width, int height) {
		return new ViewGroup.MarginLayoutParams(width, height);
	}

	public static boolean setWeight(@NonNull View view, float weight) {
		var params = view.getLayoutParams();

		if(params instanceof LinearLayout.LayoutParams layoutParams) {
			layoutParams.weight = weight;
			view.setLayoutParams(params);
			return true;
		}

		return false;
	}

	public static void setScale(@NonNull View view, float scale) {
		view.setScaleX(scale);
		view.setScaleY(scale);
	}

	public static float getScale(@NonNull View view) {
		return (view.getScaleX() + view.getScaleY()) / 2;
	}

	public static boolean setMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return false;

		margins.setMargins(margin, margin, margin, margin);
		view.setLayoutParams(margins);
		return true;
	}

	public static int getTopMargin(View view) {
		var margins = getMargins(view);
		return margins == null ? 0 : margins.topMargin;
	}

	public static int getBottomMargin(View view) {
		var margins = getMargins(view);
		return margins == null ? 0 : margins.bottomMargin;
	}

	public static int getRightMargin(View view) {
		var margins = getMargins(view);
		return margins == null ? 0 : margins.rightMargin;
	}

	public static int getLeftMargin(View view) {
		var margins = getMargins(view);
		return margins == null ? 0 : margins.leftMargin;
	}

	public static boolean setMargin(View view, int horizontal, int vertical) {
		var margins = getMargins(view);
		if(margins == null) return false;

		margins.setMargins(horizontal, vertical, horizontal, vertical);
		view.setLayoutParams(margins);
		return true;
	}

	public static void setPadding(@NonNull View view, int padding) {
		view.setPadding(padding, padding, padding, padding);
	}

	public static void setPadding(@NonNull View view, int horizontal, int vertical) {
		view.setPadding(horizontal, vertical, horizontal, vertical);
	}

	public static boolean setTopMargin(ViewGroup.LayoutParams params, int margin) {
		if(params instanceof ViewGroup.MarginLayoutParams marginLayoutParams) {
			marginLayoutParams.topMargin = margin;
			return true;
		}

		return false;
	}

	public static boolean removeParent(@NonNull View view) {
		if(view.getParent() instanceof ViewManager parent) {
			parent.removeView(view);
			return true;
		}

		return false;
	}

	public static void setVerticalMargin(ViewGroup.LayoutParams params, int margin) {
		if(params instanceof ViewGroup.MarginLayoutParams marginLayoutParams) {
			marginLayoutParams.topMargin = margin;
			marginLayoutParams.bottomMargin = margin;
		}
	}

	public static boolean setHorizontalMargin(ViewGroup.LayoutParams params, int margin) {
		if(params instanceof ViewGroup.MarginLayoutParams marginLayoutParams) {
			marginLayoutParams.rightMargin = margin;
			marginLayoutParams.leftMargin = margin;
			return true;
		}

		return false;
	}

	public interface UseLayoutParamsCallback<T extends ViewGroup.LayoutParams> {
		void onUse(T params);
	}

	@SuppressWarnings("unchecked")
	public static <T extends ViewGroup.LayoutParams> boolean useLayoutParams(
			@NonNull View view,
			UseLayoutParamsCallback<T> callback
	) {
		try {
			var params = view.getLayoutParams();
			if(params == null) return false;

			callback.onUse((T) params);
			view.setLayoutParams(params);
			return true;
		} catch(ClassCastException e) {
			Log.e(TAG, "Failed to cast layout params!", e);
			return false;
		}
	}

	public static <T extends ViewGroup.LayoutParams> boolean useLayoutParams(
			@NonNull View view,
			UseLayoutParamsCallback<T> callback,
			Class<T> paramsType
	) {
		try {
			var params = view.getLayoutParams();
			if(params == null) return false;

			callback.onUse(paramsType.cast(params));
			view.setLayoutParams(params);
			return true;
		} catch(ClassCastException e) {
			Log.e(TAG, "Failed to cast layout params!", e);
			return false;
		}
	}

	public static boolean setVerticalMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return false;

		margins.topMargin = margin;
		margins.bottomMargin = margin;
		view.setLayoutParams(margins);
		return true;
	}

	public static boolean setVerticalMargin(View view, int top, int bottom) {
		var margins = getMargins(view);
		if(margins == null) return false;

		margins.topMargin = top;
		margins.bottomMargin = bottom;
		view.setLayoutParams(margins);
		return true;
	}

	public static boolean setRightMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return false;

		margins.rightMargin = margin;
		view.setLayoutParams(margins);
		return true;
	}

	public static void setStartMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.setMarginStart(margin);
		view.setLayoutParams(margins);
	}

	public static boolean setHorizontalMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return false;

		margins.leftMargin = margin;
		margins.rightMargin = margin;
		view.setLayoutParams(margins);
		return true;
	}

	public static void setHorizontalMargin(View view, int left, int right) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.leftMargin = left;
		margins.rightMargin = right;
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

	public static void setVerticalPadding(@NonNull View view, int padding) {
		view.setPadding(view.getPaddingLeft(), padding, view.getPaddingRight(), padding);
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

	public static void setHorizontalPadding(@NonNull View view, int left, int right) {
		view.setPadding(left, view.getPaddingTop(), right, view.getPaddingBottom());
	}

	public static void setEndMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.setMarginEnd(margin);
		view.setLayoutParams(margins);
	}

	public static void setTopMargin(@NonNull View view, int margin) {
		var params = view.getLayoutParams();
		if(params == null) return;

		setTopMargin(view.getLayoutParams(), margin);
		view.setLayoutParams(params);
	}

	public static void setBottomMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return;

		margins.bottomMargin = margin;
		view.setLayoutParams(margins);
	}

	/**
	 * Automatically calls a listener.
	 * Provided insets will be used on the first call.
	 * @author MrBoomDev
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

	private static void setOnApplyUiInsetsListener(
			View view,
			InsetsUpdateListener<Insets> listener,
			WindowInsets rootInsets
	) {
		if(rootInsets == null && view.getParent() instanceof View parent) {
			rootInsets = parent.getRootWindowInsets();
		}

		if(rootInsets != null) {
			var uiInsets = WindowInsetsCompat.toWindowInsetsCompat(rootInsets, view).getInsets(UI_INSETS);
			listener.updated(uiInsets);
		}

		ViewCompat.setOnApplyWindowInsetsListener(view, (_view, insets) -> {
			var uiInsets = insets.getInsets(UI_INSETS);
			return listener.updated(uiInsets) ? WindowInsetsCompat.CONSUMED : insets;
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
		return dpPx(getAnyContext(), dp);
	}

	public static int dpPx(@NonNull Context context, int dp) {
		var metrics = context.getResources().getDisplayMetrics();
		return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics));
	}

	public static float spPx(int sp) {
		return spPx(getAnyContext(), sp);
	}

	public static float spPx(@NonNull Context context, int sp) {
		var metrics = context.getResources().getDisplayMetrics();
		return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics));
	}

	public interface InsetsUpdateListener<I> {
		/**
		 * @return true if insets were consumed. If false was returned, then children won't get any updates.
		 * @author MrBoomDev
		 */
		boolean updated(I insets);
	}
}