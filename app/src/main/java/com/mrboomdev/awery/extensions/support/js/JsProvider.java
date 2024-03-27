package com.mrboomdev.awery.extensions.support.js;

import static com.mrboomdev.awery.app.AweryApp.stream;
import static com.mrboomdev.awery.app.AweryApp.toast;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.extensions.ExtensionProvider;
import com.mrboomdev.awery.extensions.support.template.CatalogComment;
import com.mrboomdev.awery.extensions.support.template.CatalogEpisode;
import com.mrboomdev.awery.extensions.support.template.CatalogFilter;
import com.mrboomdev.awery.extensions.support.template.CatalogMedia;
import com.mrboomdev.awery.extensions.support.template.CatalogSubtitle;
import com.mrboomdev.awery.extensions.support.template.CatalogVideo;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JsProvider extends ExtensionProvider {
	private final List<Integer> FEATURES;
	private static final Object[] EMPTY_ARGS = new Object[0];
	private final ScriptableObject scope;
	private final org.mozilla.javascript.Context context;
	private final String name;
	private final JsManager manager;
	protected final String id, version;

	public JsProvider(
			JsManager manager,
			android.content.Context androidContext,
			@NonNull Context rhinoContext,
			@NonNull String script
	) {
		var features = new ArrayList<Integer>();
		this.manager = manager;
		this.context = rhinoContext;
		this.scope = rhinoContext.initSafeStandardObjects();

		rhinoContext.evaluateString(scope, script, null, 1,null);

		if(scope.get("aweryManifest") instanceof Function fun) {
			var obj = (ScriptableObject) fun.call(rhinoContext, scope, null, EMPTY_ARGS);
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
		} else {
			throw new IllegalStateException("aweryManifest is not a function or isn't defined!");
		}

		var bridge = Context.javaToJS(new JsBridge(manager, androidContext, id), scope);
		ScriptableObject.putConstProperty(scope, "Awery", bridge);

		this.FEATURES = Collections.unmodifiableList(features);
	}

	@Override
	public void readMediaComments(
			CatalogMedia media,
			CatalogEpisode episode,
			@NonNull ResponseCallback<CatalogComment> callback
	) {
		manager.postRunnable(() -> {
			if(scope.get("aweryReadMediaComments") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] { media, episode, (Callback<ScriptableObject>) (o, e) -> {
						if(e != null) {
							callback.onFailure(e);
							return;
						}

						var comment = new CatalogComment();
						comment.authorName = (String) o.get("authorName");
						comment.text = (String) o.get("text");
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
							callback.onFailure(e);
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
							callback.onFailure(e);
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

	@Override
	public void getEpisodes(int page, CatalogMedia media, @NonNull ResponseCallback<List<? extends CatalogEpisode>> callback) {
		manager.postRunnable(() -> {
			if(scope.get("aweryGetEpisodes") instanceof Function fun) {
				try {
					fun.call(context, scope, null, new Object[] { page, media, (Callback<List<ScriptableObject>>) (o, e) -> {
						if(e != null) {
							callback.onFailure(e);
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

		default void reject(Exception e) {
			onResult(null, e);
		}

		void onResult(T t, Exception e);
	}

	@Override
	public Collection<Integer> getFeatures() {
		return FEATURES;
	}

	@Override
	public String getName() {
		return name;
	}
}