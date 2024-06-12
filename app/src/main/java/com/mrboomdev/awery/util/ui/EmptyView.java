package com.mrboomdev.awery.util.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.mrboomdev.awery.databinding.LayoutLoadingBinding;

public class EmptyView implements ViewBinding {
	private final LayoutLoadingBinding binding;
	public final TextView title, message;
	public final ProgressBar progressBar;
	public final LinearLayout info;
	public final Button button;

	public EmptyView(Context context) {
		binding = LayoutLoadingBinding.inflate(LayoutInflater.from(context));

		title = binding.title;
		message = binding.message;
		progressBar = binding.progressBar;
		info = binding.info;
		button = binding.button;
	}

	public EmptyView(ViewGroup parent, boolean attachToParent) {
		binding = LayoutLoadingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, attachToParent);

		title = binding.title;
		message = binding.message;
		progressBar = binding.progressBar;
		info = binding.info;
		button = binding.button;
	}

	public void setInfo(String title, String message, String buttonText, Runnable buttonClickListener) {
		binding.title.setText(title);
		binding.message.setText(message);

		progressBar.setVisibility(View.GONE);
		info.setVisibility(View.VISIBLE);

		if(buttonText != null && buttonClickListener != null) {
			button.setText(buttonText);
			button.setOnClickListener(v -> buttonClickListener.run());
			button.setVisibility(View.VISIBLE);
		} else {
			button.setVisibility(View.GONE);
		}
	}

	public void setInfo(String title, String message) {
		setInfo(title, message, null, null);
	}

	public void startLoading() {
		progressBar.setVisibility(View.VISIBLE);
		info.setVisibility(View.GONE);
	}

	@NonNull
	@Override
	public View getRoot() {
		return binding.getRoot();
	}
}