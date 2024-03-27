package com.mrboomdev.awery.extensions.support.js;

import com.mrboomdev.awery.util.Callbacks;

public class JsTask {
	protected static final int LOAD_ALL_EXTENSIONS = 1;
	protected static final int LOAD_EXTENSION = 2;
	protected static final int POST_RUNNABLE = 3;
	private final int type;
	private final Object[] args;
	private final Callbacks.Callback1<Object> callback;

	protected JsTask(int type, Callbacks.Callback1<Object> callback, Object... args) {
		this.type = type;
		this.args = args;
		this.callback = callback;
	}

	protected JsTask(Runnable runnable) {
		this.type = POST_RUNNABLE;
		this.args = new Object[0];
		this.callback = o -> runnable.run();
	}

	protected Callbacks.Callback1<Object> getCallback() {
		return callback;
	}

	protected Object[] getArgs() {
		return args;
	}

	protected int getTaskType() {
		return type;
	}
}