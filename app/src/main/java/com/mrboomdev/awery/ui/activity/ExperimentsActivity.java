package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.AweryApp.toast;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.mrboomdev.awery.ui.ThemeManager;

public class ExperimentsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		super.onCreate(savedInstanceState);

		var scroll = new ScrollView(this);
		setContentView(scroll);

		var root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		scroll.addView(root);

		var btn = new MaterialButton(this);
		btn.setText("There's nothing yet!");
		root.addView(btn);

		btn.setOnClickListener(v -> {
			toast("Here's nothing interesting yet...");
		});
	}
}