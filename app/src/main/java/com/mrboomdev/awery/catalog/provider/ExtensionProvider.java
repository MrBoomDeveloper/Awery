package com.mrboomdev.awery.catalog.provider;

import com.mrboomdev.awery.catalog.template.CatalogCategory;
import com.mrboomdev.awery.catalog.template.CatalogMedia;

import java.util.List;
import java.util.Map;

public abstract class ExtensionProvider {
	public static final UnsupportedOperationException NOT_IMPLEMENTED = new UnsupportedOperationException("Not implemented!");

	public List<CatalogMedia<?>> search(Map<String, Object> params, int page) throws UnsupportedOperationException {
		throw NOT_IMPLEMENTED;
	}

	public Map<String, CatalogCategory> getCatalogCategories() throws UnsupportedOperationException {
		throw NOT_IMPLEMENTED;
	}
}