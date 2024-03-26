package com.mrboomdev.awery.extensions.support.js;

import com.mrboomdev.awery.util.Callbacks;

public class JsTask {
	protected static final int LOAD_ALL_EXTENSIONS = 1;
	protected static final int LOAD_EXTENSION = 2;
	private final int type;
	private final Object extra;
	private final Callbacks.Callback1<Object> callback;

	protected JsTask(int type, Object extra, Callbacks.Callback1<Object> callback) {
		this.type = type;
		this.extra = extra;
		this.callback = callback;
	}

	protected Callbacks.Callback1<Object> getCallback() {
		return callback;
	}

	protected Object getExtra() {
		return extra;
	}

	protected int getTaskType() {
		return type;
	}
}