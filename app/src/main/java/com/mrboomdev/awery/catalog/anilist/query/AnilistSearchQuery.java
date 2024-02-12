package com.mrboomdev.awery.catalog.anilist.query;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.anilist.data.AnilistMedia;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.StringUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AnilistSearchQuery extends AnilistQuery<Collection<CatalogMedia>> {
	private AnilistMedia.MediaType type;
	private MediaSort sort;
	private AnilistMedia.MediaFormat format;
	private String search;
	private Boolean isAdult;

	private AnilistSearchQuery() {}

	@NonNull
	public static AnilistSearchQuery search(
			AnilistMedia.MediaType type,
			MediaSort sort,
			AnilistMedia.MediaFormat format,
			String search,
			Boolean isAdult
	) {
		var query = new AnilistSearchQuery();
		query.type = type;
		query.sort = sort;
		query.format = format;
		query.search = search;
		query.isAdult = isAdult;
		return query;
	}

	@Override
	protected Collection<CatalogMedia> processJson(String json) throws IOException {
		List<AnilistMedia> data = parsePageList(AnilistMedia.class, json);

		return data.stream()
				.map(AnilistMedia::toCatalogMedia)
				.collect(Collectors.toList());
	}

	@Override
	public String getQuery() {
		return """
			{
				Page(page: 1, perPage: 20) {
					media(__PARAMS__) {
						type format isAdult
						id description bannerImage status
						genres averageScore
						duration episodes
						coverImage { extraLarge large color medium }
						tags { name id description isMediaSpoiler isGeneralSpoiler }
						title { romaji english }
					}
				}
			}
		""".replace("__PARAMS__", StringUtil.mapToJson(new HashMap<>() {{
			if(type != null) put("type", type);
			if(sort != null) put("sort", sort);
			if(format != null) put("format", format);
			if(search != null) put("search", search);
			if(isAdult != null) put("isAdult", isAdult);
		}}));
	}
}