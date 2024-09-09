package com.mrboomdev.awery.util.async;

abstract class AsyncFutureNow<T> extends AsyncFuture<T> {
	private Throwable t;

	@Override
	public void addCallback(Callback<T> callback) {
		if(getThrowable() != null) {
			callback.onFailure(getThrowable());
			return;
		}

		if(t != null) {
			callback.onFailure(t);
			return;
		}

		if(getResult() != null) {
			try {
				callback.onSuccess(getResult());
			} catch(Throwable e) {
				t = e;
				callback.onFailure(t);
			}
		}
	}

	@Override
	public T await() throws Throwable {
		if(getThrowable() != null) {
			throw getThrowable();
		}

		return getResult();
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}
}