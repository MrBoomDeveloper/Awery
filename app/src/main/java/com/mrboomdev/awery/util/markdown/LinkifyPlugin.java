package com.mrboomdev.awery.util.markdown;

import static com.mrboomdev.awery.app.AweryApp.openUrl;

import android.os.Parcel;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import org.commonmark.node.Link;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.RenderProps;
import io.noties.markwon.SpanFactory;
import io.noties.markwon.SpannableBuilder;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.core.CoreProps;

public class LinkifyPlugin extends AbstractMarkwonPlugin {

	@IntDef(flag = true, value = {
			Linkify.EMAIL_ADDRESSES,
			Linkify.PHONE_NUMBERS,
			Linkify.WEB_URLS
	})
	@Retention(RetentionPolicy.SOURCE)
	@interface LinkifyMask {}

	@NonNull
	public static LinkifyPlugin create() {
		return create(Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS);
	}

	@NonNull
	public static LinkifyPlugin create(@LinkifyMask int mask) {
		return new LinkifyPlugin(mask);
	}

	private final int mask;

	@SuppressWarnings("WeakerAccess")
	LinkifyPlugin(@LinkifyMask int mask) {
		this.mask = mask;
	}

	@Override
	public void configure(@NonNull Registry registry) {
		registry.require(CorePlugin.class, corePlugin -> {
			var listener = new LinkifyTextAddedListener(mask);
			corePlugin.addOnTextAddedListener(listener);
		});
	}

	private record LinkifyTextAddedListener(int mask) implements CorePlugin.OnTextAddedListener {

		@Override
		public void onTextAdded(@NonNull MarkwonVisitor visitor, @NonNull String text, int start) {
			// obtain span factory for links
			// we will be using the link that is used by markdown (instead of directly applying URLSpan)
			final SpanFactory spanFactory = visitor.configuration().spansFactory().get(Link.class);
			if(spanFactory == null) {
				return;
			}

			// we no longer re-use builder (thread safety achieved for
			// render calls from different threads and ... better performance)
			final SpannableStringBuilder builder = new SpannableStringBuilder(text);

			if(addLinks(builder, mask)) {
				// target URL span specifically
				final URLSpan[] spans = builder.getSpans(0, builder.length(), URLSpan.class);

				if(spans != null && spans.length > 0) {
					final RenderProps renderProps = visitor.renderProps();
					final SpannableBuilder spannableBuilder = visitor.builder();

					// Replace Android links spans with ours
					for(int i = 0; i < spans.length; i++) {
						var oldSpan = spans[i];
						var newSpan = new UrlSpan(oldSpan.getURL());

						spannableBuilder.setSpan(newSpan,
								start + builder.getSpanStart(oldSpan),
								start + builder.getSpanEnd(oldSpan));

						spans[i] = newSpan;
					}

					for(URLSpan span : spans) {
						CoreProps.LINK_DESTINATION.set(renderProps, span.getURL());

						SpannableBuilder.setSpans(spannableBuilder,
								spanFactory.getSpans(visitor.configuration(), renderProps),
								start + builder.getSpanStart(span),
								start + builder.getSpanEnd(span));
					}
				}
			}
		}

		private boolean addLinks(@NonNull Spannable text, @LinkifyMask int mask) {
			return Linkify.addLinks(text, mask);
		}
	}

	public static class UrlSpan extends URLSpan {

		public UrlSpan(String url) {
			super(url);
		}

		public UrlSpan(@NonNull Parcel src) {
			super(src);
		}

		@Override
		public void onClick(@NonNull View widget) {
			openUrl(widget.getContext(), getURL());
		}
	}
}