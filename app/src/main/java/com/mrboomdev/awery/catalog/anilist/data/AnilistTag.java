package com.mrboomdev.awery.catalog.anilist.data;

import com.mrboomdev.awery.catalog.template.CatalogTag;

import java.util.List;

public class AnilistTag {
	public String name, id;
	public String description;
	public boolean isAdult, isMediaSpoiler, isGeneralSpoiler;

	public CatalogTag toCatalogTag() {
		var tag = new CatalogTag();
		tag.name = name;
		tag.description = description;
		tag.isAdult = isAdult;
		tag.isSpoiler = isMediaSpoiler || isGeneralSpoiler;
		return tag;
	}
}