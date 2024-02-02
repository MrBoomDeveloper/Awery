package com.mrboomdev.awery.catalog.anilist.query;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.anilist.data.AnilistTrendingMedia;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.graphql.GraphQLAdapter;
import com.mrboomdev.awery.util.graphql.GraphQLParser;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnilistTrendingQuery extends AnilistQuery<Collection<CatalogMedia<?>>> {

	@NonNull
	public static AnilistTrendingQuery getAnime() {
		return new AnilistTrendingQuery();
	}

	@NonNull
	public static AnilistTrendingQuery getManga() {
		return new AnilistTrendingQuery();
	}

	@Override
	protected Collection<CatalogMedia<?>> processJson(String json) throws IOException {
		List<AnilistTrendingMedia> data = parsePageList(AnilistTrendingMedia.class, json);
		return data.stream().map(AnilistTrendingMedia::toCatalogMedia).collect(Collectors.toList());
	}

	@Override
	public String getQuery() {
		return """
			{
				Page(page: 1, perPage: 25) {
					mediaTrends {
						averageScore
						media {
							type format isAdult
							id description bannerImage
							genres
							duration episodes
							coverImage { extraLarge large color medium }
							tags { name id description isMediaSpoiler isGeneralSpoiler }
							title { romaji english }
						}
					}
				}
			}
		""";
	}
}