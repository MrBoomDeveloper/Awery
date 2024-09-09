package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import static com.mrboomdev.awery.util.NiceUtils.nonNullElse;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static java.util.Objects.requireNonNull;

import com.mrboomdev.awery.ext.constants.Awery;
import com.mrboomdev.awery.ext.constants.AgeRating;
import com.mrboomdev.awery.ext.data.ImageType;
import com.mrboomdev.awery.ext.data.Media;
import com.mrboomdev.awery.ext.data.Status;
import com.mrboomdev.awery.ext.data.User;
import com.mrboomdev.awery.extensions.support.yomi.YomiProvider;

import org.jetbrains.annotations.NotNull;

import eu.kanade.tachiyomi.animesource.model.SAnime;
import eu.kanade.tachiyomi.animesource.model.SAnimeImpl;
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource;
import java9.util.stream.Collectors;

public class AniyomiMedia {

	public static Media fromSAnime(@NotNull AniyomiProvider provider, @NotNull SAnime anime) {
		return new Media.Builder(
				Media.Type.TV, AniyomiManager.MANAGER_ID,
				provider.getExtension().getId(), provider.getId(), anime.getUrl()
		)
				.setTitles(anime.getTitle())
				.setAgeRating(provider.getAdultContentMode() != null && provider.getAdultContentMode().hasNsfw() ? AgeRating.NSFW : AgeRating.EVERYONE)
				.setDescription(anime.getDescription())
				.setTags(anime.getGenre() != null ? stream(requireNonNull(anime.getGenre()).split(", "))
						.map(String::trim)
						.filter(item -> !item.isBlank())
						.toArray(String[]::new) : null)
				.setImage(ImageType.LARGE_THUMBNAIL, anime.getThumbnail_url())
				.setAuthors(anime.getAuthor() == null ? null : new User.Builder()
						.setName(anime.getAuthor())
						.setRole(Awery.ARTIST)
						.build(),

					anime.getArtist() == null ? null : new User.Builder()
							.setName(anime.getArtist())
							.setRole(Awery.ARTIST)
							.build())
				.setUrl(!(provider.source instanceof AnimeHttpSource httpSource) ? null :
						YomiProvider.concatLink(httpSource.getBaseUrl(), anime.getUrl()))
				.setStatus(switch(anime.getStatus()) {
					case SAnime.COMPLETED, SAnime.PUBLISHING_FINISHED, SAnime.LICENSED -> Status.COMPLETED;
					case SAnime.CANCELLED -> Status.CANCELLED;
					case SAnime.ONGOING -> Status.ONGOING;
					case SAnime.ON_HIATUS -> Status.PAUSED;
					default -> null;
				})
				.build();
	}

	protected static @NotNull SAnime toSAnime(@NotNull Media media) {
		var anime = new SAnimeImpl();
		anime.setTitle(nonNullElse(media.getTitle(), "No title"));
		anime.setDescription(media.getDescription());
		anime.setThumbnail_url(media.getImage(ImageType.LARGE_THUMBNAIL_OR_OTHER));

		var url = media.getManagerId().equals(AniyomiManager.MANAGER_ID) ? media.getLocalId() : media.getUrl();
		if(url != null) anime.setUrl(url);

		if(media.getAuthors() != null) {
			for(var author : media.getAuthors()) {
				if(author == null) continue;

				if(Awery.AUTHOR.equals(author.getRole())) {
					anime.setAuthor(author.getName());
				}

				if(Awery.ARTIST.equals(author.getRole())) {
					anime.setArtist(author.getName());
				}
			}
		}

		if(media.getStatus() != null) {
			anime.setStatus(switch(media.getStatus()) {
				case ONGOING -> SAnime.ONGOING;
				case COMPLETED -> SAnime.COMPLETED;
				case PAUSED -> SAnime.ON_HIATUS;
				case CANCELLED -> SAnime.CANCELLED;
				case COMING_SOON -> 0;
			});
		}

		if(media.getGenres() != null) {
			anime.setGenre(stream(media.getGenres())
					.filter(genre -> genre != null && !genre.isBlank())
					.map(string -> requireNonNull(string).trim())
					.collect(Collectors.joining(", ")));
		} else if(media.getTags() != null) {
			anime.setGenre(stream(media.getTags())
					.filter(genre -> genre != null && !genre.isBlank())
					.map(string -> requireNonNull(string).trim())
					.collect(Collectors.joining(", ")));
		}

		return anime;
	}
}