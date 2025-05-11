package com.mrboomdev.awery.util.ui;

import android.animation.ObjectAnimator;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class FadeTransformer implements ViewPager2.PageTransformer {

	@Override
	public void transformPage(@NonNull View page, float position) {
		if(position != 0) return;

		ObjectAnimator.ofFloat(page, "alpha", 0, 1)
				.setDuration(150)
				.start();
	}
}