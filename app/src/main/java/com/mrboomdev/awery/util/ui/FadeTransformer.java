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

		/*var anim = new ScaleAnimation(
				1.15f, 1f, 1.15f, 1f,
				Animation.RELATIVE_TO_SELF,
				0.75f,
				Animation.RELATIVE_TO_SELF,
				0.75f);

		anim.setDuration(200);
		anim.setInterpolator(page.getContext(), R.anim.over_shoot);
		page.startAnimation(anim);*/
	}
}