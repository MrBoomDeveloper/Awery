package com.mrboomdev.awery.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObservableArrayList<E> extends ArrayList<E> implements ObservableList<E> {
	private List<AddObserver<E>> addObservers;
	private List<RemoveObserver<E>> removeObservers;
	private List<ChangeObserver<E>> changeObservers;

	public ObservableArrayList(@NonNull Collection<? extends E> c) {
		super(c);
	}

	public ObservableArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	public ObservableArrayList() {
		super();
	}

	@Override
	public void observeAdditions(AddObserver<E> callback) {
		if(addObservers == null) {
			addObservers = new ArrayList<>();
		}

		addObservers.add(callback);
	}

	@Override
	public void observeChanges(ChangeObserver<E> callback) {
		if(changeObservers == null) {
			changeObservers = new ArrayList<>();
		}

		changeObservers.add(callback);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean addAll(@NonNull Collection<? extends E> c, boolean callObservers) {
		var result = super.addAll(c);

		if(callObservers) {
			var array = c.toArray();
			var lastIndex = size() - 1;

			for(int i = 0; i < array.length; i++) {
				var item = array[i];
				var newIndex = i + lastIndex;

				for(var observer : addObservers) {
					if(item == null) {
						observer.added(null, newIndex);
						continue;
					}

					observer.added((E) item, newIndex);
				}
			}
		}

		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean addAll(int index, @NonNull Collection<? extends E> c, boolean callObservers) {
		var result = super.addAll(index, c);

		if(callObservers) {
			var array = c.toArray();
			var lastIndex = size() - 1 + index;

			for(int i = 0; i < array.length; i++) {
				var item = array[i];
				var newIndex = i + lastIndex;

				for(var observer : addObservers) {
					if(item == null) {
						observer.added(null, newIndex);
						continue;
					}

					observer.added((E) item, newIndex);
				}
			}
		}

		return result;
	}

	@Override
	public void clear(boolean callObservers) {
		super.clear();

		if(callObservers) {
			for(var observer : removeObservers) {
				observer.removed(null, 0);
			}
		}
	}

	@Override
	public void observeRemovals(RemoveObserver<E> callback) {
		if(removeObservers == null) {
			removeObservers = new ArrayList<>();
		}

		removeObservers.add(callback);
	}

	@Override
	public void removeObserver(AddObserver<E> observer) {
		addObservers.remove(observer);
	}

	@Override
	public void removeObserver(RemoveObserver<E> observer) {
		removeObservers.remove(observer);
	}

	@Override
	public void removeObserver(ChangeObserver<E> observer) {
		changeObservers.remove(observer);
	}

	@Override
	public boolean add(E e, boolean callObservers) {
		boolean result = super.add(e);

		if(callObservers) {
			for(var observer : addObservers) {
				observer.added(e, size() - 1);
			}
		}

		return result;
	}

	@Override
	public void add(int index, E element, boolean callObservers) {
		super.add(index, element);

		if(callObservers) {
			for(var observer : addObservers) {
				observer.added(element, index);
			}
		}
	}

	@Override
	public E remove(int index, boolean callObservers) {
		var result = super.remove(index);

		if(callObservers) {
			for(var observer : removeObservers) {
				observer.removed(result, index);
			}
		}

		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(@Nullable Object o, boolean callObservers) {
		int index = indexOf(o);
		var result = super.remove(o);

		if(result && callObservers) {
			for(var observer : removeObservers) {
				observer.removed((E) o, index);
			}
		}

		return result;
	}

	@Override
	public E set(int index, E element, boolean callObservers) {
		var result = super.set(index, element);

		if(callObservers) {
			for(var observer : changeObservers) {
				observer.changed(element, result, index);
			}
		}

		return result;
	}
}