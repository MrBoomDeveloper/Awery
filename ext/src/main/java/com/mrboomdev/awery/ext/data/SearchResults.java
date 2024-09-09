package com.mrboomdev.awery.ext.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SearchResults<T> extends ArrayList<T> {
	private final boolean hasNextPage;

	public SearchResults(Collection<T> original, boolean hasNextPage) {
		super(original);
		this.hasNextPage = hasNextPage;
	}

	public SearchResults(Collection<T> original) {
		super(original);
		this.hasNextPage = false;
	}

	public boolean hasNextPage() {
		return hasNextPage;
	}
}