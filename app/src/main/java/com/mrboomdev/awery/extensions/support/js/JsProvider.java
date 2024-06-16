package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.booleanFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.floatFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.fromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.isNullJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.listFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.longFromJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.returnIfNotNullJs;
import static com.mrboomdev.awery.extensions.support.js.JsBridge.stringFromJs;
import static com.mrboomdev.awery.util.NiceUtils.stream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.Constants;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.ObservableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.extensions.Extension;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.data.CatalogComment;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.data.CatalogFeed;
import com.mrboomdev.awery.extensions.data.CatalogList;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSearchResults;
import com.mrboomdev.awery.extensions.data.CatalogSubtitle;
import com.mrboomdev.awery.extensions.data.CatalogTag;
import com.mrboomdev.awery.extensions.data.CatalogTrackingOptions;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.request.PostMediaCommentRequest;
import com.mrboomdev.awery.extensions.request.ReadMediaCommentsRequest;
import com.mrboomdev.awery.ui.activity.LoginActivity;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.util.ParserAdapter;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class JsProvider extends ExtensionProvider {
	private static final String TAG = "JsProvider";
	protected String id, version, script;
	protected Extension extension;
	private  List<Integer> FEATURES;
	private final ScriptableObject scope;
	private final org.mozilla.javascript.Context context;
	private android.content.Context androidContext;
	private String name;
	private final JsManager manager;
	private boolean didInit;

	public JsProvider(
			JsManager manager,
			android.content.Context androidContext,
			@NonNull Context rhinoContext,
			@NonNull String script
	) {
		super(manager, null);

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

	protected void finishInit(JsBridge bridge, @NonNull NativeObject obj) {
		var features = new ArrayList<Integer>();

		this.id = obj.get("id").toString();
		this.name = obj.has("title", obj) ? obj.get("title").toString() : id;

		this.version = obj.get("version").toString();

		if(id == null) {
			throw new NullPointerException("id is null!");
		}

		for(var feature : (NativeArray) obj.get("features")) {
			features.add(switch((String) feature) {
				case "media_comments" -> FEATURE_MEDIA_COMMENTS;
				case "media_comments_sort" -> FEATURE_COMMENTS_SORT;
				case "media_comments_per_episode" -> FEATURE_COMMENTS_PER_EPISODE;

				case "media_watch" -> FEATURE_MEDIA_WATCH;
				case "media_read" -> FEATURE_MEDIA_READ;
				case "media_report" -> FEATURE_MEDIA_REPORT;

				case "search_tags" -> FEATURE_TAGS_SEARCH;
				case "search_media" -> FEATURE_MEDIA_SEARCH;

				case "account_login" -> FEATURE_LOGIN;
				case "account_track" -> FEATURE_TRACK;

				case "changelog" -> FEATURE_CHANGELOG;
				case "feeds" -> FEATURE_FEEDS;

				default -> 0;
			});
		}

		var appContext = androidContext.getApplicationContext();
		bridge.context = new WeakReference<>(appContext);
		androidContext = null;

		this.FEATURES = Collections.unmodifiableList(features);
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

	@Override
	public void getSettings(android.content.Context context, @NonNull ResponseCallback<SettingsItem> callback) {
		var root = new Settings();

		if(hasFeature(FEATURE_LOGIN)) {
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
			while(isLoggedIn.get() == null);

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
								runOnUiThread(() -> root.onSettingChange(setting, 0));
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
								if(getActivity(context) instanceof SettingsActivity settingsActivity) {
									settingsActivity.addActivityResultCallback(new ActivityResultCallback<>() {
										@Override
										public void onActivityResult(ActivityResult result) {
											settingsActivity.removeActivityResultCallback(this);
											if(result.getResultCode() != Activity.RESULT_OK) return;

											var params = new HashMap<String, String>();
											var data = Objects.requireNonNull(result.getData()).getExtras();

											for(var key : Objects.requireNonNull(data).keySet()) {
												params.put(key, data.getString(key));
											}

											login(params, new ResponseCallback<>() {
												@Override
												public void onSuccess(Boolean aBoolean) {
													toast("Logged in successfully");
													reload.run();
													runOnUiThread(() -> root.onSettingChange(setting, 0));
												}

												@Override
												public void onFailure(Throwable e) {
													Log.e(TAG, "Failed to login", e);
													toast("Failed to login");
												}
											});
										}
									});

									var intent = new Intent(context, LoginActivity.class);
									var extras = new Bundle();

									for(var entry : stringStringMap.entrySet()) {
										extras.putString(entry.getKey(), entry.getValue());
									}

									intent.putExtras(extras);
									settingsActivity.getActivityResultLauncher().launch(intent);
								} else {
									throw new IllegalArgumentException("Activity is not a SettingsActivity!");
								}
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

		root.addItem(new CustomSettingsItem(SettingsItemType.ACTION) {

			@Override
			public String getTitle(android.content.Context context) {
				return context.getString(R.string.uninstall_extension);
			}

			@Override
			public void onClick(android.content.Context context) {
				var activity = getActivity(context);

				manager.uninstallExtension(context, id);
				toast("Uninstalled successfully");

				if(activity != null) {
					var settingsScreen = root.getParent().getParent();
					activity.finish();

					if(settingsScreen instanceof ObservableSettingsItem listenable) {
						var wasIndex = settingsScreen.getItems().indexOf(root.getParent());
						runOnUiThread(() -> listenable.onSettingRemoval(root.getParent(), wasIndex));
					} else {
						throw new IllegalStateException("Settings screen doesn't implement ObservableSettingsItem!");
					}
				}
			}
		});

		callback.onSuccess(root);
	}

	@Override
	public void voteComment(CatalogComment comment, @NonNull ResponseCallback<CatalogComment> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryVoteComment") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] {
							JsComment.createJsComment(context, scope, comment),
							(Callback<NativeObject>) (o, e) -> {
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
				callback.onFailure(new UnimplementedException("\"aweryVoteComment\" is not a function or isn't defined!"));
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public void getFeeds(@NonNull ResponseCallback<List<CatalogFeed>> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryFeeds") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] {
							(Callback<NativeArray>) (o, e) -> {
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
	}

	@Override
	public void editComment(CatalogComment oldComment, CatalogComment newComment, @NonNull ResponseCallback<CatalogComment> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryEditComment") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] {
							JsComment.createJsComment(this.context, this.scope, oldComment),
							JsComment.createJsComment(this.context, this.scope, newComment),
							(Callback<NativeObject>) (o, e) -> {
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
				callback.onFailure(new UnimplementedException("\"aweryEditComment\" is not a function or isn't defined!"));
			}
		});
	}

	@Override
	public void deleteComment(CatalogComment comment, @NonNull ResponseCallback<Boolean> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryDeleteComment") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] {
							JsComment.createJsComment(this.context, this.scope, comment),
							(Callback<Boolean>) (o, e) -> {
								if(e != null) {
									callback.onFailure(new JsException(e));
									return;
								}

								callback.onSuccess(o);
							}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException("\"aweryDeleteComment\" is not a function or isn't defined!"));
			}
		});
	}

	@Override
	public void getChangelog(@NonNull ResponseCallback<String> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryChangelog") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[]{
							(Callback<Object>) (o, e) -> {
								if(e != null) {
									callback.onFailure(new JsException(e));
									return;
								}

								callback.onSuccess(o.toString());
							}
					});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException("\"aweryChangelog\" is not a function or isn't defined!"));
			}
		});
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

					fun.call(context, scope, null, new Object[]{ media, input, (Callback<NativeObject>) (o, e) -> {
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

						result.progress = returnIfNotNullJs(fromJs(o.get("progress", o), Float.class), Constants::returnMe);
						result.score = returnIfNotNullJs(fromJs(o.get("score", o), Float.class), Constants::returnMe);

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

					fun.call(context, scope, null, new Object[]{ obj, (Callback<Boolean>) (o, e) -> {
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

	@Override
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
							(Callback<NativeObject>) (o, e) -> {
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
	}

	@Override
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

					fun.call(context, scope, null, new Object[] { jsRequest, (Callback<NativeObject>) (o, e) -> {
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
	}

	@Override
	@SuppressWarnings("unchecked")
	public void getFilters(@NonNull ResponseCallback<List<SettingsItem>> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryFilters") instanceof Function fun) {
				try {
					fun.call(this.context, scope, null, new Object[] { (Callback<NativeArray>) (o, e) -> {
						if(e != null) {
							callback.onFailure(new JsException(e));
							return;
						}

						callback.onSuccess(stream(o)
								.map(item -> JsSettingsItem.fromJs((NativeObject) item))
								.toList());
					}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException("\"aweryFilters\" function not found!"));
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public void searchMedia(
			android.content.Context context,
			List<SettingsItem> filters,
			@NonNull ResponseCallback<CatalogSearchResults<? extends CatalogMedia>> callback
	) {
		manager.postRunnable(() -> {
			if(scope.get("awerySearchMedia") instanceof Function fun) {
				try {
					var jsFilters = this.context.newArray(scope, stream(filters)
							.map(filter -> JsSettingsItem.toJs(filter, this.context, fun))
							.toArray());

					fun.call(this.context, scope, null, new Object[] { jsFilters, (Callback<NativeObject>) (o, e) -> {
						if(e != null) {
							callback.onFailure(new JsException(e));
							return;
						}

						if(((NativeArray)o.get("items", o)).isEmpty()) {
							callback.onFailure(new ZeroResultsException("Zero results",
									R.string.no_media_found));
							return;
						}

						var results = new ArrayList<CatalogMedia>();

						for(var arrayItem : (NativeArray) o.get("items", o)) {
							if(JsBridge.isNullJs(arrayItem)) continue;
							var item = (NativeObject) arrayItem;

							var result = new CatalogMedia(manager.getId(), id, item.get("id").toString());
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

											tag.setName(jsTag.get("name").toString());
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
									result.setTitle(title.toString());
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

						callback.onSuccess(CatalogSearchResults.of(
								results, booleanFromJs(o.get("hasNextPage"))));
					}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException("\"aweryMediaSearch\" function not found!"));
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public void getVideos(CatalogEpisode episode, @NonNull ResponseCallback<List<CatalogVideo>> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryMediaVideos") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] {
							episode,
							(Callback<List<ScriptableObject>>) (o, e) -> {
								if(e != null) {
									callback.onFailure(new JsException(e));
									return;
								}

								callback.onSuccess(stream(o)
										.map(videoObject -> new CatalogVideo(
												stringFromJs(videoObject.get("title")),
												stringFromJs(videoObject.get("url")),
												null,
												stream((List<ScriptableObject>) videoObject.get("subtitles"))
														.map(subtitleObject -> new CatalogSubtitle(
																stringFromJs(subtitleObject.get("title")),
																stringFromJs(subtitleObject.get("url"))))
														.toList()))
										.toList());
							}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException(
						"\"aweryMediaVideos\" is not a function or isn't defined!"));
			}
		});
	}

	private void logOut(@NonNull ResponseCallback<Boolean> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryLogOut") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[]{ (Callback<Boolean>) (o, e) -> {
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

	private void getLoginScreen(@NonNull ResponseCallback<Map<String, String>> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryLoginScreen") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[]{ (Callback<ScriptableObject>) (o, e) -> {
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
	}

	@Override
	public void getEpisodes(
			int page,
			CatalogMedia media,
			@NonNull ResponseCallback<List<? extends CatalogEpisode>> callback
	) {
		manager.postRunnable(() -> {
			if(scope.get("aweryMediaEpisodes") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] {
							page, media,
							(Callback<List<ScriptableObject>>) (o, e) -> {
								if(e != null) {
									callback.onFailure(new JsException(e));
									return;
								}

								callback.onSuccess(stream(o)
										.map(item -> new CatalogEpisode(
												stringFromJs(item.get("title")),
												stringFromJs(item.get("url")),
												stringFromJs(item.get("banner")),
												stringFromJs(item.get("description")),
												longFromJs(item.get("releaseDate")),
												floatFromJs(item.get("number"))
										)).toList());
							}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new UnimplementedException(
						"\"aweryMediaEpisodes\" is not a function or isn't defined!"));
			}
		});
	}

	/**
	 * Bridge callback for JavaScript
	 * @author MrBoomDev
	 */
	@SuppressWarnings("unused")
	private interface Callback<T> {
		default void resolve(T t) {
			onResult(t, null);
		}

		default void reject(ScriptableObject e) {
			onResult(null, e);
		}

		void onResult(T t, ScriptableObject e);
	}

	@Override
	public Collection<Integer> getFeatures() {
		return FEATURES;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}
}