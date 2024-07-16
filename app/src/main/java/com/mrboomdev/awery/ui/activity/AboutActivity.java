package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.AweryApp.enableEdgeToEdge;
import static com.mrboomdev.awery.app.AweryApp.getMarkwon;
import static com.mrboomdev.awery.app.AweryApp.isLandscape;
import static com.mrboomdev.awery.app.AweryApp.openUrl;
import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.ui.ViewUtil.MATCH_PARENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.WRAP_CONTENT;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setImageTintAttr;
import static com.mrboomdev.awery.util.ui.ViewUtil.setLeftMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopMargin;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.textview.MaterialTextView;
import com.mrboomdev.awery.BuildConfig;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.databinding.ScreenAboutBinding;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.util.Date;
import java.util.List;

import java9.util.stream.Collectors;

public class AboutActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		var binding = ScreenAboutBinding.inflate(getLayoutInflater());
		binding.getRoot().setBackgroundColor(resolveAttrColor(this, android.R.attr.colorBackground));
		setContentView(binding.getRoot());

		binding.back.setOnClickListener(v -> finish());

		binding.version.setText(stream(List.of(
				"Version: " + BuildConfig.VERSION_NAME,
				"Build at: " + new Date(BuildConfig.BUILD_TIME)
		)).collect(Collectors.joining("\n")));

		getMarkwon(this).setMarkdown(binding.info.fundMessage,
				binding.info.fundMessage.getText().toString());

		setOnApplyUiInsetsListener(binding.getRoot(), insets -> {
			if(isLandscape(this)) {
				binding.getRoot().setPadding(insets.left, insets.top, insets.right, insets.bottom);
			} else {
				binding.getRoot().setPadding(insets.left, 0, insets.right, insets.bottom);
				setTopMargin(binding.header, insets.top);
			}

			return true;
		});
	}

	public static class ContributorsView extends LinearLayoutCompat {

		public ContributorsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
			super(context, attrs, defStyleAttr);
			setOrientation(VERTICAL);

			for(var contributor : List.of(
					new Contributor("MrBoomDev", new String[] { "Main developer" }, "https://github.com/MrBoomDeveloper",
							"https://cdn.discordapp.com/avatars/1034891767822176357/3420c6a4d16fe513a69c85d86cb206c2.png?size=4096"),

					new Contributor("Ichiro", new String[] { "App icon" },
							"https://discord.com/channels/@me/1262060731981889536",
							"https://cdn.discordapp.com/avatars/778503249619058689/9d5baf6943f4eafbaf09eb8e9e287f2d.png?size=4096")
			)) {
				var linear = new LinearLayoutCompat(context);
				linear.setOrientation(HORIZONTAL);
				addView(linear, MATCH_PARENT, WRAP_CONTENT);
				ViewUtil.setPadding(linear, dpPx(linear, 8));
				setBottomMargin(linear, dpPx(linear, 4));

				var iconWrapper = new CardView(context);
				iconWrapper.setRadius(dpPx(this, 48));
				linear.addView(iconWrapper);

				var icon = new AppCompatImageView(context);
				iconWrapper.addView(icon, dpPx(this, 48), dpPx(this, 48));

				var info = new LinearLayoutCompat(context);
				info.setOrientation(VERTICAL);
				linear.addView(info);
				setLeftMargin(info, dpPx(this, 16));

				var name = new MaterialTextView(context);
				name.setText(contributor.name());
				name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				name.setTextColor(resolveAttrColor(name.getContext(), com.google.android.material.R.attr.colorOnBackground));
				info.addView(name);
				setBottomMargin(name, dpPx(this, 4));

				var roles = new MaterialTextView(context);
				roles.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				roles.setText(stream(contributor.roles()).collect(Collectors.joining(", ")));
				info.addView(roles);

				Glide.with(icon)
						.load(contributor.avatar())
						.transition(DrawableTransitionOptions.withCrossFade())
						.into(icon);

				linear.setClickable(true);
				linear.setFocusable(true);
				linear.setBackgroundResource(R.drawable.ripple_round_you);
				linear.setOnClickListener(v -> openUrl(context, contributor.url()));
			}
		}

		public ContributorsView(@NonNull Context context, @Nullable AttributeSet attrs) {
			this(context, attrs, 0);
		}
	}

	public static class SocialView extends LinearLayoutCompat {

		public SocialView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
			super(context, attrs, defStyleAttr);

			var linear = new LinearLayoutCompat(context);
			linear.setGravity(Gravity.CENTER_HORIZONTAL);
			linear.setOrientation(VERTICAL);
			addView(linear);
			ViewUtil.setPadding(linear, dpPx(linear, 12), dpPx(linear, 8));

			var icon = new AppCompatImageView(context);
			linear.addView(icon, dpPx(icon, 42), dpPx(icon, 42));
			setImageTintAttr(icon, com.google.android.material.R.attr.colorOnSecondaryContainer);

			var label = new MaterialTextView(context);
			linear.addView(label, WRAP_CONTENT, WRAP_CONTENT);
			setTopMargin(label, dpPx(label, 4));

			if(attrs != null) {
				try(var typed = context.obtainStyledAttributes(attrs, R.styleable.SocialView)) {
					icon.setImageDrawable(typed.getDrawable(R.styleable.SocialView_socialIcon));
					label.setText(typed.getString(R.styleable.SocialView_socialName));

					linear.setClickable(true);
					linear.setFocusable(true);
					linear.setBackgroundResource(R.drawable.ripple_round_you);

					var url = typed.getString(R.styleable.SocialView_socialLink);
					linear.setOnClickListener(v -> openUrl(context, url));
				}
			}
		}

		public SocialView(@NonNull Context context, @Nullable AttributeSet attrs) {
			this(context, attrs, 0);
		}
	}

	public record Contributor(String name, String[] roles, String url, String avatar) {}
}