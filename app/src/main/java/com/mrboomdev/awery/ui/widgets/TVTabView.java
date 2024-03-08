package com.mrboomdev.awery.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.util.ui.ViewUtil;

public class TVTabView extends FrameLayout {
	private final AppCompatImageView background;
	private final AppCompatTextView title;
	private boolean isSelected;
	private String text;

	public TVTabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		setClickable(true);
		setFocusable(true);

		background = new AppCompatImageView(context);
		background.setImageResource(R.drawable.view_tv_tab_background);
		addView(background, ViewUtil.MATCH_PARENT, ViewUtil.MATCH_PARENT);

		title = new AppCompatTextView(context);
		ViewUtil.setPadding(title, ViewUtil.dpPx(24), ViewUtil.dpPx(8));
		addView(title);

		var attributes = context.obtainStyledAttributes(attrs, R.styleable.TVTabView);
		setText(attributes.getString(R.styleable.TVTabView_android_text));
		setIsSelected(attributes.getBoolean(R.styleable.TVTabView_android_state_selected, false));
		attributes.recycle();
	}

	public TVTabView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TVTabView(Context context) {
		this(context, null);
	}

	@Override
	public boolean performClick() {
		var parent = getParent();

		if(parent != null) {
			if(parent instanceof TVTabBarView bar) {
				bar.handleTabClick(this);
			} else {
				throw new IllegalStateException("Parent must be TvTabBar!");
			}
		}

		return super.performClick();
	}

	public TVTabView setText(String text) {
		title.setText(text);
		this.text = text;
		return this;
	}

	public TVTabView setString(@StringRes int stringRes) {
		setText(getContext().getString(stringRes));
		return this;
	}

	public String getText() {
		return text;
	}

	public TVTabView setIsSelected(boolean isSelected) {

		background.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
		title.setTextColor(isSelected ? 0xFF_000000 : 0xFF_EEEEEE);
		title.setShadowLayer(isSelected ? 0 : 10, 0, 0, 0xDD_000000);

		setActivated(isSelected);
		this.isSelected = isSelected;
		return this;
	}

	public boolean isSelected() {
		return isSelected;
	}
}