package com.mrboomdev.awery.ui.widgets;

import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SubtitlesView extends LinearLayout {
	private final List<TextView> cachedViews = new ArrayList<>();
	private Runnable updateStyles;
	private int textColor = Color.WHITE,
			textOutlineColor = Color.BLACK,
			textSize = dpPx(14),
			textOutlineWidth = 0;

	public SubtitlesView(Context context) {
		super(context);
	}

	@UiThread
	public void setLines(@NonNull Collection<String> lines) {
		for(var textView : cachedViews) {
			textView.setVisibility(GONE);
		}

		for(int i = 0; i < lines.size(); i++) {
			var textView = i < cachedViews.size()
					? cachedViews.get(i)
					: addTextViewToPool();

			textView.setVisibility(VISIBLE);
		}
	}

	public void setTextColor(@ColorInt int color) {
		this.textColor = color;
	}

	public void setTextSize(int size) {
		this.textSize = size;
	}

	public void setTextOutlineColor(@ColorInt int color) {
		this.textOutlineColor = color;
	}

	public void setTextOutlineWidth(int width) {
		this.textOutlineWidth = width;
	}

	@NonNull
	private TextView addTextViewToPool() {
		var textView = new OutlineTextView(getContext());
		updateTextViewStyle(textView);

		cachedViews.add(textView);
		addView(textView);

		return textView;
	}

	private void updateTextViewStyle(@NonNull TextView view) {
		view.setTextColor(textColor);
		view.setTextSize(textSize);
	}

	private void postUpdateStyles() {
		if(updateStyles == null) {
			updateStyles = runOnUiThread(() -> {
				for(var textView : cachedViews) {
					updateTextViewStyle(textView);
				}

				updateStyles = null;
			});
		}
	}

	/**
	 * <a href="https://stackoverflow.com/a/54680974">Source code on StackOverflow</a>
	 */
	private class OutlineTextView extends AppCompatTextView {
		private boolean isDrawing;

		public OutlineTextView(@NonNull Context context) {
			super(context);
		}

		@Override
		public void invalidate() {
			if(!isDrawing) {
				super.invalidate();
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if(textOutlineWidth > 0) {
				drawOutline(canvas);
			}

			super.onDraw(canvas);
		}

		@SuppressLint("WrongCall")
		private void drawOutline(Canvas canvas) {
			isDrawing = true;

			setTextColor(textOutlineColor);
			getPaint().setStrokeWidth(textOutlineWidth);
			getPaint().setStyle(Paint.Style.STROKE);
			super.onDraw(canvas);

			setTextColor(textColor);
			getPaint().setStrokeWidth(0);
			getPaint().setStyle(Paint.Style.FILL);

			isDrawing = false;
		}
	}
}