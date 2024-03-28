package com.mrboomdev.awery.extensions.support.anilist.query;

import static com.mrboomdev.awery.app.AweryApp.stream;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.app.AweryApp;
import com.mrboomdev.awery.extensions.support.anilist.data.AnilistMedia;
import com.mrboomdev.awery.extensions.support.template.CatalogMedia;
import com.mrboomdev.awery.util.StringUtil;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class AnilistSearchQuery extends AnilistQuery<Collection<CatalogMedia>> {
	private AnilistMedia.MediaType type;
	private MediaSort sort;
	private AnilistMedia.MediaFormat format;
	private String search, seasonName;
	private Integer seasonYear, page, itemsPerPage;
	private Boolean isAdult;

	private AnilistSearchQuery() {}

	public static class Builder {
		private AnilistMedia.MediaType type;
		private MediaSort sort;
		private AnilistMedia.MediaFormat format;
		private String search, seasonName;
		private Boolean isAdult;
		private Integer seasonYear, page, itemsPerPage;

		public Builder setType(AnilistMedia.MediaType type) {
			this.type = type;
			return this;
		}

		public Builder setSort(MediaSort sort) {
			this.sort = sort;
			return this;
		}

		public Builder setItemsPerPage(Integer itemsPerPage) {
			this.itemsPerPage = itemsPerPage;
			return this;
		}

		public Builder setPage(Integer page) {
			this.page = page;
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
			query.page = page;
			query.itemsPerPage = itemsPerPage;
			return query;
		}
	}

	@NonNull
	@Contract(" -> new")
	public static Builder builder() {
		return new Builder();
	}

	@Override
	protected Collection<CatalogMedia> processJson(String json) throws IOException {
		List<AnilistMedia> data = parsePageList(AnilistMedia.class, json);

		return stream(data).map(item -> {
					var catalogMedia = item.toCatalogMedia();

					var dbMedia = AweryApp.getDatabase().getMediaDao().get(catalogMedia.globalId);

					if(dbMedia != null && dbMedia.lists != null) {
						catalogMedia.lists = new ArrayList<>(StringUtil.uniqueStringToList(dbMedia.lists));
					}

					return catalogMedia;
				}).toList();
	}

	@Override
	public String getQuery() {
		return """
			{
				Page(page: __PAGE__, perPage: __ITEMS_PER_PAGE__) {
					media(__PARAMS__) {
						format duration
						countryOfOrigin
						id description bannerImage status
						genres averageScore episodes
						startDate { year month day }
						endDate { year month day }
						coverImage { extraLarge large color medium }
						tags { name id }
						title { romaji(stylised: false) english(stylised: false) native(stylised: false) }
					}
				}
			}
		""".replace("__PARAMS__", StringUtil.mapToJson(new HashMap<>() {{
			if(type != null) put("type", type);
			if(sort != null) put("sort", sort);
			if(format != null) put("format", format);
			if(search != null) put("search", "\"" + search + "\"");
			if(isAdult != null) put("isAdult", isAdult);
			if(seasonName != null) put("season", seasonName);
			if(seasonYear != null) put("seasonYear", seasonYear);
		}})).replace("__PAGE__", (page != null) ? String.valueOf(page + 1) : "1")
			.replace("__ITEMS_PER_PAGE__", (itemsPerPage != null) ? String.valueOf(itemsPerPage) : "20");
	}
}