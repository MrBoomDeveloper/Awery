package com.mrboomdev.awery.util.ui.adapter;

import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.WeakHashMap;

public abstract class DropdownBindingAdapter<I, V> extends DropdownAdapter<I> {
	private final WeakHashMap<View, V> bindings = new WeakHashMap<>();

	public DropdownBindingAdapter(Collection<I> items) {
		super(items);
	}

	public DropdownBindingAdapter() {
		super();
	}

	@Override
	public final View onCreateView(ViewGroup parent, int viewType) {
		var binding = onCreateBinding(parent, viewType);
		var view = getView(binding);
		bindings.put(view, binding);
		return view;
	}

	public abstract V onCreateBinding(ViewGroup parent, int viewType);

	public abstract View getView(V binding);

	@Override
	public final void onBindItem(I item, View view) {
		onBindItem(item, bindings.get(view));
	}

	public abstract void onBindItem(I item, V binding);
}