package com.mrboomdev.awery.ui.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mrboomdev.awery.ui.ThemeManager;

import ani.awery.databinding.MainActivityLayoutBinding;

public class MainActivity extends AppCompatActivity {
	private MainActivityLayoutBinding binding;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		new ThemeManager(this).applyTheme();
		super.onCreate(savedInstanceState);

		binding = MainActivityLayoutBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
	}
}