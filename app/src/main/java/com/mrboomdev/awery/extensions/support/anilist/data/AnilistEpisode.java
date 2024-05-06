package com.mrboomdev.awery.extensions.support.anilist.data;

import com.mrboomdev.awery.extensions.data.CatalogEpisode;

public class AnilistEpisode {
	public String thumbnail;

	public CatalogEpisode toCatalogEpisode() {
		return new CatalogEpisode(null, null, thumbnail, null, -1, -1);
	}
}