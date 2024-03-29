package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.app.AweryApp.getActivity;
import static com.mrboomdev.awery.app.AweryApp.stream;
import static com.mrboomdev.awery.app.AweryApp.toast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;

import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.request.ReadMediaCommentsRequest;
import com.mrboomdev.awery.extensions.support.template.CatalogComment;
import com.mrboomdev.awery.extensions.support.template.CatalogEpisode;
import com.mrboomdev.awery.extensions.support.template.CatalogFilter;
import com.mrboomdev.awery.extensions.support.template.CatalogMedia;
import com.mrboomdev.awery.extensions.support.template.CatalogSubtitle;
import com.mrboomdev.awery.extensions.support.template.CatalogVideo;
import com.mrboomdev.awery.ui.activity.LoginActivity;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.util.exceptions.JsException;

import org.jetbrains.annotations.Contract;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class JsProvider extends ExtensionProvider {
	private static final String TAG = "JsProvider";
	protected String id, version;
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
		this.manager = manager;
		this.context = rhinoContext;
		this.androidContext = androidContext;
		this.scope = rhinoContext.initStandardObjects();

		var bridge = Context.javaToJS(new JsBridge(manager, this, scope), scope);
		ScriptableObject.putConstProperty(scope, "Awery", bridge);

		rhinoContext.evaluateString(scope, script, null, 1,null);

		if(!didInit) {
			throw new JsException("It looks like your forgot to call the \"setManifest\"!");
		}
	}

	protected void finishInit(JsBridge bridge, @NonNull ScriptableObject obj) {
		var features = new ArrayList<Integer>();

		this.name = (String) obj.get("title");
		this.id = (String) obj.get("id");
		this.version = (String) obj.get("version");

		if(id == null) {
			throw new NullPointerException("id is null!");
		}

		for(var feature : (NativeArray) obj.get("features")) {
			features.add(switch((String) feature) {
				case "media_comments_read" -> FEATURE_READ_MEDIA_COMMENTS;
				case "media_comments_write" -> FEATURE_WRITE_MEDIA_COMMENTS;
				case "media_comments_sort" -> FEATURE_COMMENTS_SORT;

				case "media_watch" -> FEATURE_WATCH_MEDIA;
				case "media_read" -> FEATURE_READ_MEDIA;

				case "account_login" -> FEATURE_LOGIN;
				case "account_track" -> FEATURE_TRACK;

				default -> {
					toast("Unknown feature: " + feature);
					yield 0;
				}
			});
		}

		bridge.prefs = AwerySettings.getInstance(androidContext, "JsBridge-" + id);
		androidContext = null;

		this.FEATURES = Collections.unmodifiableList(features);
		this.didInit = true;
	}

	@Override
	public void getSettings(android.content.Context context, @NonNull ResponseCallback<SettingsItem> callback) {
		var items = new ArrayList<SettingsItem>();

		if(hasFeature(FEATURE_LOGIN)) {
			var isLoggedIn = new AtomicReference<Boolean>();

			isLoggedIn(new ResponseCallback<>() {
				@Override
				public void onSuccess(Boolean aBoolean) {
					isLoggedIn.set(aBoolean);
				}

				@Override
				public void onFailure(Throwable e) {
					isLoggedIn.set(false);

					Log.e(TAG, "Failed to check if user is logged in", e);
					toast("Failed to check if user is logged in");
				}
			});

			while(isLoggedIn.get() == null);

			items.add(new CustomSettingsItem() {

				@Override
				public String getTitle(android.content.Context context) {
					return isLoggedIn.get() ? "Logout" : "Login";
				}

				@Override
				public void onClick(android.content.Context context) {
					if(isLoggedIn.get()) {
						logOut(new ResponseCallback<>() {

							@Override
							public void onSuccess(Boolean aBoolean) {
								toast("Logged out successfully");
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

				@Override
				public SettingsItemType getType() {
					return SettingsItemType.ACTION;
				}
			});
		}

		var root = new SettingsItem() {

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
		};

		callback.onSuccess(root);
	}

	private void login(Map<String, String> params, @NonNull ResponseCallback<Boolean> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryLogin") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[]{ params, (Callback<Boolean>) (o, e) -> {
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
				callback.onFailure(new NullPointerException("aweryLogin is not a function or isn't defined!"));
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

			callback.onFailure(new NullPointerException("aweryIsLoggedIn is not a function or isn't defined!"));
		});
	}

	@Override
	public void readMediaComments(ReadMediaCommentsRequest request, @NonNull ResponseCallback<CatalogComment> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryReadMediaComments") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] { request, (Callback<ScriptableObject>) (o, e) -> {
						if(e != null) {
							callback.onFailure(new JsException(e));
							return;
						}

						var comment = new CatalogComment();
						comment.authorName = o.has("authorName", o) ? o.get("authorName").toString() : null;
						comment.authorAvatar = o.has("authorAvatar", o) ? o.get("authorAvatar").toString() : null;
						comment.text = o.has("text", o) ? o.get("text").toString() : null;
						callback.onSuccess(comment);
					}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new NullPointerException("aweryReadMediaComments is not a function or isn't defined!"));
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public void search(CatalogFilter filter, @NonNull ResponseCallback<List<? extends CatalogMedia>> callback) {
		manager.postRunnable(() -> {
			if(scope.get("awerySearch") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] { filter, (Callback<List<ScriptableObject>>) (o, e) -> {
						if(e != null) {
							callback.onFailure(new JsException(e));
							return;
						}

						var results = new ArrayList<CatalogMedia>();

						for(var item : o) {
							var result = new CatalogMedia(manager.getId(), id, (String) item.get("id"));
							result.setTitles((Collection<String>) item.get("titles"));
							result.banner = (String) item.get("banner");
							result.description = (String) item.get("description");

							results.add(result);
						}

						callback.onSuccess(results);
					}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new NullPointerException("awerySearch is not a function or isn't defined!"));
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public void getVideos(CatalogEpisode episode, @NonNull ResponseCallback<List<CatalogVideo>> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryGetVideos") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] { episode, (Callback<List<ScriptableObject>>) (o, e) -> {
						if(e != null) {
							callback.onFailure(new JsException(e));
							return;
						}

						callback.onSuccess(stream(o)
								.map(videoObject -> new CatalogVideo(
										(String) videoObject.get("title"),
										(String) videoObject.get("url"),
										null,
										stream((List<ScriptableObject>) videoObject.get("subtitles"))
												.map(subtitleObject -> new CatalogSubtitle(
														(String) subtitleObject.get("title"),
														(String) subtitleObject.get("url")))
												.toList()))
								.toList());
					}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new NullPointerException("aweryGetVideos is not a function or isn't defined!"));
			}
		});
	}

	private void logOut(@NonNull ResponseCallback<Boolean> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryLogOut") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[]{(Callback<Boolean>) (o, e) -> {
						if(e != null) {
							callback.onFailure(new JsException(e));
							return;
						}

						callback.onSuccess(true);
					}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			}
		});
	}

	private void getLoginScreen(@NonNull ResponseCallback<Map<String, String>> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryLoginScreen") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[]{(Callback<ScriptableObject>) (o, e) -> {
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
				callback.onFailure(new NullPointerException("aweryLoginScreen is not a function or isn't defined!"));
			}
		});
	}

	@Override
	public void getEpisodes(int page, CatalogMedia media, @NonNull ResponseCallback<List<? extends CatalogEpisode>> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryGetEpisodes") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] { page, media, (Callback<List<ScriptableObject>>) (o, e) -> {
						if(e != null) {
							callback.onFailure(new JsException(e));
							return;
						}

						callback.onSuccess(stream(o)
								.map(item -> new CatalogEpisode(
										(String) item.get("title"),
										(String) item.get("url"),
										(String) item.get("banner"),
										(String) item.get("description"),
										(Long) item.get("releaseDate"),
										(Float) item.get("number"))).toList());
					}});
				} catch(Throwable e) {
					callback.onFailure(e);
				}
			} else {
				callback.onFailure(new NullPointerException("aweryGetEpisodes is not a function or isn't defined!"));
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