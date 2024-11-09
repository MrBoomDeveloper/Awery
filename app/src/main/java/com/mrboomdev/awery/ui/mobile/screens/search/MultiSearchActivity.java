package com.mrboomdev.awery.ui.mobile.screens.search;

import static com.mrboomdev.awery.app.App.enableEdgeToEdge;
import static com.mrboomdev.awery.app.App.resolveAttrColor;
import static com.mrboomdev.awery.app.AweryLifecycle.postRunnable;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.ui.ViewUtil.dpPx;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalPadding;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;
import static com.mrboomdev.awery.util.ui.ViewUtil.setTopPadding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import com.mrboomdev.awery.app.data.settings.SettingsItem;
import com.mrboomdev.awery.app.data.settings.SettingsItemType;
import com.mrboomdev.awery.app.data.settings.SettingsList;
import com.mrboomdev.awery.databinding.LayoutHeaderSearchBinding;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.support.internal.InternalProviders;
import com.mrboomdev.awery.generated.AwerySettings;
import com.mrboomdev.awery.app.ThemeManager;
import com.mrboomdev.awery.ui.mobile.screens.catalog.feeds.FeedsFragment;
import com.mrboomdev.awery.util.NiceUtils;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class MultiSearchActivity extends AppCompatActivity {
	private static final int FRAGMENT_VIEW_ID = View.generateViewId();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		var fragmentView = new FragmentContainerView(this);
		fragmentView.setId(FRAGMENT_VIEW_ID);

		if(savedInstanceState == null) {
			var arguments = new Bundle();
			arguments.putSerializable("feeds", (Serializable) getFeeds());

			getSupportFragmentManager().beginTransaction()
					.setReorderingAllowed(true)
					.add(fragmentView.getId(), SearchFeedsFragment.class, arguments, "SearchFeedsFragment")
					.commit();
		}

		setContentView(fragmentView);
		fragmentView.setBackgroundColor(resolveAttrColor(this, android.R.attr.colorBackground));
	}

	@NonNull
	private List<CatalogFeed> getFeeds() {
		return stream(ExtensionsFactory.getExtensions__Deprecated(Extension.FLAG_WORKING))
				.map(ext -> ext.getProviders(ExtensionProvider.FEATURE_MEDIA_SEARCH))
				.flatMap(NiceUtils::stream)
				.filter(provider -> {
					if(provider instanceof InternalProviders.Lists) {
						return false;
					}

					var adultMode = AwerySettings.ADULT_MODE.getValue();

					if(adultMode != null) {
						switch(adultMode) {
							case SAFE -> {
								switch(provider.getAdultContentMode()) {
									case ONLY, PARTIAL -> {
										return false;
									}
								}
							}

							case ONLY -> {
								if(provider.getAdultContentMode() == ExtensionProvider.AdultContent.NONE) {
									return false;
								}
							}
						}
					}

					return true;
				})
				.map(provider -> {
					var feed = new CatalogFeed();
					feed.sourceManager = provider.getManager().getId();
					feed.sourceId = provider.getId();
					feed.title = provider.getName();
					return feed;
				})
				.toList();
	}

	public static class SearchFeedsFragment extends FeedsFragment {
		private static final String SAVED_QUERY = "query";
		private LayoutHeaderSearchBinding binding;
		public String query;

		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			if(savedInstanceState != null) {
				query = savedInstanceState.getString(SAVED_QUERY);
			}
		}

		@Override
		protected View getHeader(ViewGroup parent) {
			binding = LayoutHeaderSearchBinding.inflate(
					LayoutInflater.from(getContext()), parent, false);

			setOnApplyUiInsetsListener(binding.getRoot(), insets -> {
				setTopPadding(binding.getRoot(), insets.top + dpPx(binding, 4));
				setHorizontalPadding(binding.getRoot(), insets.left + dpPx(binding, 4), insets.right + dpPx(binding, 4));
				return true;
			});

			binding.back.setOnClickListener(v ->
					requireActivity().finish());

			binding.clear.setOnClickListener(v ->
					binding.edittext.setText(""));

			binding.filters.setVisibility(View.GONE);

			var inputManager = requireContext().getSystemService(InputMethodManager.class);

			binding.edittext.setOnEditorActionListener((v, action, event) -> {
				if(action != EditorInfo.IME_ACTION_SEARCH) {
					return false;
				}

				inputManager.hideSoftInputFromWindow(
						binding.edittext.getWindowToken(), 0);

				startLoading(true);
				query = binding.edittext.getText().toString();
				return true;
			});

			postRunnable(() -> {
				binding.edittext.requestFocus();
				inputManager.showSoftInput(binding.edittext, 0);
			});

			if(query != null) {
				binding.edittext.setText(query);
			}

			return binding.getRoot();
		}

		@Override
		public void onSaveInstanceState(@NonNull Bundle outState) {
			outState.putString(SAVED_QUERY, query);
			super.onSaveInstanceState(outState);
		}

		@Override
		protected SettingsList getFilters() {
			var text = binding.edittext.getText().toString();
			return new SettingsList(new SettingsItem(SettingsItemType.STRING, ExtensionProvider.FILTER_QUERY, text));
		}

		@Override
		protected int getMaxLoadsAtSameTime() {
			return 6;
		}

		@Override
		protected boolean loadOnStartup() {
			return query != null;
		}

		@Nullable
		@Override
		protected File getCacheFile() {
			return null;
		}
	}
}