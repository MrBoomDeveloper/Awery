package com.mrboomdev.awery.util.ui.dialog;

import android.app.Dialog;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.AweryApp;
import com.mrboomdev.awery.util.ui.ViewUtil;

public class DialogUtil {

	public static void limitDialogSize(@NonNull Dialog dialog) {
		var window = dialog.getWindow();
		if(window == null) return;

		if(AweryApp.getConfiguration().screenWidthDp > 400) {
			window.setLayout(ViewUtil.dpPx(400), ViewGroup.LayoutParams.MATCH_PARENT);
		}
	}
}