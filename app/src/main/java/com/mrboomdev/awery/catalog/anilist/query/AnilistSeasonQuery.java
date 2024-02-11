package com.mrboomdev.awery.catalog.anilist.query;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.anilist.data.AnilistMedia;
import com.mrboomdev.awery.catalog.template.CatalogMedia;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AnilistSeasonQuery extends AnilistQuery<Collection<CatalogMedia<?>>> {
	private final String seasonName;
	private final int seasonYear;
	private final boolean isAnime;

	private AnilistSeasonQuery(String seasonName, int seasonYear, boolean isAnime) {
		this.seasonName = seasonName;
		this.seasonYear = seasonYear;
		this.isAnime = isAnime;
	}

	@NonNull
	@Contract(" -> new")
	public static AnilistSeasonQuery getCurrentAnimeSeason() {
		var calendar = Calendar.getInstance();
		var season = calendar.get(Calendar.MONTH) / 3;

		var seasonName = switch(season) {
			case 0 -> "WINTER";
			case 1 -> "SPRING";
			case 2 -> "SUMMER";
			case 3 -> "FALL";
			default -> throw new IllegalStateException("Invalid season: " + season);
		};

		return new AnilistSeasonQuery(seasonName, calendar.get(Calendar.YEAR), true);
	}

	@Override
	protected Collection<CatalogMedia<?>> processJson(String json) throws IOException {
		List<AnilistMedia> data = parsePageList(AnilistMedia.class, json);
		return data.stream().map(AnilistMedia::toCatalogMedia).collect(Collectors.toList());
	}

	@Override
	public String getQuery() {
		return """
			{
				Page(page: 1, perPage: 10) {
					media(seasonYear: __SEASON_YEAR__, season: __SEASON_NAME__, isAdult: false, type: __TYPE__, sort: POPULARITY_DESC) {
						type format status
						id description bannerImage
						genres averageScore
						duration episodes
						coverImage { extraLarge large color medium }
						tags { name id description isMediaSpoiler isGeneralSpoiler }
						title { romaji english }
					}
				}
			}
		""".replaceAll("__SEASON_NAME__", seasonName)
			.replaceAll("__SEASON_YEAR__", Integer.toString(seasonYear))
				.replaceAll("__TYPE__", isAnime ? "ANIME" : "MANGA");
	}
}