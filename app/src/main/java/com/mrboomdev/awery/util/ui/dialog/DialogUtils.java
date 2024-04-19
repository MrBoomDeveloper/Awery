package com.mrboomdev.awery.util.ui.dialog;

import android.app.Dialog;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.util.ui.ViewUtil;

public class DialogUtils {

	public static void fixDialog(@NonNull Dialog dialog) {
		if(dialog instanceof BottomSheetDialog sheet) {
			sheet.getBehavior().setPeekHeight(1000);
		}

		var window = dialog.getWindow();
		if(window == null) return;

		if(AweryApp.getConfiguration().screenWidthDp > 400) {
			window.setLayout(ViewUtil.dpPx(400), ViewGroup.LayoutParams.MATCH_PARENT);
		}
	}
}