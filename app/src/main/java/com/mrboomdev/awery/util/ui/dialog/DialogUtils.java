package com.mrboomdev.awery.util.ui.dialog;

import static com.mrboomdev.awery.app.AweryApp.getConfiguration;
import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.useLayoutParams;

import android.app.Dialog;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.sidesheet.SideSheetDialog;

public class DialogUtils {

	/**
	 * A hacky method to fix the height, width of the dialog and color of the navigation bar.
	 * @author MrBoomDev
	 */
	public static void fixDialog(@NonNull Dialog dialog) {
		var context = dialog.getContext();

		if(dialog instanceof BottomSheetDialog sheet) {
			sheet.getBehavior().setPeekHeight(1000);

			var window = dialog.getWindow();
			if(window == null) return;

			window.setNavigationBarColor(SurfaceColors.SURFACE_1.getColor(context));
		}

		if(dialog instanceof SideSheetDialog) {
			var window = dialog.getWindow();
			if(window == null) return;

			var sheet = window.findViewById(com.google.android.material.R.id.m3_side_sheet);
			useLayoutParams(sheet, params -> params.width = dpPx(400));

			window.setNavigationBarColor(SurfaceColors.SURFACE_1.getColor(context));
		} else {
			/* If we'll try to do this shit with the SideSheetDialog, it will get centered,
			   so we use different approaches for different dialog types.*/

			var window = dialog.getWindow();
			if(window == null) return;

			if(getConfiguration().screenWidthDp > 400) {
				window.setLayout(dpPx(400), MATCH_PARENT);
			}
		}
	}
}