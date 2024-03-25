package com.mrboomdev.awery.util.observable;

public interface Observable<T> {
	int ADDED = 1, CHANGED = 2, REMOVED = 3;

	void observe(Listener<T> lister);
	void removeObserver(Listener<T> listener);

	interface Listener<T> {
		void onEvent(T extra, int event);
	}
}