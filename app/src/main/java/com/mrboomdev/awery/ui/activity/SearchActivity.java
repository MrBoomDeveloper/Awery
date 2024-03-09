package com.mrboomdev.awery.ui.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mrboomdev.awery.ui.ThemeManager;

public class SearchActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
	}
}