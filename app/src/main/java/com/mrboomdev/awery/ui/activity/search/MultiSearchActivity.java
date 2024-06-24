package com.mrboomdev.awery.ui.activity.search;

import static com.mrboomdev.awery.app.AweryApp.enableEdgeToEdge;
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
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainerView;

import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.databinding.LayoutHeaderSearchBinding;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.support.internal.InternalProviders;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.fragments.FeedsFragment;
import com.mrboomdev.awery.util.NiceUtils;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class MultiSearchActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		var fragmentView = new FragmentContainerView(this);
		fragmentView.setId(View.generateViewId());

		var arguments = new Bundle();
		arguments.putSerializable("feeds", (Serializable) getFeeds());

		var fragment = new SearchFeedsFragment();
		fragment.setArguments(arguments);

		getSupportFragmentManager().beginTransaction()
				.setReorderingAllowed(true)
				.add(fragmentView, fragment, null)
				.commit();

		setContentView(fragmentView);
	}

	@NonNull
	private List<CatalogFeed> getFeeds() {
		return stream(ExtensionsFactory.getExtensions(Extension.FLAG_WORKING))
				.map(ext -> ext.getProviders(ExtensionProvider.FEATURE_MEDIA_SEARCH))
				.flatMap(NiceUtils::stream)
				.filter(provider -> !(provider instanceof InternalProviders.Lists))
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
		private EditText editText;

		@Override
		protected View getHeader(ViewGroup parent) {
			var binding = LayoutHeaderSearchBinding.inflate(
					LayoutInflater.from(parent.getContext()), parent, false);

			setOnApplyUiInsetsListener(binding.getRoot(), insets -> {
				setTopPadding(binding.getRoot(), insets.top + dpPx(4));
				setHorizontalPadding(binding.getRoot(), insets.left + dpPx(4), insets.right + dpPx(4));
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
				return true;
			});

			postRunnable(() -> {
				binding.edittext.requestFocus();
				inputManager.showSoftInput(binding.edittext, 0);
			});

			editText = binding.edittext;
			return binding.getRoot();
		}

		@Override
		protected List<SettingsItem> getFilters() {
			var text = editText != null ? editText.getText().toString() : null;
			return List.of(new SettingsItem(SettingsItemType.STRING, ExtensionProvider.FILTER_QUERY, text));
		}

		@Override
		protected boolean loadOnStartup() {
			return false;
		}

		@Nullable
		@Override
		protected File getCacheFile() {
			return null;
		}
	}
}