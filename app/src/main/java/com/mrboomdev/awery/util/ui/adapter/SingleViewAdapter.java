package com.mrboomdev.awery.util.ui.adapter;

import static com.mrboomdev.awery.app.AweryLifecycle.postRunnable;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.mrboomdev.awery.util.ui.ViewUtil;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class SingleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private final List<PendingViewOperation<View>> pendingViewOperations = new ArrayList<>();
	private Integer pendingVisibility;
	private View view;
	private boolean isEnabled = true;
	private int id, viewType;

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
		void onGotView(V view);
	}

	public static abstract class BindingSingleViewAdapter<T extends ViewBinding> extends SingleViewAdapter {
		private final Queue<PendingViewOperation<T>> pendingViewOperations = new LinkedList<>();
		private T binding;

		public abstract T onCreateBinding(ViewGroup parent);

		public T getBinding() {
			return binding;
		}

		protected void setBinding(T binding) {
			this.binding = binding;
		}

		public void getBinding(@NonNull PendingViewOperation<T> operation) {
			if(binding != null) operation.onGotView(binding);
			else pendingViewOperations.add(operation);
		}

		@NonNull
		@Override
		public View onCreateView(@NonNull ViewGroup parent) {
			this.binding = onCreateBinding(parent);

			PendingViewOperation<T> operation;
			while((operation = pendingViewOperations.poll()) != null) {
				operation.onGotView(binding);
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
	public static <T extends ViewBinding> BindingSingleViewAdapter<T> fromBindingDynamic(ViewCreator<T> createListener, int viewType) {
		var adapter = fromBindingDynamic(createListener);
		adapter.setViewType(viewType);
		return adapter;
	}

	public void setViewType(int viewType) {
		this.viewType = viewType;
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
		if(view != null) operation.onGotView(view);
		else pendingViewOperations.add(operation);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setEnabled(boolean isEnabled) {
		if(this.isEnabled == isEnabled) return;
		this.isEnabled = isEnabled;

		try {
			notifyDataSetChanged();
		} catch(RuntimeException e) {
			postRunnable(() -> setEnabled(isEnabled));
		}
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
			next.onGotView(view);
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
	public int getItemViewType(int position) {
		return viewType;
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {}

	@Override
	public int getItemCount() {
		return isEnabled ? 1 : 0;
	}
}