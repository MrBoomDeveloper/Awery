package com.mrboomdev.awery.extensions.data;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface CatalogSearchResults<T> extends List<T> {

	boolean hasNextPage();

	abstract class Impl<T> extends ArrayList<T> implements CatalogSearchResults<T> {
		public Impl(Collection<T> collection) {
			super(collection);
		}
	}

	@NonNull
	@Contract("_, _ -> new")
	static <T> CatalogSearchResults<T> of(Collection<T> list, boolean hasNextPage) {
		return new CatalogSearchResults.Impl<>(list) {

			@Override
			public boolean hasNextPage() {
				return hasNextPage;
			}
		};
	}

	@NonNull
	static <T> CatalogSearchResults<T> empty() {
		return new CatalogSearchResults.Impl<>(Collections.emptyList()) {

			@Override
			public boolean hasNextPage() {
				return false;
			}
		};
	}
}