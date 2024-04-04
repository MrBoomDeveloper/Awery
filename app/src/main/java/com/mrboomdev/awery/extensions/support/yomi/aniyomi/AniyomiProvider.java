package com.mrboomdev.awery.extensions.support.yomi.aniyomi;

import static com.mrboomdev.awery.app.AweryApp.stream;
import static com.mrboomdev.awery.app.AweryApp.toast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.data.CatalogFilter;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSubtitle;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource;
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource;
import eu.kanade.tachiyomi.animesource.model.AnimesPage;
import java9.util.stream.Collectors;
import okhttp3.Headers;

public class AniyomiProvider extends ExtensionProvider {
	private final List<Integer> FEATURES = List.of(FEATURE_MEDIA_WATCH);
	private final AnimeCatalogueSource source;

	public AniyomiProvider(AnimeCatalogueSource source) {
		this.source = source;
	}

	@Override
	public void getEpisodes(
			int page,
			@NonNull CatalogMedia media,
			@NonNull ResponseCallback<List<? extends CatalogEpisode>> callback
	) {
		new Thread(() -> AniyomiKotlinBridge.getEpisodesList(source, AniyomiMedia.fromMedia(media), (episodes, e) -> {
			if(e != null) {
				callback.onFailure(e);
				return;
			}

			if(episodes == null || episodes.isEmpty()) {
				callback.onFailure(new ZeroResultsException("Aniyomi: No episodes found"));
				return;
			}

			callback.onSuccess(stream(episodes)
					.map(AniyomiEpisode::new).toList());
		})).start();
	}

	@Override
	public void getVideos(@NonNull CatalogEpisode episode, @NonNull ResponseCallback<List<CatalogVideo>> callback) {
		new Thread(() -> AniyomiKotlinBridge.getVideosList(source, AniyomiEpisode.fromEpisode(episode), (videos, e) -> {
			if(e != null) {
				callback.onFailure(e);
				return;
			}

			if(videos == null || videos.isEmpty()) {
				callback.onFailure(new ZeroResultsException("Aniyomi: No videos found"));
				return;
			}

			callback.onSuccess(stream(videos).map(item -> {
						var headers = item.getHeaders();

						var subtitles = stream(item.getSubtitleTracks()).map(track ->
								new CatalogSubtitle(track.getLang(), track.getUrl())).toList();

						return new CatalogVideo(
								item.getQuality(),
								item.getVideoUrl(),
								headers != null ? headers.toString() : "",
								subtitles
						);
					}).collect(Collectors.toCollection(ArrayList::new)));
		})).start();
	}

	@Override
	public Collection<Integer> getFeatures() {
		return FEATURES;
	}

	@NonNull
	private Bundle getHeaders(@NonNull Headers headers) {
		var bundle = new Bundle();

		for(var header : headers) {
			bundle.putString(header.getFirst(), header.getSecond());
		}

		return bundle;
	}

	@Contract("null, _, _ -> false")
	private boolean checkSearchResults(AnimesPage page, Throwable t, ResponseCallback<List<? extends CatalogMedia>> callback) {
		if(t != null) {
			callback.onFailure(t);
			return false;
		}

		if(page == null) {
			callback.onFailure(new NullPointerException("page is null!"));
			return false;
		}

		if(page.getAnimes().isEmpty()) {
			callback.onFailure(new ZeroResultsException("Found nothing in the catalog. Try changing your query."));
			return false;
		}

		return true;
	}

	@Override
	@SuppressLint("RestrictedApi")
	public void getSettings(Context context, @NonNull ResponseCallback<SettingsItem> callback) {
		if(source instanceof ConfigurableAnimeSource configurableSource) {
			var manager = new PreferenceManager(context);
			var screen = manager.createPreferenceScreen(context);
			configurableSource.setupPreferenceScreen(screen);

			var items = new ArrayList<SettingsItem>();

			for(int i = 0; i < screen.getPreferenceCount(); i++) {
				var preference = screen.getPreference(i);

				if(preference instanceof SwitchPreferenceCompat switchPref) {
					items.add(new CustomSettingsItem(SettingsItemType.BOOLEAN) {

						@Override
						public void saveValue(Object value) {
							switchPref.setChecked(false);
						}

						@Nullable
						@Override
						public String getTitle(Context context) {
							return preference.getTitle() == null ? null : preference.getTitle().toString();
						}

						@Override
						public boolean getBooleanValue() {
							return switchPref.isChecked();
						}

						@Override
						public String getKey() {
							return preference.getKey();
						}

						@Nullable
						@Override
						public String getDescription(Context context) {
							return preference.getSummary() == null ? null : preference.getSummary().toString();
						}
					});
				} else if(preference instanceof ListPreference listPref) {
					var prefVariants = new ArrayList<SettingsItem>();
					var entries = listPref.getEntries();
					var values = listPref.getEntryValues();

					for(int index = 0; index < entries.length; index++) {
						var title = entries[index];
						var value = values[index];

						prefVariants.add(new SettingsItem.Builder(SettingsItemType.STRING)
								.setTitle(title.toString())
								.setKey(value.toString())
								.build());
					}

					items.add(new CustomSettingsItem(SettingsItemType.SELECT) {

						@Override
						public void saveValue(Object value) {
							listPref.setValue(value.toString());
						}

						@NonNull
						@Override
						public String getTitle(Context context) {
							return preference.getTitle() == null ? "No title" : preference.getTitle().toString();
						}

						@Override
						public List<SettingsItem> getItems() {
							return prefVariants;
						}

						@Override
						public String getStringValue() {
							return listPref.getValue();
						}

						@Override
						public String getKey() {
							return preference.getKey();
						}

						@Nullable
						@Override
						public String getDescription(Context context) {
							return preference.getSummary() == null ? null : preference.getSummary().toString();
						}
					});
				} else if(preference instanceof MultiSelectListPreference multiSelectPref) {
					var prefVariants = new ArrayList<SettingsItem>();
					var entries = multiSelectPref.getEntries();
					var values = multiSelectPref.getEntryValues();

					for(int index = 0; index < entries.length; index++) {
						var title = entries[index];
						var value = values[index];

						prefVariants.add(new SettingsItem.Builder(SettingsItemType.STRING)
								.setTitle(title.toString())
								.setKey(value.toString())
								.build());
					}

					items.add(new CustomSettingsItem(SettingsItemType.MULTISELECT) {

						@Override
						@SuppressWarnings("unchecked")
						public void saveValue(Object value) {
							multiSelectPref.setValues((Set<String>) value);
						}

						@NonNull
						@Override
						public String getTitle(Context context) {
							return preference.getTitle() == null ? "No title" : preference.getTitle().toString();
						}

						@Override
						public List<SettingsItem> getItems() {
							return prefVariants;
						}

						@Override
						public String getKey() {
							return preference.getKey();
						}

						@Override
						public Set<String> getStringSetValue() {
							return multiSelectPref.getValues();
						}

						@Nullable
						@Override
						public String getDescription(Context context) {
							return preference.getSummary() == null ? null : preference.getSummary().toString();
						}
					});
				} else {
					toast("Unsupported setting: " + preference.getClass().getName());
				}
			}

			callback.onSuccess(new SettingsItem() {
				@Override
				public String getTitle(Context context) {
					return AniyomiProvider.this.getName() + " [" + AniyomiProvider.this.getLang() + "]";
				}

				@Override
				public List<SettingsItem> getItems() {
					return items;
				}

				@Override
				public SettingsItemType getType() {
					return SettingsItemType.SCREEN;
				}
			});
		} else {
			callback.onFailure(new IllegalStateException("Extension doesn't support settings!"));
		}
	}

	@Override
	public void search(CatalogFilter params, @NonNull ResponseCallback<List<? extends CatalogMedia>> callback) {
		var filter = source.getFilterList();

		new Thread(() -> AniyomiKotlinBridge.searchAnime(source, params.getPage(), params.getQuery(), filter, (page, t) -> {
			if(!checkSearchResults(page, t, callback)) return;

			callback.onSuccess(stream(page.getAnimes())
					.map(item -> new AniyomiMedia(this, item))
					.toList());
		})).start();
	}

	@NonNull
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String getId() {
		return String.valueOf(source.getId());
	}

	@Override
	public String getLang() {
		return source.getLang();
	}

	@Override
	public String getName() {
		return source.getName();
	}
}