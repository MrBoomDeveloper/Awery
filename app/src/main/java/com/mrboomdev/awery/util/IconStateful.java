package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.App.getResourceId;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.appcompat.graphics.drawable.StateListDrawableCompat;
import androidx.core.content.ContextCompat;

import com.mrboomdev.awery.R;

public class IconStateful {
	private String active, inActive;
	public String[] names;

	@NonNull
	public String getActive() {
		return active != null ? active : inActive;
	}

	@NonNull
	public String getInActive() {
		return inActive != null ? inActive : active;
	}

	public Drawable getDrawable(Context context) {
		var statefulDrawable = new StateListDrawableCompat();
		statefulDrawable.addState(new int[] { android.R.attr.state_checked }, getDrawable(context, State.ACTIVE));
		statefulDrawable.addState(new int[] { }, getDrawable(context, State.INACTIVE));
		return statefulDrawable;
	}

	public Drawable getDrawable(Context context, @NonNull State state) {
		return ContextCompat.getDrawable(context, switch(state) {
			case ACTIVE -> getResourceId(R.drawable.class, getActive());
			case INACTIVE -> getResourceId(R.drawable.class, getInActive());
		});
	}

	public enum State {
		ACTIVE, INACTIVE
	}
}