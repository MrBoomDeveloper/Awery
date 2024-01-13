package com.mrboomdev.awery.anilist.query;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.anilist.data.AnilistTag;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AnilistTagsQuery {
	public static final short ADULT = 1;
	public static final short SAFE = 2;
	public static final short ALL = ADULT | SAFE;

	@NonNull
	@Contract("_ -> new")
	public static GenresQuery getGenres(short flags) {
		return new GenresQuery(flags);
	}

	@NonNull
	@Contract("_ -> new")
	public static TagsQuery getTags(short flags) {
		return new TagsQuery(flags);
	}

	public static class TagsQuery extends AnilistQuery<Collection<AnilistTag>> {
		private final boolean showAdult, showSafe;

		private TagsQuery(short flags) {
			this.showAdult = (flags & ADULT) == ADULT;
			this.showSafe = (flags & SAFE) == SAFE;
		}

		@Override
		protected Collection<AnilistTag> processJson(String json) throws IOException {
			List<AnilistTag> data = simpleListParse(AnilistTag.class, json);
			if(data == null) return Collections.emptyList();

			return data.stream().filter(item -> {
				if(item.isAdult() && !showAdult) return false;
				return item.isAdult() || showSafe;
			}).collect(Collectors.toList());
		}

		@NonNull
		@Contract(pure = true)
		@Override
		public String getQuery() {
			return "{ MediaTagCollection { name category description isAdult } }";
		}
	}

	public static class GenresQuery extends AnilistQuery<Collection<String>> {
		private final boolean showAdult, showSafe;

		private GenresQuery(short flags) {
			this.showAdult = (flags & ADULT) == ADULT;
			this.showSafe = (flags & SAFE) == SAFE;
		}

		@Override
		protected Collection<String> processJson(String json) throws IOException {
			List<String> data = simpleListParse(String.class, json);
			if(data == null) return Collections.emptyList();

			return data.stream().filter(item ->
					(showAdult || !item.equalsIgnoreCase("Hentai")))
					.collect(Collectors.toList());
		}

		@NonNull
		@Contract(pure = true)
		@Override
		public String getQuery() {
			return "{ GenreCollection }";
		}
	}
}