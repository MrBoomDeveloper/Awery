package com.mrboomdev.awery.util.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.mrboomdev.awery.databinding.LayoutLoadingBinding;
import com.mrboomdev.awery.util.exceptions.ExceptionDescriptor;

public class EmptyView implements ViewBinding {
	private final LayoutLoadingBinding binding;
	public final TextView title, message;
	public final CircularProgressIndicator progressBar;
	public final LinearLayout info;
	public final Button button;

	public EmptyView(@NonNull LayoutLoadingBinding binding) {
		this.binding = binding;
		title = binding.title;
		message = binding.message;
		progressBar = binding.progressBar;
		info = binding.info;
		button = binding.button;
	}

	public EmptyView(Context context) {
		this(LayoutLoadingBinding.inflate(LayoutInflater.from(context)));
	}

	public EmptyView(ViewGroup parent, boolean attachToParent) {
		this(LayoutLoadingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, attachToParent));
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

		getRoot().setVisibility(View.VISIBLE);
	}

	public void hideAll() {
		progressBar.setVisibility(View.GONE);
		info.setVisibility(View.GONE);
		getRoot().setVisibility(View.GONE);
	}

	public void setInfo(Context context, Throwable t) {
		var descriptor = new ExceptionDescriptor(t);
		setInfo(descriptor.getTitle(context), descriptor.getMessage(context));
	}

	public void setInfo(String title, String message) {
		setInfo(title, message, null, null);
	}

	public void setInfo(@StringRes int title, @StringRes int message) {
		setInfo(getContext().getString(title), getContext().getString(message));
	}

	public Context getContext() {
		return binding.getRoot().getContext();
	}

	public void startLoading() {
		progressBar.setVisibility(View.VISIBLE);
		info.setVisibility(View.GONE);
		getRoot().setVisibility(View.VISIBLE);
	}

	@NonNull
	@Override
	public View getRoot() {
		return binding.getRoot();
	}
}