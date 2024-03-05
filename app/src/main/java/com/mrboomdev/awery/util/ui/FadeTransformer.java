package com.mrboomdev.awery.util.ui;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.mrboomdev.awery.R;

public class FadeTransformer implements ViewPager2.PageTransformer {

	@Override
	public void transformPage(@NonNull View page, float position) {
		if(position != 0) return;
		ObjectAnimator.ofFloat(page, "alpha", 0, 1).setDuration(150).start();

		var anim = new ScaleAnimation(
				1.25f, 1f, 1.25f, 1f,
				Animation.RELATIVE_TO_SELF,
				0.5f,
				Animation.RELATIVE_TO_SELF,
				0);

		anim.setDuration(200);
		anim.setInterpolator(page.getContext(), R.anim.over_shoot);
		page.startAnimation(anim);
	}
}