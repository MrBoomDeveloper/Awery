package com.mrboomdev.awery.util.ui.markdown;

import android.graphics.Color;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.regex.Pattern;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.utils.ColorUtils;

/**
 * Stolen from Dantotsu :)
 */
public class SpoilerPlugin extends AbstractMarkwonPlugin {
	private static final Pattern RE = Pattern.compile("\\|\\|.+?\\|\\|" /*"~!.+?!~"*/);

	@NonNull
	@Contract(" -> new")
	public static SpoilerPlugin create() {
		return new SpoilerPlugin();
	}

	@Override
	public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
		applySpoilerSpans((Spannable) markdown);
	}

	private static void applySpoilerSpans(@NonNull Spannable spannable) {
		var text = spannable.toString();
		var matcher = RE.matcher(text);

		while(matcher.find()) {
			var spoilerSpan = new RedditSpoilerSpan();

			var clickableSpan = new ClickableSpan() {
				@Override
				public void onClick(@NonNull View widget) {
					spoilerSpan.setRevealed(true);
					widget.postInvalidateOnAnimation();
				}

				@Override
				public void updateDrawState(@NonNull TextPaint ds) {
					// no op
				}
			};

			int start = matcher.start(), end = matcher.end();

			spannable.setSpan(spoilerSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			spannable.setSpan(new HideSpoilerSyntaxSpan(), start, start + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			spannable.setSpan(new HideSpoilerSyntaxSpan(), end - 2, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	private static class HideSpoilerSyntaxSpan extends CharacterStyle {

		@Override
		public void updateDrawState(@NonNull TextPaint tp) {
			tp.setColor(0);
		}
	}

	private static class RedditSpoilerSpan extends CharacterStyle {
		private boolean isRevealed;

		@Override
		public void updateDrawState(TextPaint tp) {
			if(isRevealed) {
				tp.bgColor = ColorUtils.applyAlpha(Color.DKGRAY, 25);
				return;
			}

			tp.bgColor = Color.DKGRAY;
			tp.setColor(Color.DKGRAY);
		}

		public void setRevealed(boolean isRevealed) {
			this.isRevealed = isRevealed;
		}
	}
}