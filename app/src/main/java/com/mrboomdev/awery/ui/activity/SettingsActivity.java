package com.mrboomdev.awery.ui.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mrboomdev.awery.data.settings.SettingsFactory;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.ui.adapter.SettingsAdapter;

import ani.awery.R;

public class SettingsActivity extends AppCompatActivity {
	public RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);

		var recycler = new RecyclerView(this);
		recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		recycler.setRecycledViewPool(viewPool);

		recycler.setAdapter(new SettingsAdapter(SettingsFactory.getInstance(this)));

		setContentView(recycler);
	}
}