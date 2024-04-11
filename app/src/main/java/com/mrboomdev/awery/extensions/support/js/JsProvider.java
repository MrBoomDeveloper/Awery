package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.app.AweryApp.stream;
import static com.mrboomdev.awery.app.AweryApp.toast;
import static com.mrboomdev.awery.app.AweryLifecycle.getActivity;
import static com.mrboomdev.awery.app.AweryLifecycle.runOnUiThread;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;

import com.mrboomdev.awery.data.settings.AwerySettings;
import com.mrboomdev.awery.data.settings.CustomSettingsItem;
import com.mrboomdev.awery.data.settings.ListenableSettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItem;
import com.mrboomdev.awery.data.settings.SettingsItemType;
import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.data.CatalogComment;
import com.mrboomdev.awery.extensions.data.CatalogEpisode;
import com.mrboomdev.awery.extensions.data.CatalogFilter;
import com.mrboomdev.awery.extensions.data.CatalogMedia;
import com.mrboomdev.awery.extensions.data.CatalogSubtitle;
import com.mrboomdev.awery.extensions.data.CatalogVideo;
import com.mrboomdev.awery.extensions.request.ReadMediaCommentsRequest;
import com.mrboomdev.awery.ui.activity.LoginActivity;
import com.mrboomdev.awery.ui.activity.settings.SettingsActivity;
import com.mrboomdev.awery.util.Callbacks;
import com.mrboomdev.awery.util.exceptions.JsException;

import org.jetbrains.annotations.Contract;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
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
	protected String id, version, script;
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
		this.script = script;
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
				case "media_comments" -> FEATURE_MEDIA_COMMENTS;
				case "media_comments_sort" -> FEATURE_COMMENTS_SORT;
				case "media_comments_vote" -> FEATURE_COMMENTS_VOTE;

				case "media_watch" -> FEATURE_MEDIA_WATCH;
				case "media_read" -> FEATURE_MEDIA_READ;

				case "account_login" -> FEATURE_LOGIN;
				case "account_track" -> FEATURE_TRACK;

				default -> 0;
			});
		}

		bridge.prefs = AwerySettings.getInstance(androidContext, "JsBridge-" + id);
		androidContext = null;

		this.FEATURES = Collections.unmodifiableList(features);
		this.didInit = true;
	}

	private class Settings extends SettingsItem implements ListenableSettingsItem {
		private final List<SettingsItem> items = new ArrayList<>();
		private Callbacks.Callback2<SettingsItem, Integer> newItemListener, editItemListener, deleteItemListener;

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

		@Override
		public void setNewItemListener(Callbacks.Callback2<SettingsItem, Integer> listener) {
			this.newItemListener = listener;
		}

		@Override
		public void setRemovalItemListener(Callbacks.Callback2<SettingsItem, Integer> listener) {
			this.deleteItemListener = listener;
		}

		@Override
		public void setChangeItemListener(Callbacks.Callback2<SettingsItem, Integer> listener) {
			this.editItemListener = listener;
		}

		@Override
		public void onNewItem(SettingsItem item, int position) {
			if(newItemListener != null) {
				newItemListener.run(item, position);
			}
		}

		@Override
		public void onRemoval(SettingsItem item, int position) {
			if(deleteItemListener != null) {
				deleteItemListener.run(item, position);
			}
		}

		@Override
		public void onChange(SettingsItem item, int position) {
			if(editItemListener != null) {
				editItemListener.run(item, position);
			}
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
					toast("Failed to check if user is logged in");
				}
			});

			reload.run();
			while(isLoggedIn.get() == null);

			root.addItem(new CustomSettingsItem(SettingsItemType.ACTION) {

				@Override
				public String getTitle(android.content.Context context) {
					return isLoggedIn.get() ? "Logout" : "Login";
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
								runOnUiThread(() -> root.onChange(setting, 0));
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
													runOnUiThread(() -> root.onChange(setting, 0));
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
				return "Uninstall extension";
			}

			@Override
			public void onClick(android.content.Context context) {
				var activity = getActivity(context);

				var extensions = new ArrayList<>(manager.getAllExtensions());
				var wasIndex = extensions.indexOf(manager.getExtension(id));

				manager.uninstallExtension(context, id);
				toast("Uninstalled successfully");

				if(activity != null) {
					activity.finish();

					if(root.getParent().getParent() instanceof ListenableSettingsItem listenable) {
						runOnUiThread(() -> listenable.onRemoval(root, wasIndex));
					}
				}
			}
		});

		callback.onSuccess(root);
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
	public void postMediaComment(
			CatalogComment parent,
			CatalogComment comment,
			@NonNull ResponseCallback<CatalogComment> callback
	) {
		manager.postRunnable(() -> {
			if(scope.get("aweryPostMediaComment") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] {
							JsComment.createJsComment(context, scope, parent),
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
				callback.onFailure(new NullPointerException("aweryPostMediaComment is not a function or isn't defined!"));
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
					jsRequest.put("episode", jsRequest, request.getEpisode());
					jsRequest.put("media", jsRequest, request.getMedia());

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
				callback.onFailure(new NullPointerException("aweryReadMediaComments is not a function or isn't defined!"));
			}
		});
	}

	@Override
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
							result.banner = item.has("banner", item) ? item.get("banner").toString() : null;
							result.country = item.has("country", item) ? item.get("country").toString() : null;
							result.description = item.has("description", item) ? item.get("description").toString() : null;

							result.setTitles(item.has("titles", item) ? stream(item.get("titles"))
									.map(Object::toString)
									.toList() : null);

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
			if(scope.get("aweryMediaVideos") instanceof Function fun) {
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
				callback.onFailure(new NullPointerException("aweryMediaVideos is not a function or isn't defined!"));
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
			if(scope.get("aweryMediaEpisodes") instanceof Function fun) {
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
				callback.onFailure(new NullPointerException("aweryMediaEpisodes is not a function or isn't defined!"));
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