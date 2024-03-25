package com.mrboomdev.awery.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mrboomdev.awery.databinding.LayoutLoadingBinding;

public class MediaRelationsFragment extends Fragment {

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