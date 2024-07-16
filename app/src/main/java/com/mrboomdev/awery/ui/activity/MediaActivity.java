package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.AweryApp.enableEdgeToEdge;
import static com.mrboomdev.awery.app.AweryApp.isLandscape;
import static com.mrboomdev.awery.app.AweryApp.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.util.ui.ViewUtil.setBottomPadding;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.fragments.MediaCommentsFragment;
import com.mrboomdev.awery.ui.fragments.MediaInfoFragment;
import com.mrboomdev.awery.ui.fragments.MediaPlayFragment;
import com.mrboomdev.awery.ui.fragments.MediaRelationsFragment;
import com.mrboomdev.awery.util.MediaUtils;
import com.mrboomdev.awery.util.Parser;
import com.mrboomdev.awery.util.ui.FadeTransformer;
import com.mrboomdev.awery.util.ui.ViewUtil;

import java.io.IOException;
import java.util.Objects;

public class MediaActivity extends AppCompatActivity {
	private static final String TAG = "MediaActivity";
	private ScreenMediaDetailsBinding binding;
	private MediaCommentsFragment commentsFragment;
	private CatalogMedia media;
	private Object pendingExtra;

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

		ViewUtil.setOnApplyUiInsetsListener(binding.navigation, insets -> {
			var nav = (NavigationBarView) binding.navigation;
			nav.setBackgroundTintList(ColorStateList.valueOf(SurfaceColors.SURFACE_2.getColor(this)));
			getWindow().setNavigationBarColor(isLandscape() ? 0 : SurfaceColors.SURFACE_2.getColor(this));

			if(nav instanceof NavigationRailView) {
				nav.setPadding(insets.left, insets.top, 0, 0);
			} else {
				setBottomPadding(nav, insets.bottom, false);
				getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
			}

			return true;
		});

		if(binding.navigation instanceof NavigationRailView rail) {
			var style = com.google.android.material.R.attr.floatingActionButtonSmallSecondaryStyle;
			var header = new FloatingActionButton(this, null, style);
			header.setImageResource(R.drawable.ic_back);
			header.setOnClickListener(v -> finish());
			header.setBackgroundTintList(ColorStateList.valueOf(0x00000000));
			rail.addHeaderView(header);
		}

		if(AwerySettings.USE_AMOLED_THEME.getValue()) {
			binding.navigation.setBackgroundColor(0x00000000);
		}

		try {
			var json = getIntent().getStringExtra("media");
			setMedia(Parser.fromString(CatalogMedia.class, json));
		} catch(IOException e) {
			toast("Failed to load media!", 1);
			Log.e(TAG, "Failed to load media!", e);
			finish();
		}
	}

	public static void handleOptionsClick(@NonNull View anchor, @NonNull CatalogMedia media) {
		var context = new ContextThemeWrapper(anchor.getContext(), anchor.getContext().getTheme());
		var popup = new PopupMenu(context, anchor);

		if(media.url != null) {
			popup.getMenu().add(0, 0, 0, R.string.share);
		}

		popup.getMenu().add(0, 1, 0, R.string.blacklist);

		popup.setOnMenuItemClickListener(item -> switch(item.getItemId()) {
			case 0: MediaUtils.shareMedia(context, media); yield true;
			case 1: MediaUtils.blacklistMedia(media, () -> toast("Blacklisted successfully")); yield true;
			default: yield false;
		});

		popup.show();
	}

	@SuppressLint("NonConstantResourceId")
	public void setMedia(CatalogMedia media) {
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

		launchAction(Objects.requireNonNull(getIntent().getStringExtra("action")));
		setContentView(binding.getRoot());
	}

	public void launchAction(@NonNull String action, Object payload) {
		var navigation = (NavigationBarView) binding.navigation;

		navigation.setSelectedItemId(switch(action) {
			case MediaUtils.ACTION_INFO -> R.id.info;
			case MediaUtils.ACTION_WATCH -> R.id.watch;
			case MediaUtils.ACTION_RELATIONS -> R.id.relations;

			case MediaUtils.ACTION_COMMENTS -> {
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