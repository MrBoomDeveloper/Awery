package com.mrboomdev.awery.util.ui;

import static com.mrboomdev.awery.app.App.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryLifecycle.getAnyContext;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;

import org.jetbrains.annotations.Contract;

public class ViewUtil {
	@Deprecated(forRemoval = true)
	public static final int UI_INSETS = WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();
	@Deprecated(forRemoval = true)
	public static final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
	@Deprecated(forRemoval = true)
	public static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
	@Deprecated(forRemoval = true)
	public static final int MATCH_CONSTRAINT = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
	private static final String TAG = "ViewUtil";

	@Deprecated(forRemoval = true)
	public static boolean setLeftMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return false;

		margins.leftMargin = margin;
		view.setLayoutParams(margins);
		return true;
	}

	@Deprecated(forRemoval = true)
	public static void setImageTintColor(@NonNull ImageView view, @ColorInt int color) {
		view.setImageTintList(ColorStateList.valueOf(color));
	}

	@Deprecated(forRemoval = true)
	public static void setImageTintAttr(@NonNull ImageView view, @AttrRes int attr) {
		var color = resolveAttrColor(view.getContext(), attr);
		view.setImageTintList(ColorStateList.valueOf(color));
	}

	@Deprecated(forRemoval = true)
	public static void clearImageTint(@NonNull ImageView view) {
		view.setImageTintList(null);
	}

	@NonNull
	@Contract("_, _ -> new")
	@Deprecated(forRemoval = true)
	public static LinearLayout.LayoutParams createLinearParams(int width, int height) {
		return new LinearLayout.LayoutParams(width, height);
	}

	@NonNull
	@Deprecated(forRemoval = true)
	public static LinearLayoutCompat.LayoutParams createLinearParams(int width, int height, int weight) {
		var params = new LinearLayoutCompat.LayoutParams(width, height);
		params.weight = weight;
		return params;
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

	public static boolean setMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return false;

		margins.setMargins(margin, margin, margin, margin);
		view.setLayoutParams(margins);
		return true;
	}

	@Deprecated(forRemoval = true)
	public static int getTopMargin(View view) {
		var margins = getMargins(view);
		return margins == null ? 0 : margins.topMargin;
	}

	@Deprecated(forRemoval = true)
	public static int getBottomMargin(View view) {
		var margins = getMargins(view);
		return margins == null ? 0 : margins.bottomMargin;
	}

	@Deprecated(forRemoval = true)
	public static int getRightMargin(View view) {
		var margins = getMargins(view);
		return margins == null ? 0 : margins.rightMargin;
	}

	@Deprecated(forRemoval = true)
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

	public static boolean setMargin(View view, int left, int top, int right, int bottom) {
		var margins = getMargins(view);
		if(margins == null) return false;

		margins.setMargins(left, top, right, bottom);
		view.setLayoutParams(margins);
		return true;
	}

	public static void setPadding(@NonNull View view, int padding) {
		view.setPadding(padding, padding, padding, padding);
	}

	public static void setPadding(@NonNull View view, int horizontal, int vertical) {
		view.setPadding(horizontal, vertical, horizontal, vertical);
	}

	@Deprecated(forRemoval = true)
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

	@Deprecated(forRemoval = true)
	public interface UseLayoutParamsCallback<T extends ViewGroup.LayoutParams> {
		void onUse(T params);
	}

	@SuppressWarnings("unchecked")
	@Deprecated(forRemoval = true)
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

	@Deprecated(forRemoval = true)
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

	@Deprecated(forRemoval = true)
	public static boolean setRightMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return false;

		margins.rightMargin = margin;
		view.setLayoutParams(margins);
		return true;
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

	@Deprecated(forRemoval = true)
	public static void setLeftPadding(@NonNull View view, int padding) {
		view.setPadding(padding, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
	}

	@Deprecated(forRemoval = true)
	public static void setRightPadding(@NonNull View view, int padding) {
		view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), padding, view.getPaddingBottom());
	}

	@Deprecated(forRemoval = true)
	public static void setTopPadding(@NonNull View view, int padding) {
		view.setPadding(view.getPaddingLeft(), padding, view.getPaddingRight(), view.getPaddingBottom());
	}

	@Deprecated(forRemoval = true)
	public static void setVerticalPadding(@NonNull View view, int padding) {
		view.setPadding(view.getPaddingLeft(), padding, view.getPaddingRight(), padding);
	}

	@Deprecated(forRemoval = true)
	public static void setBottomPadding(@NonNull View view, int padding) {
		setBottomPadding(view, padding, true);
	}

	@Deprecated(forRemoval = true)
	public static void setBottomPadding(@NonNull View view, int padding, boolean getOther) {
		view.setPadding(
				getOther ? view.getPaddingLeft() : 0,
				getOther ? view.getPaddingTop() : 0,
				getOther ? view.getPaddingRight() : 0,
				padding);
	}

	@Deprecated(forRemoval = true)
	public static void setHorizontalPadding(@NonNull View view, int padding) {
		view.setPadding(padding, view.getPaddingTop(), padding, view.getPaddingBottom());
	}

	@Deprecated(forRemoval = true)
	public static void setHorizontalPadding(@NonNull View view, int left, int right) {
		view.setPadding(left, view.getPaddingTop(), right, view.getPaddingBottom());
	}

	@Deprecated(forRemoval = true)
	public static void setTopMargin(@NonNull View view, int margin) {
		var params = view.getLayoutParams();
		if(params == null) return;

		setTopMargin(view.getLayoutParams(), margin);
		view.setLayoutParams(params);
	}

	@Deprecated(forRemoval = true)
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
	@Deprecated(forRemoval = true)
	public static void setOnApplyInsetsListener(View view, InsetsUpdateListener<WindowInsetsCompat> listener) {
		ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
			listener.updated(insets);
			return WindowInsetsCompat.CONSUMED;
		});
	}

	@Deprecated(forRemoval = true)
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

	@Deprecated(forRemoval = true)
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

	@Deprecated(forRemoval = true)
	public static void setOnApplyUiInsetsListener(View view, InsetsUpdateListener<Insets> listener) {
		setOnApplyUiInsetsListener(view, listener, view.getRootWindowInsets());
	}

	@Nullable
	@Deprecated(forRemoval = true)
	public static ViewGroup.MarginLayoutParams getMargins(@NonNull View view) {
		var params = view.getLayoutParams();

		if(params instanceof ViewGroup.MarginLayoutParams marginLayoutParams) {
			return marginLayoutParams;
		}

		return null;
	}

	/**
	 * @deprecated Use {@link #dpPx(Context, float)}
	 */
	@Deprecated(forRemoval = true)
	public static int dpPx(float dp) {
		return dpPx(getAnyContext(), dp);
	}

	@Deprecated(forRemoval = true)
	public static int dpPx(@NonNull Context context, float dp) {
		var metrics = context.getResources().getDisplayMetrics();
		return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics));
	}

	@Deprecated(forRemoval = true)
	public static int dpPx(@NonNull View view, float dp) {
		return dpPx(view.getContext(), dp);
	}

	@Deprecated(forRemoval = true)
	public static int dpPx(@NonNull ViewBinding view, float dp) {
		return dpPx(view.getRoot().getContext(), dp);
	}

	@Deprecated(forRemoval = true)
	public static float spPx(@NonNull View view, float sp) {
		return spPx(view.getContext(), sp);
	}

	@Deprecated(forRemoval = true)
	public static float spPx(@NonNull Context context, float sp) {
		var metrics = context.getResources().getDisplayMetrics();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
	}

	@Deprecated(forRemoval = true)
	public interface InsetsUpdateListener<I> {
		/**
		 * @return true if insets were consumed. If false was returned, then children won't get any updates.
		 * @author MrBoomDev
		 */
		boolean updated(I insets);
	}
}