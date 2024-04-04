package com.mrboomdev.awery.extensions.support.anilist.query;

import static com.mrboomdev.awery.app.AweryApp.stream;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.util.graphql.GraphQLParser;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

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

	public static class TagsQuery extends AnilistQuery<Collection<CatalogTag>> {
		private final boolean showAdult, showSafe;

		private TagsQuery(short flags) {
			this.showAdult = (flags & ADULT) == ADULT;
			this.showSafe = (flags & SAFE) == SAFE;
		}

		@Override
		protected List<CatalogTag> processJson(String json) throws IOException {
			var data = GraphQLParser.parseList(json, CatalogTag.class);

			return stream(data).filter(item -> {
				if(item.isAdult() && !showAdult) return false;
				return item.isAdult() || showSafe;
			}).toList();
		}

		@NonNull
		@Contract(pure = true)
		@Override
		public String getQuery() {
			return "{ MediaTagCollection { name description isAdult } }";
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
			var data = GraphQLParser.parseList(json, String.class);

			return stream(data).filter(item ->
					(showAdult || !item.equalsIgnoreCase("Hentai")))
					.toList();
		}

		@NonNull
		@Contract(pure = true)
		@Override
		public String getQuery() {
			return "{ GenreCollection }";
		}
	}
}