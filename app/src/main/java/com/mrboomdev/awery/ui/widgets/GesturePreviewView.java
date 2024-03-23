package com.mrboomdev.awery.ui.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.util.ui.ViewUtil;

public class GesturePreviewView extends FrameLayout {
	private ConstraintLayout progressWrapperView;
	private LinearLayoutCompat linearView;
	private AppCompatImageView iconView, progressView;
	private float progress, maxProgress, alpha = -1;
	private boolean isVisible = true;

	public GesturePreviewView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		createViews(context);

		var attributes = context.obtainStyledAttributes(attrs, R.styleable.GesturePreviewView);
		setIcon(attributes.getDrawable(R.styleable.GesturePreviewView_icon));
		setMaxProgress(attributes.getInteger(R.styleable.GesturePreviewView_maxProgress, 100));
		setProgress(attributes.getInteger(R.styleable.GesturePreviewView_progress, 0));
		setIsVisible(attributes.getBoolean(R.styleable.GesturePreviewView_isVisible, true), false);
		attributes.recycle();
	}

	private void createViews(Context context) {
		linearView = new LinearLayoutCompat(context);
		linearView.setOrientation(LinearLayoutCompat.VERTICAL);
		linearView.setGravity(Gravity.CENTER_HORIZONTAL);
		linearView.setBackgroundResource(R.drawable.view_gesture_background);
		addView(linearView, ViewUtil.MATCH_PARENT, ViewUtil.MATCH_PARENT);

		if(alpha != -1) {
			linearView.setAlpha(alpha);
		}

		progressWrapperView = new ConstraintLayout(context);
		linearView.addView(progressWrapperView, ViewUtil.MATCH_PARENT, 0);
		ViewUtil.setWeight(progressWrapperView, 1);

		var progressParams = new ConstraintLayout.LayoutParams(ViewUtil.MATCH_PARENT, ViewUtil.MATCH_CONSTRAINT);
		progressParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

		progressView = new AppCompatImageView(context);
		progressView.setImageResource(R.drawable.view_gesture_foreground);
		progressWrapperView.addView(progressView, progressParams);

		iconView = new AppCompatImageView(context);
		var primaryColor = AweryApp.resolveAttrColor(context, android.R.attr.colorPrimary);
		iconView.setImageTintList(ColorStateList.valueOf(primaryColor));
		linearView.addView(iconView);
		ViewUtil.setVerticalMargin(iconView, ViewUtil.dpPx(8));
	}

	public GesturePreviewView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GesturePreviewView(Context context) {
		this(context, null);
	}

	public void setIsVisible(boolean isVisible, boolean animate) {
		if(isVisible == this.isVisible) return;

		if(animate) {
			var animation = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					linearView.setAlpha(isVisible
							? interpolatedTime
							: (1 - interpolatedTime));
				}
			};

			animation.setDuration(isVisible ? 100 : 250);
			animation.setInterpolator(new AccelerateDecelerateInterpolator());
			linearView.startAnimation(animation);
		} else {
			linearView.setAlpha(isVisible ? 1 : 0);
		}

		this.isVisible = isVisible;
	}

	public void setIsVisible(boolean isVisible) {
		setIsVisible(isVisible, true);
	}

	@Override
	public void setAlpha(float alpha) {
		if(linearView == null) {
			this.alpha = alpha;
			return;
		}

		linearView.setAlpha(alpha);
	}

	@Override
	public float getAlpha() {
		if(linearView == null) {
			if(alpha == -1) {
				return 1;
			}

			return alpha;
		}

		return linearView.getAlpha();
	}

	public void setIcon(Drawable drawable) {
		iconView.setImageDrawable(drawable);
	}

	public void setMaxProgress(int max) {
		this.maxProgress = max;
		updateProgressBars(false);
	}

	public void setProgress(int progress, boolean animate) {
		this.progress = progress;

		if(animate) {
			TransitionManager.beginDelayedTransition(progressWrapperView);
		}

		updateProgressBars(animate);
	}

	public void setProgress(int progress) {
		setProgress(progress, true);
	}

	private void updateProgressBars(boolean animate) {
		if(progress > maxProgress) return;

		if(animate) {
			TransitionManager.beginDelayedTransition(progressWrapperView, new ChangeBounds());
		}

		if(progressView.getLayoutParams() instanceof ConstraintLayout.LayoutParams params) {
			var percentage = progress / maxProgress;

			if(percentage == 1) {
				percentage = .99f;
			}

			params.matchConstraintPercentHeight = percentage;
			progressView.setLayoutParams(params);
		}
	}
}