package com.mrboomdev.awery.extensions.support.aweryjs;

import static com.mrboomdev.awery.util.NiceUtils.doIfNotNull;
import static com.mrboomdev.awery.util.NiceUtils.iterable;
import static com.mrboomdev.awery.util.NiceUtils.returnIfNotNull;
import static com.mrboomdev.awery.util.NiceUtils.returnWith;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static java.util.Objects.requireNonNull;

import androidx.annotation.NonNull;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.IV8Value;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.IV8ValuePromise;
import com.caoccao.javet.values.reference.V8ValueArray;
import com.caoccao.javet.values.reference.V8ValueFunction;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.caoccao.javet.values.reference.V8ValuePromise;
import com.mrboomdev.awery.R;
import com.mrboomdev.awery.app.data.settings.base.SettingsList;
import com.mrboomdev.awery.extensions.__Extension;
import com.mrboomdev.awery.extensions.__ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.util.ParserAdapter;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.mrboomdev.awery.util.exceptions.JsException;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java9.util.stream.Collectors;

public class AweryJsProvider extends __ExtensionProvider {
	protected __Extension extension;
	private static final String TAG = "AweryJsProvider";
	private final Map<String, V8Value> remembered = new HashMap<>();
	private final AweryJsManager manager;
	private final V8ValueObject impl;

	public AweryJsProvider(AweryJsManager manager, V8ValueObject impl) {
		this.manager = manager;
		this.impl = impl;
	}

	@Override
	public Set<String> getFeatures() {
		try {
			var value = rememberOrGet("getFeatures").await();

			if(!(value instanceof V8ValueArray array)) {
				throw new IllegalStateException("Result isn't an Array!");
			}

			return stream(AweryJsConverter.toJavaList(array))
					.filter(AweryJsProvider::nonNull)
					.map(item -> {
						try {
							return item.asString();
						} catch(JavetException e) {
							throw new RuntimeException(e);
						}
					}).collect(Collectors.toSet());
		} catch(Throwable e) {
			return Collections.emptySet();
		}
	}

	@Override
	public AsyncFuture<SettingsList> getMediaSearchFilters() {
		return call("getMediaSearchFilters").then(value -> {
			if(!(value instanceof V8ValueArray array)) {
				throw new IllegalStateException("Result isn't an Array!");
			}

			return AweryJsConverter.toSettingsList(array);
		});
	}

	@Override
	public AsyncFuture<SettingsList> getCommentFilters() {
		return call("getCommentFilters").then(value -> {
			if(!(value instanceof V8ValueArray array)) {
				throw new IllegalStateException("Result isn't an Array!");
			}

			return AweryJsConverter.toSettingsList(array);
		});
	}

	@Override
	public AsyncFuture<SettingsList> getTrackingFilters() {
		return call("getTrackingFilters").then(value -> {
			if(!(value instanceof V8ValueArray array)) {
				throw new IllegalStateException("Result isn't an Array!");
			}

			return AweryJsConverter.toSettingsList(array);
		});
	}

	@Override
	public AsyncFuture<SettingsList> getSubtitlesSearchFilters() {
		return call("getSubtitlesSearchFilters").then(value -> {
			if(!(value instanceof V8ValueArray array)) {
				throw new IllegalStateException("Result isn't an Array!");
			}

			return AweryJsConverter.toSettingsList(array);
		});
	}

	@Override
	public AsyncFuture<List<CatalogFeed>> getFeeds() {
		return super.getFeeds();
	}

	@Override
	public AsyncFuture<CatalogSearchResults<? extends CatalogMedia>> searchMedia(SettingsList filters) {
		return call("searchMedia", filters).then(results -> {
			if(!(results instanceof V8ValueObject object)) {
				throw new ZeroResultsException("Zero results", R.string.no_media_found);
			}

			var items = AweryJsConverter.toJavaList(object instanceof V8ValueArray array ? array : object.get("items"));

			if(items.isEmpty()) {
				throw new ZeroResultsException("Zero results", R.string.no_media_found);
			}

			return CatalogSearchResults.of(stream(items)
					.filter(AweryJsProvider::nonNull)
					.map(item -> {
						if(!(item instanceof V8ValueObject objItem)) {
							throw new IllegalArgumentException("Array item must be an Object!");
						}

						try {
							var id = requireNonNull(objItem.getString("id"));
							var media = new CatalogMedia(manager.getId(), extension.getId(), getId(), id);

							media.banner = objItem.getString("banner");
							media.url = objItem.getString("url");
							media.country = objItem.getString("country");
							media.description = objItem.getString("description");
							media.ageRating = objItem.getString("ageRating");
							media.extra = returnIfNotNull(objItem.getString("extra"), Object::toString);

							media.duration = objItem.getInteger("duration");
							media.averageScore = objItem.getFloat("averageScore");
							media.episodesCount = objItem.getInteger("episodesCount");
							media.latestEpisode = objItem.getInteger("latestEpisode");

							media.releaseDate = returnIfNotNull(objItem.get("startDate"),
									date -> (date instanceof Number releaseDateNumber)
											? ParserAdapter.calendarFromNumber(releaseDateNumber)
											: ParserAdapter.calendarFromString(date.toString()));

							media.status = returnIfNotNull(objItem.getString("status"), CatalogMedia.MediaStatus::valueOf);

							if(objItem.get("genres") instanceof V8ValueArray genres) {
								media.genres = stream(genres)
										.filter(AweryJsProvider::nonNull)
										.map(Object::toString)
										.toList();
							}

							if(objItem.get("tags") instanceof V8ValueArray tags) {
								media.tags = stream(tags)
										.filter(o -> o != null && !o.isNullOrUndefined())
										.map(tag -> {
											if(tag instanceof V8ValueObject map) try {
												var result = new CatalogTag();
												result.setName(map.getString("name"));
												result.setDescription(map.getString("description"));
												result.setIsAdult(Boolean.TRUE.equals(map.getBoolean("isAdult")));
												result.setIsSpoiler(Boolean.TRUE.equals(map.getBoolean("isSpoiler")));
												return result;
											} catch(JavetException ignored) {}

											return new CatalogTag(tag.toString());
										})
										.toList();
							}

							doIfNotNull(objItem.get("poster"), poster -> {
								if(poster instanceof V8ValueObject o) try {
									media.poster = new CatalogMedia.ImageVersions();
									media.poster.large = o.getString("large");
									media.poster.medium = o.getString("medium");
									media.poster.extraLarge = o.getString("extraLarge");
								} catch(JavetException ignored) {}

								media.setPoster(poster.toString());
							});

							if(objItem.get("titles") instanceof V8ValueArray array) {
								media.setTitles(stream(array)
										.filter(AweryJsProvider::nonNull)
										.map(Object::toString)
										.toList());
							} else {
								media.setTitles(List.of(media.getId()));
							}

							media.type = returnWith(objItem.getString("type"), type ->
									type == null ? CatalogMedia.MediaType.TV : CatalogMedia.MediaType.valueOf(type));

							if(objItem.get("ids") instanceof V8ValueObject ids) {
								for(var idEntry : iterable(stream(ids))) {
									media.setId(idEntry.getKey().asString(), idEntry.getValue().asString());
								}
							}

							return media;
						} catch(JavetException e) {
							throw new RuntimeException(e);
						}
					}).toList(), object != items ? object.getBoolean("hasNextPage") : false);
		});
	}

	@Override
	public String getName() {
		try {
			var value = rememberOrGet("getName").await();
			return value.isNullOrUndefined() ? extension.getName() : value.asString();
		} catch(Throwable e) {
			return extension.getName();
		}
	}

	@Override
	public AsyncFuture<String> getChangelog() {
		return call("getChangelog").then(IV8Value::asString);
	}

	@Override
	public String getId() {
		try {
			var value = rememberOrGet("getId").await();
			return value.isNullOrUndefined() ? extension.getId() : value.asString();
		} catch(Throwable e) {
			return extension.getId();
		}
	}

	@Override
	public AdultContent getAdultContentMode() {
		try {
			return AdultContent.valueOf(rememberOrGet("getAdultContentMode").await().asString());
		} catch(Throwable e) {
			return AdultContent.PARTIAL;
		}
	}

	@Override
	public __Extension getExtension() {
		return extension;
	}

	@Override
	public ExtensionsManager getManager() {
		return manager;
	}

	/**
	 * @return Future of either the value either null. It cannot be undefined.
	 */
	@NonNull
	private AsyncFuture<V8Value> call(String methodName, Object... arguments) {
		try {
			var it = impl.get(methodName);

			if(it instanceof V8ValueFunction fun) {
				V8Value[] jsValues;

				if(arguments instanceof V8Value[] v8Values) {
					jsValues = v8Values;
				} else {
					jsValues = new V8Value[arguments.length];

					for(int i = 0; i < arguments.length; i++) {
						V8Value obj;

						if(arguments[i] instanceof V8Value value) {
							obj = value;
						} else {
							var o = manager.jsRuntime.get().createV8ValueObject();
							o.bind(arguments[i]);
							obj = o;
						}

						jsValues[i] = obj;
					}
				}

				it = fun.call(null, jsValues);
			}

			if(it instanceof V8ValuePromise promise) {
				return AsyncUtils.controllableFuture(future -> {
					promise.register(new IV8ValuePromise.IListener() {
						@Override
						public void onFulfilled(V8Value v8Value) {
							try {
								future.complete(v8Value);
							} catch(Throwable e) {
								future.fail(e);
							}
						}

						@Override
						public void onCatch(V8Value v8Value) {
							try {
								future.fail(new JsException(v8Value));
							} catch(JavetException e) {
								future.fail(e);
							}
						}

						@Override
						public void onRejected(V8Value v8Value) {
							onCatch(v8Value);
						}
					});

					promise.getV8Runtime().await();
				});
			}

			return AsyncUtils.futureNow(it);
		} catch(JavetException e) {
			return AsyncUtils.futureFailNow(e);
		}
	}

	private AsyncFuture<V8Value> rememberOrGet(String key) {
		var remember = remembered.get(key);

		if(remember != null) {
			return AsyncUtils.futureNow(remember);
		}

		return call(key).then(result -> {
			remembered.put(key,result.toClone());
			return result;
		}, false);
	}

	private static boolean nonNull(@NonNull V8Value v8Value) {
		return !v8Value.isNullOrUndefined();
	}
}