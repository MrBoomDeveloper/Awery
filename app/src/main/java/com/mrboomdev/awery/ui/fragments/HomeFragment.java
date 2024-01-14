package com.mrboomdev.awery.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ani.awery.databinding.HomeFragmentBinding;

public class HomeFragment extends Fragment {
	private HomeFragmentBinding binding;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
		binding = HomeFragmentBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}


}