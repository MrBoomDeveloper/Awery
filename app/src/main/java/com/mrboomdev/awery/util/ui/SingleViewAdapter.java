package com.mrboomdev.awery.util.ui;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

public abstract class SingleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private final List<PendingViewOperation<View>> pendingViewOperations = new ArrayList<>();
	private final Handler handler = new Handler(Looper.getMainLooper());
	private Integer pendingVisibility;
	private View view;
	private boolean isEnabled = true;
	private int id;

	{ setHasStableIds(true); }

	public SingleViewAdapter() {}

	public SingleViewAdapter(int id) {
		this.id = id;
	}

	@NonNull
	public static SingleViewAdapter fromView(@NonNull View view, int id, ViewGroup.LayoutParams layoutParams) {
		var adapter = new SingleViewAdapter(id) {
			@NonNull
			@Override
			public View onCreateView(@NonNull ViewGroup parent) {
				if(layoutParams != null) {
					view.setLayoutParams(layoutParams);
				}

				return view;
			}
		};

		adapter.setView(view);
		return adapter;
	}

	public interface PendingViewOperation<V> {
		void onGotView(V view, boolean didJustCreated);
	}

	public static abstract class BindingSingleViewAdapter<T extends ViewBinding> extends SingleViewAdapter {
		private final List<PendingViewOperation<T>> pendingViewOperations = new ArrayList<>();
		private T binding;

		public abstract T onCreateBinding(ViewGroup parent);

		public T getBinding() {
			return binding;
		}

		protected void setBinding(T binding) {
			this.binding = binding;
		}

		public void getBinding(@NonNull PendingViewOperation<T> operation) {
			if(binding != null) operation.onGotView(binding, false);
			else pendingViewOperations.add(operation);
		}

		@NonNull
		@Override
		public View onCreateView(@NonNull ViewGroup parent) {
			this.binding = onCreateBinding(parent);

			var iterator = pendingViewOperations.iterator();
			while(iterator.hasNext()) {
				var next = iterator.next();
				next.onGotView(binding, true);
				iterator.remove();
			}

			return binding.getRoot();
		}
	}

	public interface ViewCreator<T> {
		@NonNull
		T createBinding(ViewGroup parent);
	}

	@NonNull
	@Contract("_ -> new")
	public static SingleViewAdapter fromViewDynamic(ViewCreator<View> createListener) {
		return new SingleViewAdapter() {

			@Override
			protected View onCreateView(ViewGroup parent) {
				return createListener.createBinding(parent);
			}
		};
	}

	@NonNull
	@Contract("_ -> new")
	public static <T extends ViewBinding> BindingSingleViewAdapter<T> fromBindingDynamic(ViewCreator<T> createListener) {
		return new BindingSingleViewAdapter<>() {

			@NonNull
			@Override
			public T onCreateBinding(ViewGroup parent) {
				return createListener.createBinding(parent);
			}
		};
	}

	@NonNull
	public static <T extends ViewBinding> BindingSingleViewAdapter<T> fromBinding(T binding, ViewGroup.LayoutParams layoutParams) {
		var adapter = new BindingSingleViewAdapter<T>() {
			@Override
			public T onCreateBinding(ViewGroup parent) {
				if(layoutParams != null) {
					//var newParams = ViewUtil.createFittingLayoutParams(parent, layoutParams);
					binding.getRoot().setLayoutParams(/*newParams*/ layoutParams);
				}

				return binding;
			}
		};

		adapter.setBinding(binding);
		return adapter;
	}

	@NonNull
	public static <T extends ViewBinding> BindingSingleViewAdapter<T> fromBinding(T binding) {
		return fromBinding(binding, null);
	}

	@NonNull
	public static SingleViewAdapter fromView(@NonNull View view, int id) {
		return fromView(view, id, null);
	}

	@NonNull
	@Contract("_ -> new")
	public static SingleViewAdapter fromView(@NonNull View view) {
		return fromView(view, 0);
	}

	public View getView() {
		return view;
	}

	public void getView(@NonNull PendingViewOperation<View> operation) {
		if(view != null) operation.onGotView(view, false);
		else pendingViewOperations.add(operation);
	}

	@SuppressLint("NotifyDataSetChanged")
	public synchronized void setEnabled(boolean isEnabled, boolean force) {
		if(this.isEnabled == isEnabled) return;

		boolean wasEnabled = this.isEnabled;
		this.isEnabled = isEnabled;

		try {
			if(!force) {
				if(wasEnabled) notifyItemRemoved(0);
				else notifyItemInserted(0);
			} else {
				notifyDataSetChanged();
			}
		} catch(IllegalStateException e) {
			handler.post(() -> setEnabled(isEnabled));
		}
	}

	public synchronized void setEnabled(boolean isEnabled) {
		setEnabled(isEnabled, false);
	}

	public void setEnabledSuperForce(boolean isEnabled) {
		setEnabled(isEnabled, false);
		setEnabled(isEnabled, true);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setId(int id) {
		this.id = id;
		notifyDataSetChanged();
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	protected abstract View onCreateView(ViewGroup parent);

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		view = onCreateView(parent);
		ViewUtil.removeParent(view);

		if(pendingVisibility != null) {
			view.setVisibility(pendingVisibility);
			pendingVisibility = null;
		}

		var iterator = pendingViewOperations.iterator();
		while(iterator.hasNext()) {
			var next = iterator.next();
			next.onGotView(view, true);
			iterator.remove();
		}

		return new RecyclerView.ViewHolder(view) {};
	}

	public void setVisibility(int visibility) {
		if(view != null) view.setVisibility(visibility);
		else pendingVisibility = visibility;
	}

	@Override
	public long getItemId(int position) {
		return id;
	}

	protected void setView(View view) {
		this.view = view;
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {}

	@Override
	public int getItemCount() {
		return isEnabled ? 1 : 0;
	}
}