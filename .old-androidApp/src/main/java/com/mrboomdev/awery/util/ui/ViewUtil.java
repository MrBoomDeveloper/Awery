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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;

@Deprecated(forRemoval = true)
public class ViewUtil {
	public static final int UI_INSETS = WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();
	private static final String TAG = "ViewUtil";
	
	public static boolean setLeftMargin(View view, int margin) {
		var margins = getMargins(view);
		if(margins == null) return false;

		margins.leftMargin = margin;
		view.setLayoutParams(margins);
		return true;
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

	@Deprecated(forRemoval = true)
	public interface UseLayoutParamsCallback<T extends ViewGroup.LayoutParams> {
		void onUse(T params);
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

	@Deprecated(forRemoval = true)
	public static void setRightPadding(@NonNull View view, int padding) {
		view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), padding, view.getPaddingBottom());
	}

	@Deprecated(forRemoval = true)
	public static void setTopPadding(@NonNull View view, int padding) {
		view.setPadding(view.getPaddingLeft(), padding, view.getPaddingRight(), view.getPaddingBottom());
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
	private static ViewGroup.MarginLayoutParams getMargins(@NonNull View view) {
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