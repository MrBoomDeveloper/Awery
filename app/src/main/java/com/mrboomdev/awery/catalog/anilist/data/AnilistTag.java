package com.mrboomdev.awery.catalog.anilist.data;

import com.mrboomdev.awery.catalog.template.CatalogTag;

public class AnilistTag {
	public String name, id;
	public String description;
	public boolean isAdult, isMediaSpoiler, isGeneralSpoiler;

	public CatalogTag toCatalogTag() {
		var tag = new CatalogTag();
		tag.setId(id);
		tag.setName(name);
		tag.setDescription(description);
		tag.setIsAdult(isAdult);
		tag.setIsSpoiler(isMediaSpoiler || isGeneralSpoiler);
		return tag;
	}
}