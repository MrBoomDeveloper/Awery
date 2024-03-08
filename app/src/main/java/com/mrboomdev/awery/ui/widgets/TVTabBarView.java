package com.mrboomdev.awery.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TVTabBarView extends LinearLayout {
	private final List<TVTabView> tabs = new ArrayList<>();
	private OnTabSelectedListener listener;

	public TVTabBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public TVTabBarView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TVTabBarView(Context context) {
		this(context, null);
	}

	public void handleTabClick(TVTabView tab) {
		boolean autoSelect = true;

		if(listener != null) {
			autoSelect = listener.onTabSelected(tab);
		}

		if(autoSelect) {
			selectTab(getTabIndex(tab), false);
		}
	}

	public void selectTab(int index, boolean callListener) {
		var tab = getTab(index);
		if(tab == null) return;

		if(callListener) {
			handleTabClick(tab);
		}

		for(var nextTab : tabs) {
			nextTab.setIsSelected(nextTab == tab);
		}
	}

	public TVTabView getTab(int index) {
		if(index < 0 || index >= tabs.size()) return null;
		return tabs.get(index);
	}

	public void selectTab(int index) {
		selectTab(index, true);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if(child instanceof TVTabView tab) {
			if(index == -1) {
				index = tabs.size();
			}

			tabs.add(index, tab);
			super.addView(child, index, params);
		} else {
			throw new IllegalArgumentException("Only TvTab is allowed in TvTabBar!");
		}
	}

	public int getTabIndex(TVTabView tab) {
		return tabs.indexOf(tab);
	}

	public interface OnTabSelectedListener {
		boolean onTabSelected(TVTabView tab);
	}

	public void setOnTabSelectedListener(OnTabSelectedListener listener) {
		this.listener = listener;
	}
}