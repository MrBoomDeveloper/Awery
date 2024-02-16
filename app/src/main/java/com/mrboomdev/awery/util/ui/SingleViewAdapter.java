package com.mrboomdev.awery.util.ui;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Contract;

public abstract class SingleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private final Handler handler = new Handler(Looper.getMainLooper());
	private boolean isEnabled = true;

	{ setHasStableIds(true); }

	@NonNull
	public static SingleViewAdapter fromView(@NonNull View view, ViewGroup.LayoutParams layoutParams) {
		return new SingleViewAdapter() {
			@NonNull
			@Override
			public View onCreateView(@NonNull ViewGroup parent) {
				if(layoutParams != null) {
					view.setLayoutParams(layoutParams);
				}

				return view;
			}
		};
	}

	@NonNull
	@Contract("_ -> new")
	public static SingleViewAdapter fromView(@NonNull View view) {
		return fromView(view, null);
	}

	public synchronized void setEnabled(boolean isEnabled) {
		boolean wasEnabled = this.isEnabled;
		this.isEnabled = isEnabled;

		try {
			if(wasEnabled && !isEnabled) {
				notifyItemRemoved(0);
			}

			if(!wasEnabled && isEnabled) {
				notifyItemInserted(0);
			}
		} catch(IllegalStateException e) {
			handler.post(() -> setEnabled(wasEnabled));
		}
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	protected abstract View onCreateView(ViewGroup parent);

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new RecyclerView.ViewHolder(onCreateView(parent)) {};
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {}

	@Override
	public int getItemCount() {
		return isEnabled ? 1 : 0;
	}
}