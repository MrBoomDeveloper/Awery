package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.App.enableEdgeToEdge;
import static com.mrboomdev.awery.app.App.isLandscape;
import static com.mrboomdev.awery.app.App.resolveAttrColor;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.databinding.ScreenMediaDetailsBinding;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.fragments.MediaCommentsFragment;
import com.mrboomdev.awery.ui.fragments.MediaInfoFragment;
import com.mrboomdev.awery.ui.fragments.MediaPlayFragment;
import com.mrboomdev.awery.ui.fragments.MediaRelationsFragment;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.extensions.MediaExtensionsKt;
import com.mrboomdev.awery.util.ui.FadeTransformer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MediaActivity extends AppCompatActivity {
	public static final String EXTRA_ACTION_WATCH = "WATCH";
	public static final String EXTRA_ACTION_INFO = "INFO";
	public static final String EXTRA_ACTION_COMMENTS = "COMMENTS";
	public static final String EXTRA_MEDIA = "MEDIA";
	public static final String EXTRA_ACTION = "ACTION";
	private ScreenMediaDetailsBinding binding;
	private MediaCommentsFragment commentsFragment;
	private CatalogMedia media;
	private Object pendingExtra;

	@Retention(RetentionPolicy.SOURCE)
	@StringDef({ EXTRA_ACTION_WATCH, EXTRA_ACTION_INFO, EXTRA_ACTION_COMMENTS })
	public @interface Action {}

	@SuppressLint("NonConstantResourceId")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		binding = ScreenMediaDetailsBinding.inflate(getLayoutInflater());
		binding.pager.setUserInputEnabled(false);
		binding.pager.setPageTransformer(new FadeTransformer());
		binding.getRoot().setBackgroundColor(resolveAttrColor(this, android.R.attr.colorBackground));

		setOnApplyUiInsetsListener(binding.navigation, insets -> {
			if(AwerySettings.USE_AMOLED_THEME.getValue()) {
				binding.navigation.setBackgroundColor(0xff000000);
				getWindow().setNavigationBarColor(isLandscape() ? 0 : 0xff000000);
			} else {
				binding.navigation.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this));
				getWindow().setNavigationBarColor(isLandscape() ? 0 : SurfaceColors.SURFACE_2.getColor(this));
			}

			if(binding.navigation instanceof NavigationRailView) {
				binding.navigation.setPadding(insets.left, insets.top, 0, 0);
			} else {
				binding.navigation.setPadding(0, 0, 0, insets.bottom);
			}

			return true;
		});

		if(binding.navigation instanceof NavigationRailView rail) {
			var style = com.google.android.material.R.attr.floatingActionButtonSmallSecondaryStyle;
			var header = new FloatingActionButton(this, null, style);
			header.setImageResource(R.drawable.ic_back);
			header.setOnClickListener(v -> finish());
			header.setBackground(ContextCompat.getDrawable(this, R.drawable.ripple_circle_white));
			rail.addHeaderView(header);
		}

		if(AwerySettings.USE_AMOLED_THEME.getValue()) {
			binding.navigation.setBackgroundColor(0x00000000);
		}

		setMedia(requireArgument(this, EXTRA_MEDIA, CatalogMedia.class));
	}

	public static void handleOptionsClick(@NonNull View anchor, @NonNull CatalogMedia media) {
		var context = new ContextThemeWrapper(anchor.getContext(), anchor.getContext().getTheme());
		var popup = new PopupMenu(context, anchor);

		if(media.url != null) {
			popup.getMenu().add(0, 0, 0, R.string.share);
		}

		popup.getMenu().add(0, 1, 0, R.string.blacklist);

		popup.setOnMenuItemClickListener(item -> switch(item.getItemId()) {
			case 0: MediaExtensionsKt.share(media, context); yield true;
			case 1: MediaUtils.blacklistMedia(media, () -> toast("Blacklisted successfully")); yield true;
			default: yield false;
		});

		popup.show();
	}

	@SuppressLint("NonConstantResourceId")
	public void setMedia(@NonNull CatalogMedia media) {
		this.media = media;
		binding.pager.setAdapter(new PagerAdapter(getSupportFragmentManager(), getLifecycle()));

		var navigation = (NavigationBarView) binding.navigation;

		navigation.setOnItemSelectedListener(item -> {
			binding.pager.setCurrentItem(switch(item.getItemId()) {
				case R.id.info -> 0;
				case R.id.watch -> 1;
				case R.id.comments -> 2;
				case R.id.relations -> 3;
				default -> throw new IllegalArgumentException("Invalid item id: " + item.getItemId());
			}, false);
			return true;
		});

		if(media.type == CatalogMedia.MediaType.POST || media.type == CatalogMedia.MediaType.BOOK) {
			var item = navigation.getMenu().findItem(R.id.watch);
			item.setIcon(R.drawable.ic_book);
			item.setTitle(R.string.read);
		}

		var action = getIntent().getStringExtra(EXTRA_ACTION);
		if(action != null) launchAction(action);

		setContentView(binding.getRoot());
	}

	public void launchAction(@NonNull String action, Object payload) {
		var navigation = (NavigationBarView) binding.navigation;

		navigation.setSelectedItemId(switch(action) {
			case EXTRA_ACTION_INFO -> R.id.info;
			case EXTRA_ACTION_WATCH -> R.id.watch;

			case EXTRA_ACTION_COMMENTS -> {
				if(commentsFragment != null) {
					commentsFragment.setEpisode((CatalogVideo) payload);
				} else {
					pendingExtra = payload;
				}

				yield R.id.comments;
			}

			default -> throw new IllegalArgumentException("Invalid action: " + action);
		});
	}

	public void launchAction(@NonNull String action) {
		launchAction(action, null);
	}

	private class PagerAdapter extends FragmentStateAdapter {

		public PagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
			super(fragmentManager, lifecycle);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			return switch(position) {
				case 0 -> new MediaInfoFragment(media);
				case 1 -> new MediaPlayFragment(media);
				case 3 -> new MediaRelationsFragment();
				case 2 -> commentsFragment = new MediaCommentsFragment(media, (CatalogVideo) pendingExtra);
				default -> throw new IllegalArgumentException("Invalid position: " + position);
			};
		}

		@Override
		public int getItemCount() {
			return 4;
		}
	}
}