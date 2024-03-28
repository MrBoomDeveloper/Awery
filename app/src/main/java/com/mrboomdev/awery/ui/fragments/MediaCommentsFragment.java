package com.mrboomdev.awery.ui.fragments;

import static com.mrboomdev.awery.app.AweryApp.stream;
import static com.mrboomdev.awery.app.AweryApp.toast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.databinding.LayoutLoadingBinding;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsFactory;
import com.mrboomdev.awery.extensions.support.template.CatalogComment;
import com.mrboomdev.awery.extensions.support.template.CatalogMedia;

import java.util.List;

public class MediaCommentsFragment extends Fragment {
	private List<ExtensionProvider> providers;
	private CatalogMedia media;

	public MediaCommentsFragment() {
		this(null);
	}

	public MediaCommentsFragment(CatalogMedia media) {
		setMedia(media);
	}

	public void setMedia(CatalogMedia media) {
		if(media == null) return;
		this.media = media;
		//if(binding == null) return;

		if(providers != null) {
			if(providers.isEmpty()) {
				toast("Sorry, but you have no Comment Extensions installed!");
				return;
			}

			providers.get(0).readMediaComments(media, null, new ExtensionProvider.ResponseCallback<>() {
				@Override
				public void onSuccess(CatalogComment parent) {
					toast(parent.authorName + " - " + parent.text);
				}

				@Override
				public void onFailure(Throwable e) {
					toast("Failed to load comments!");
					e.printStackTrace();
				}
			});
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		providers = stream(ExtensionsFactory.getExtensions(Extension.FLAG_WORKING))
				.map(extension -> extension.getProviders(ExtensionProvider.FEATURE_READ_MEDIA_COMMENTS))
				.flatMap(AweryApp::stream)
				.sorted().toList();

		setMedia(media);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		var binding = LayoutLoadingBinding.inflate(inflater, container, false);

		binding.title.setText("Coming soon");
		binding.message.setText("This feature will be available in future updates");

		binding.info.setVisibility(View.VISIBLE);
		binding.progressBar.setVisibility(View.GONE);

		return binding.getRoot();
	}
}