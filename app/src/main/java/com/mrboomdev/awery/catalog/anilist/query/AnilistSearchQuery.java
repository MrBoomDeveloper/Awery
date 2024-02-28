package com.mrboomdev.awery.catalog.anilist.query;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.catalog.anilist.data.AnilistMedia;
import com.mrboomdev.awery.catalog.template.CatalogMedia;
import com.mrboomdev.awery.util.StringUtil;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AnilistSearchQuery extends AnilistQuery<Collection<CatalogMedia>> {
	private AnilistMedia.MediaType type;
	private MediaSort sort;
	private AnilistMedia.MediaFormat format;
	private String search, seasonName;
	private Integer seasonYear;
	private Boolean isAdult;

	private AnilistSearchQuery() {}

	public static class Builder {
		private AnilistMedia.MediaType type;
		private MediaSort sort;
		private AnilistMedia.MediaFormat format;
		private String search, seasonName;
		private Boolean isAdult;
		private Integer seasonYear;

		public Builder setType(AnilistMedia.MediaType type) {
			this.type = type;
			return this;
		}

		public Builder setSort(MediaSort sort) {
			this.sort = sort;
			return this;
		}

		public Builder setFormat(AnilistMedia.MediaFormat format) {
			this.format = format;
			return this;
		}

		public Builder setSearchQuery(String search) {
			this.search = search;
			return this;
		}

		public Builder setCurrentSeason() {
			var calendar = Calendar.getInstance();
			var season = calendar.get(Calendar.MONTH) / 3;

			this.seasonName = switch(season) {
				case 0 -> "WINTER";
				case 1 -> "SPRING";
				case 2 -> "SUMMER";
				case 3 -> "FALL";
				default -> throw new IllegalStateException("Invalid season: " + season);
			};

			this.seasonYear = calendar.get(Calendar.YEAR);

			return this;
		}

		public Builder setIsAdult(Boolean isAdult) {
			this.isAdult = isAdult;
			return this;
		}

		public Builder setSeasonName(String seasonName) {
			this.seasonName = seasonName;
			return this;
		}

		public Builder setSeasonYear(Integer seasonYear) {
			this.seasonYear = seasonYear;
			return this;
		}

		public AnilistSearchQuery build() {
			var query = new AnilistSearchQuery();
			query.type = type;
			query.sort = sort;
			query.format = format;
			query.search = search;
			query.isAdult = isAdult;
			query.seasonName = seasonName;
			query.seasonYear = seasonYear;
			return query;
		}
	}

	@NonNull
	@Contract(" -> new")
	public static Builder builder() {
		return new Builder();
	}

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
						type format duration
						countryOfOrigin
						id description bannerImage status
						genres averageScore episodes
						startDate { year month day }
						endDate { year month day }
						coverImage { extraLarge large color medium }
						tags { name id description }
						title { romaji(stylised: false) english(stylised: false) native(stylised: false) }
					}
				}
			}
		""".replace("__PARAMS__", StringUtil.mapToJson(new HashMap<>() {{
			if(type != null) put("type", type);
			if(sort != null) put("sort", sort);
			if(format != null) put("format", format);
			if(search != null) put("search", search);
			if(isAdult != null) put("isAdult", isAdult);
			if(seasonName != null) put("season", seasonName);
			if(seasonYear != null) put("seasonYear", seasonYear);
		}}));
	}
}