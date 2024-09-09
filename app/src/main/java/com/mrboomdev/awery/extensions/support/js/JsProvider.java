/*
package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.extensions.support.js.JsBridge.booleanFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.floatFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.fromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.isNullJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.listFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.longFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.returnIfNotNullJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.stringFromJs;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;
import static com.mrboomdev.awery.util.NiceUtils.nonNullElse;
import static com.mrboomdev.awery.util.NiceUtils.returnWith;
import static com.mrboomdev.awery.util.NiceUtils.stream;
import static com.mrboomdev.awery.util.async.AsyncUtils.thread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.Awery;
import com.mrboomdev.awery.data.settings.ObservableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.data.settings.SettingsList;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionConstants;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.ExtensionsManager;
import com.mrboomdev.awery.extensions.data.CatalogComment;
import com.mrboomdev.awery.extensions.data.CatalogList;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.data.CatalogSubtitle;
import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.extensions.data.CatalogTrackingOptions;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.data.CatalogVideoFile;
import com.mrboomdev.awery.util.ParserAdapter;
import com.mrboomdev.awery.util.async.AsyncFuture;
import com.mrboomdev.awery.util.async.AsyncUtils;
import com.mrboomdev.awery.util.exceptions.JsException;
import com.mrboomdev.awery.util.exceptions.UnimplementedException;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;

import org.jetbrains.annotations.Contract;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Deprecated(forRemoval = true)
public class JsProvider extends ExtensionProvider {
	protected Extension extension;
	protected String id, version, script;
	private static final String TAG = "JsProvider";
	private final org.mozilla.javascript.Context context;
	private final ScriptableObject scope;
	private final JsManager manager;
	private Set<String> features;
	private android.content.Context androidContext;
	private AdultContentMode adultContentMode;
	private String name;
	private boolean didInit;

	public JsProvider(
			JsManager manager,
			android.content.Context androidContext,
			@NonNull Context rhinoContext,
			@NonNull String script
	) {
		super(null);

		this.manager = manager;
		this.context = rhinoContext;
		this.script = script;
		this.androidContext = androidContext;
		this.scope = rhinoContext.initStandardObjects();

		var bridge = Context.javaToJS(new JsBridge(manager, this, scope), scope);
		ScriptableObject.putConstProperty(scope, "Awery", bridge);

		rhinoContext.evaluateString(scope, script, null, 1,null);

		if(!didInit) {
			throw new JsException("It looks like you've forgot to call the \"setManifest\"!");
		}
	}

	@Override
	public Extension getExtension() {
		return extension;
	}

	@Override
	public ExtensionsManager getManager() {
		return manager;
	}

	protected void finishInit(@NonNull JsBridge bridge, @NonNull NativeObject o) {
		this.features = new HashSet<>(listFromJs(o.get("features", o), String.class));
		this.id = requireArgument(o, "id", String.class);
		this.name = returnWith(stringFromJs(o, "title"), title -> nonNullElse(title, id));
		this.version = requireArgument(o, "version", String.class);

		this.adultContentMode = returnWith(stringFromJs(o, "adultContent"),
				mode -> mode == null ? AdultContentMode.NONE : AdultContentMode.valueOf(mode.toUpperCase(Locale.ROOT)));

		var appContext = androidContext.getApplicationContext();
		bridge.context = new WeakReference<>(appContext);

		this.androidContext = null;
		this.didInit = true;
	}

	private class Settings extends SettingsItem implements ObservableSettingsItem {
		private final List<SettingsItem> items = new ArrayList<>();

		@NonNull
		@Contract(pure = true)
		@Override
		public String getTitle(android.content.Context context) {
			return getName();
		}

		@Override
		public SettingsItemType getType() {
			return SettingsItemType.SCREEN;
		}

		@Override
		public List<SettingsItem> getItems() {
			return items;
		}

		public void addItem(SettingsItem item) {
			items.add(item);
		}
	}

	*/
/*@Override
	public void getSettings(android.content.Context context, @NonNull ResponseCallback<SettingsItem> callback) {
		var root = new Settings();

		if(hasFeatures(FEATURE_LOGIN)) {
			var isLoggedIn = new AtomicReference<Boolean>();

			Runnable reload = () -> isLoggedIn(new ResponseCallback<>() {
				@Override
				public void onSuccess(Boolean aBoolean) {
					isLoggedIn.set(aBoolean);
				}

				@Override
				public void onFailure(Throwable e) {
					isLoggedIn.set(false);
					Log.e(TAG, "Failed to check if user is logged in", e);
				}
			});

			reload.run();
			await(() -> isLoggedIn.get() != null);

			root.addItem(new CustomSettingsItem(SettingsItemType.ACTION) {

				@Override
				public String getTitle(android.content.Context context) {
					return context.getString(isLoggedIn.get() ? R.string.logout : R.string.login);
				}

				@Override
				public void onClick(android.content.Context context) {
					var setting = this;

					if(isLoggedIn.get()) {
						logOut(new ResponseCallback<>() {

							@Override
							public void onSuccess(Boolean aBoolean) {
								toast("Logged out successfully");
								reload.run();
								runOnUiThread(() -> root.onSettingChange(setting));
							}

							@Override
							public void onFailure(Throwable e) {
								Log.e(TAG, "Failed to log out", e);
								toast("Failed to log out");
							}
						});
					} else {
						getLoginScreen(new ResponseCallback<>() {

							@Override
							public void onSuccess(Map<String, String> stringStringMap) {
								var intent = new Intent(context, LoginActivity.class);

								for(var entry : stringStringMap.entrySet()) {
									intent.putExtra(entry.getKey(), entry.getValue());
								}

								runOnUiThread(() -> startActivityForResult(context, intent, (resultCode, result) -> {
									if(resultCode != Activity.RESULT_OK) return;

									var params = new HashMap<String, String>();
									var data = result.getExtras();

									for(var key : Objects.requireNonNull(data).keySet()) {
										params.put(key, data.getString(key));
									}

									login(params, new ResponseCallback<>() {
										@Override
										public void onSuccess(Boolean aBoolean) {
											if(!aBoolean) return;

											toast("Logged in successfully");
											reload.run();
											runOnUiThread(() -> root.onSettingChange(setting));
										}

										@Override
										public void onFailure(Throwable e) {
											Log.e(TAG, "Failed to login", e);
											toast("Failed to login");
										}
									});
								}));
							}

							@Override
							public void onFailure(Throwable e) {
								Log.e(TAG, "Failed to get login screen", e);
								toast("Failed to login");
							}
						});
					}
				}
			});
		}

		callback.onSuccess(root);
	}*//*


	@Override
	public AsyncFuture<CatalogComment> voteComment(CatalogComment comment) {
		return this.<NativeObject>runFunction("aweryVoteComment",
				JsComment.createJsComment(context, scope, comment)).then(JsComment::new);
	}

	*/
/*@Override
	@SuppressWarnings("unchecked")
	public void getFeeds(@NonNull ResponseCallback<List<CatalogFeed>> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryFeeds") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] {
							(JsCallback<NativeArray>) (o, e) -> {
								if(e != null) {
									callback.onFailure(new JsException(e));
									return;
								}

								callback.onSuccess(stream(o)
										.filter(feed -> !isNullJs(feed))
										.map(item -> JsFeed.fromJs(this, (NativeObject) item))
										.toList());
							}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException("\"aweryFeeds\" is not a function or isn't defined!"));
			}
		});
	}*//*


	@Override
	public AsyncFuture<CatalogComment> editComment(CatalogComment oldComment, CatalogComment newComment) {
		return this.<NativeObject>runFunction("aweryEditComment",
				JsComment.createJsComment(this.context, this.scope, oldComment),
				JsComment.createJsComment(this.context, this.scope, newComment)).then(JsComment::new);
	}

	@Override
	public AsyncFuture<Boolean> deleteComment(CatalogComment comment) {
		return this.<Boolean>runFunction("aweryDeleteComment", JsComment.createJsComment(context, scope, comment)).then(result -> result);
	}

	@Override
	public AsyncFuture<String> getChangelog() {
		return runFunction("aweryChangelog").then(JsBridge::stringFromJs);
	}

	@Override
	public void trackMedia(
			CatalogMedia media,
			@Nullable CatalogTrackingOptions options,
			@NonNull ResponseCallback<CatalogTrackingOptions> callback
	) {
		manager.postRunnable(() -> {
			if(scope.get("aweryTrackMedia") instanceof Function fun) {
				try {
					Scriptable input = null;

					if(options != null) {
						input = context.newObject(scope);
						input.put("isPrivate", input, options.isPrivate);
						input.put("progress", input, options.progress);
						input.put("score", input, options.score);
						input.put("id", input, options.id);

						if(options.lists != null) {
							input.put("lists", input, context.newArray(scope, stream(options.lists)
									.map(list -> {
										var listObj = context.newObject(scope);
										listObj.put("id", listObj, list.getId());
										listObj.put("title", listObj, list.getTitle());
										return listObj;
									}).toArray()));
						}

						if(options.currentLists != null) {
							input.put("currentLists", input, context.newArray(scope, options.currentLists.toArray()));
						}

						if(options.startDate != null) {
							input.put("startDate", input, options.startDate.getTimeInMillis());
						}

						if(options.endDate != null) {
							input.put("endDate", input, options.endDate.getTimeInMillis());
						}
					}

					fun.call(context, scope, null, new Object[]{ media, input, (JsCallback<NativeObject>) (o, e) -> {
						if(e != null) {
							callback.onFailure(new JsException(e));
							return;
						}

						int features = 0;

						for(var feature : (NativeArray) o.get("features", o)) {
							if(JsBridge.isNullJs(feature)) continue;

							features = features | switch(feature.toString()) {
								case "startDate" -> CatalogTrackingOptions.FEATURE_DATE_START;
								case "endDate" -> CatalogTrackingOptions.FEATURE_DATE_END;
								case "progress" -> CatalogTrackingOptions.FEATURE_PROGRESS;
								case "score" -> CatalogTrackingOptions.FEATURE_SCORE;
								case "lists" -> CatalogTrackingOptions.FEATURE_LISTS;
								case "isPrivate" -> CatalogTrackingOptions.FEATURE_PRIVATE;
								case "createList" -> CatalogTrackingOptions.FEATURE_LIST_CREATE;
								default -> 0;
							};
						}

						var result = new CatalogTrackingOptions(features);
						result.isPrivate = booleanFromJs(o.get("isPrivate", o));
						result.id = stringFromJs(o.get("id", o));

						result.progress = returnIfNotNullJs(fromJs(o.get("progress", o), Float.class), Awery::returnMe);
						result.score = returnIfNotNullJs(fromJs(o.get("score", o), Float.class), Awery::returnMe);

						if(!isNullJs(o.get("currentLists", o))) {
							result.currentLists = listFromJs(o.get("currentLists", o), String.class);
						}

						if(!isNullJs(o.get("lists", o))) {
							result.lists = stream(listFromJs(o.get("lists", o), NativeObject.class))
									.map(itemObj -> new CatalogList(
											itemObj.get("title").toString(),
											itemObj.get("id").toString()))
									.toList();
						}

						if(!isNullJs(o.get("startDate", o))) {
							var startDate = o.get("startDate");

							if(startDate instanceof Number dateNumber) {
								result.startDate = ParserAdapter.calendarFromNumber(dateNumber);
							} else {
								result.startDate = ParserAdapter.calendarFromString(startDate.toString());
							}
						}

						if(!isNullJs(o.get("endDate", o))) {
							var endDate = o.get("endDate");

							if(endDate instanceof Number dateNumber) {
								result.endDate = ParserAdapter.calendarFromNumber(dateNumber);
							} else {
								result.endDate = ParserAdapter.calendarFromString(endDate.toString());
							}
						}

						callback.onSuccess(result);
					}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException("\"aweryTrackMedia\" is not a function or isn't defined!"));
			}
		});
	}

	private void login(Map<String, String> params, @NonNull ResponseCallback<Boolean> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryLogin") instanceof Function fun) {
				try {
					var obj = context.newObject(scope);

					for(var entry : params.entrySet()) {
						obj.put(entry.getKey(), obj, entry.getValue());
					}

					fun.call(context, scope, null, new Object[]{ obj, (JsCallback<Boolean>) (o, e) -> {
						if(e != null) {
							callback.onFailure(new JsException(e));
							return;
						}

						callback.onSuccess(o != null && o);
					}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException("\"aweryLogin\" is not a function or isn't defined!"));
			}
		});
	}

	private void isLoggedIn(@NonNull ResponseCallback<Boolean> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryIsLoggedIn") instanceof Function fun) {
				try {
					callback.onSuccess((boolean) fun.call(context, scope, null, new Object[0]));
					return;
				} catch(Throwable e) {
					callback.onFailure(e);
					return;
				}
			}

			callback.onFailure(new UnimplementedException("\"aweryIsLoggedIn\" is not a function or isn't defined!"));
		});
	}

	*/
/*@Override
	public void postMediaComment(
			PostMediaCommentRequest request,
			@NonNull ResponseCallback<CatalogComment> callback
	) {
		manager.postRunnable(() -> {
			if(scope.get("aweryPostMediaComment") instanceof Function fun) {
				try {
					var jsRequest = this.context.newObject(scope);

					jsRequest.put("episode", jsRequest, JsEpisode.getJsEpisode(
							this.context, scope, request.getEpisode()));

					jsRequest.put("comment", jsRequest, JsComment.createJsComment(
							this.context, scope, request.getComment()));

					jsRequest.put("parentComment", jsRequest, JsComment.createJsComment(
							this.context, scope, request.getParentComment()));

					fun.call(context, scope, null, new Object[] {
							jsRequest,
							(JsCallback<NativeObject>) (o, e) -> {
								if(e != null) {
									callback.onFailure(new JsException(e));
									return;
								}

								callback.onSuccess(new JsComment(o));
							}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException("\"aweryPostMediaComment\" is not a function or isn't defined!"));
			}
		});
	}*//*


	*/
/*@Override
	public void readMediaComments(ReadMediaCommentsRequest request, @NonNull ResponseCallback<CatalogComment> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryReadMediaComments") instanceof Function fun) {
				try {
					var jsRequest = context.newObject(scope);
					jsRequest.put("page", jsRequest, request.getPage());
					jsRequest.put("sortMode", jsRequest, request.getSortMode());
					jsRequest.put("media", jsRequest, request.getMedia());

					jsRequest.put("episode", jsRequest, JsEpisode.getJsEpisode(
							this.context, scope, request.getEpisode()));

					jsRequest.put("parentComment", jsRequest, JsComment.createJsComment(
							context, scope, request.getParentComment()));

					fun.call(context, scope, null, new Object[] { jsRequest, (JsCallback<NativeObject>) (o, e) -> {
						if(e != null) {
							callback.onFailure(new JsException(e));
							return;
						}

						callback.onSuccess(new JsComment(o));
					}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException("\"aweryReadMediaComments\" is not a function or isn't defined!"));
			}
		});
	}*//*


	@Override
	@SuppressWarnings("unchecked")
	public AsyncFuture<SettingsList> getMediaSearchFilters() {
		return this.<NativeArray>runFunction("aweryFilters")
				.then(array -> new SettingsList(stream(array)
						.map(item -> JsSettingsItem.fromJs((NativeObject) item, null))
						.toList()));
	}

	@Override
	public AsyncFuture<CatalogSearchResults<CatalogSubtitle>> searchSubtitles(SettingsList filters) {
		return this.<NativeObject>runFunction("awerySearchSubtitles", filters).then(o -> {
			if(((NativeArray)o.get("items")).isEmpty()) {
				throw new ZeroResultsException("Zero results", R.string.no_media_found);
			}

			return CatalogSearchResults.of(stream(listFromJs(o.get("items"), NativeObject.class))
					.filter(item -> !isNullJs(item))
					.map(item -> new CatalogSubtitle(stringFromJs(item.get("title")), stringFromJs(item.get("url"))))
					.toList(), booleanFromJs(o.get("hasNextPage")));
		});
	}

	private Scriptable filtersToJson(SettingsList filters) {
		return context.newArray(scope, stream(filters)
				.map(filter -> JsSettingsItem.toJs(filter, context, scope))
				.toArray());
	}

	@Override
	@SuppressWarnings("unchecked")
	public AsyncFuture<CatalogSearchResults<? extends CatalogMedia>> searchMedia(SettingsList filters) {
		return this.<NativeObject>runFunction("awerySearchMedia", filtersToJson(filters)).then(o -> {
			if(((NativeArray)o.get("items", o)).isEmpty()) {
				throw new ZeroResultsException("Zero results",
						R.string.no_media_found);
			}

			var results = new ArrayList<CatalogMedia>();

			for(var arrayItem : (NativeArray) o.get("items", o)) {
				if(JsBridge.isNullJs(arrayItem)) continue;
				var item = (NativeObject) arrayItem;

				var result = new CatalogMedia(manager.getId(), id, id, stringFromJs(item, "id"));
				result.url = stringFromJs(item.get("url"));
				result.banner = stringFromJs(item.get("banner"));
				result.country = stringFromJs(item.get("country"));
				result.ageRating = stringFromJs(item.get("ageRating"));
				result.extra = stringFromJs(("extra"));
				result.description = stringFromJs(item.get("description"));

				result.averageScore = fromJs(item.get("averageScore"), Float.class);
				result.duration = fromJs(item.get("duration"), Integer.class);
				result.episodesCount = fromJs(item.get("episodesCount"), Integer.class);
				result.latestEpisode = fromJs(item.get("latestEpisode"), Integer.class);

				result.releaseDate = returnIfNotNullJs(item.get("endDate", item), date -> {
					if(date instanceof Number releaseDateNumber) {
						return ParserAdapter.calendarFromNumber(releaseDateNumber);
					} else {
						return ParserAdapter.calendarFromString(date.toString());
					}
				});

				if(!isNullJs(item.get("poster", item))) {
					var poster = item.get("poster");

					if(poster instanceof NativeObject posterObject) {
						result.poster = new CatalogMedia.ImageVersions();
						result.poster.extraLarge = stringFromJs(posterObject.get("extraLarge"));
						result.poster.large = stringFromJs(posterObject.get("large"));
						result.poster.medium = stringFromJs(posterObject.get("medium"));
					} else {
						result.setPoster(stringFromJs(poster));
					}
				}

				if(item.get("tags", item) instanceof NativeArray array) {
					result.tags = stream(array)
							.filter(jsTag -> !JsBridge.isNullJs(jsTag))
							.map(obj -> {
								var jsTag = (NativeObject) obj;
								var tag = new CatalogTag();
								tag.setDescription(stringFromJs(jsTag.get("description")));
								tag.setIsSpoiler(booleanFromJs(jsTag.get("isSpoiler")));
								tag.setName(stringFromJs(jsTag.get("name")));
								return tag;
							})
							.toList();
				}

				if(item.get("genres", item) instanceof NativeArray array) {
					result.genres = stream(array)
							.filter(jsTag -> !JsBridge.isNullJs(jsTag))
							.map(Object::toString)
							.toList();
				}

				if(!JsBridge.isNullJs(item.get("status"))) {
					result.status = switch(item.get("status").toString().toLowerCase(Locale.ROOT)) {
						case "cancelled" -> CatalogMedia.MediaStatus.CANCELLED;
						case "coming_soon" -> CatalogMedia.MediaStatus.COMING_SOON;
						case "ongoing" -> CatalogMedia.MediaStatus.ONGOING;
						case "paused" -> CatalogMedia.MediaStatus.PAUSED;
						case "completed" -> CatalogMedia.MediaStatus.COMPLETED;
						default -> CatalogMedia.MediaStatus.UNKNOWN;
					};
				}

				if(!JsBridge.isNullJs(item.get("type"))) {
					result.type = switch(item.get("type").toString().toLowerCase(Locale.ROOT)) {
						case "movie" -> CatalogMedia.MediaType.MOVIE;
						case "book" -> CatalogMedia.MediaType.BOOK;
						case "tv" -> CatalogMedia.MediaType.TV;
						case "post" -> CatalogMedia.MediaType.POST;
						default -> null;
					};
				}

				if(item.has("title", item)) {
					var title = item.get("title");

					if(!JsBridge.isNullJs(title)) {
						//result.setTitle(title.toString());
					}
				}

				if(item.get("ids") instanceof NativeObject ids) {
					for(var entry : ids.entrySet()) {
						if(JsBridge.isNullJs(entry.getKey())) continue;
						if(JsBridge.isNullJs(entry.getValue())) continue;

						result.setId(entry.getKey().toString(), entry.getValue().toString());
					}
				}

				if(item.get("titles") instanceof NativeArray titles) {
					result.setTitles(stream(titles)
							.filter(jsTag -> !JsBridge.isNullJs(jsTag))
							.map(Object::toString)
							.toList());
				}

				results.add(result);
			}

			return CatalogSearchResults.of(results, booleanFromJs(o.get("hasNextPage")));
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public AsyncFuture<List<CatalogVideoFile>> getVideoFiles(SettingsList filters) {
		return thread(() -> {
			var episode = filters.require(
					ExtensionConstants.FILTER_EPISODE).parseJsonValue(CatalogVideo.class);

			var o = this.<List<ScriptableObject>>
					runFunction("aweryMediaVideos", episode).await();

			return stream(o)
					.map(videoObject -> new CatalogVideoFile(
							stringFromJs(videoObject.get("title")),
							stringFromJs(videoObject.get("url")),
							null,
							stream((List<ScriptableObject>) videoObject.get("subtitles"))
									.map(subtitleObject -> new CatalogSubtitle(
											stringFromJs(subtitleObject.get("title")),
											stringFromJs(subtitleObject.get("url"))))
									.toList()))
					.toList();
		});
	}

	private void logOut(@NonNull ResponseCallback<Boolean> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryLogOut") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[]{ (JsCallback<Boolean>) (o, e) -> {
						if(e != null) {
							callback.onFailure(new JsException(e));
							return;
						}

						callback.onSuccess(true);
					}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException(
						"\"aweryLogOut\" is not a function or isn't defined!"));
			}
		});
	}

	*/
/*private void getLoginScreen(@NonNull ResponseCallback<Map<String, String>> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryLoginScreen") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[]{ (JsCallback<ScriptableObject>) (o, e) -> {
						if(e != null) {
							callback.onFailure(new JsException(e));
							return;
						}

						var map = new HashMap<String, String>();

						for(var id : o.getIds()) {
							map.put(id.toString(), o.get(id).toString());
						}

						callback.onSuccess(map);
					}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException(
						"\"aweryLoginScreen\" is not a function or isn't defined!"));
			}
		});
	}*//*


	@Override
	public AsyncFuture<List<? extends CatalogVideo>> getVideos(@NonNull SettingsList filters) {
		return thread(() -> {
			var page = filters.require(ExtensionConstants.FILTER_PAGE).getIntegerValue();

			var media = filters.require(
					ExtensionConstants.FILTER_MEDIA).parseJsonValue(CatalogMedia.class);

			var o = this.<List<ScriptableObject>>
					runFunction("aweryMediaEpisodes", page, media).await();

			return stream(o)
					.map(item -> new CatalogVideo(
							stringFromJs(item.get("title")),
							stringFromJs(item.get("url")),
							stringFromJs(item.get("banner")),
							stringFromJs(item.get("description")),
							longFromJs(item.get("releaseDate")),
							floatFromJs(item.get("number"))
					)).toList();
		});
	}

	@NonNull
	private <T> AsyncFuture<T> runFunction(String name, SettingsList filters) {
		return runFunction(name, this.context.newArray(scope, stream(filters)
				.map(filter -> JsSettingsItem.toJs(filter, this.context, scope))
				.toArray()));
	}

	@NonNull
	private <T> AsyncFuture<T> runFunction(String name, Object... args) {
		return AsyncUtils.controllableFuture(future -> manager.postRunnable(() -> {
			if(scope.get(name) instanceof Function fun) {
				try {
					var newArray = new Object[args.length + 1];
					System.arraycopy(args, 0, newArray, 0, args.length);

					newArray[args.length] = (JsCallback<T>) (t, e) -> {
						if(t != null) {
							try {
								future.complete(t);
							} catch(Throwable ex) {
								future.fail(ex);
								return;
							}
						}

						if(e != null) {
							future.fail(new JsException(e));
							return;
						}

						future.fail(new NullPointerException("No result and no error was provided!"));
					};

					fun.call(this.context, scope, null, newArray);
				} catch(Throwable e) {
					future.fail(e);
				}
			} else {
				future.fail(new UnimplementedException("\"" + name + "\" function not found!"));
			}
		}));
	}

	*/
/**
	 * Bridge callback for JavaScript
	 * @author MrBoomDev
	 *//*

	@SuppressWarnings("unused")
	private interface JsCallback<T> {
		default void resolve(T t) {
			onResult(t, null);
		}

		default void reject(ScriptableObject e) {
			onResult(null, e);
		}

		void onResult(T t, ScriptableObject e);
	}

	@Override
	public Set<String> getFeatures() {
		return features;
	}

	@Override
	public AdultContentMode getAdultContentMode() {
		return adultContentMode;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}
}*/