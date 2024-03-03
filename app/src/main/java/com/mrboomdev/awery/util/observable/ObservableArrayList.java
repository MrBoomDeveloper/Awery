package com.mrboomdev.awery.util.observable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObservableArrayList<E> extends ArrayList<E> implements ObservableList<E> {
	private List<AddObserver<E>> addObservers;
	private List<RemoveObserver<E>> removeObservers;
	private List<ChangeObserver<E>> changeObservers;
	private List<LargeChangeObserver> largeChangeObservers;

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
	public void observeLargeChanges(LargeChangeObserver callback) {
		if(largeChangeObservers == null) {
			largeChangeObservers = new ArrayList<>();
		}

		largeChangeObservers.add(callback);
	}

	@Override
	public void removeLargeChangeObserver(LargeChangeObserver callback) {
		if(largeChangeObservers != null) {
			largeChangeObservers.remove(callback);
		}
	}

	@Override
	public boolean addAll(@NonNull Collection<? extends E> c, boolean callObservers) {
		boolean callSmallObservers = true;

		if(callObservers && largeChangeObservers != null) {
			for(var observer : largeChangeObservers) {
				var result = observer.changed();
				if(result) callSmallObservers = false;
			}
		}

		for(var item : c) {
			if(!super.add(item)) {
				return false;
			}

			if(callObservers && callSmallObservers) {
				for(var observer : addObservers) {
					observer.added((E) item, size() - 1);
				}
			}
		}

		return true;
	}

	@Override
	public boolean addAll(int index, @NonNull Collection<? extends E> c, boolean callObservers) {
		int nextIndex = index;
		boolean callSmallObservers = true;

		if(callObservers && largeChangeObservers != null) {
			for(var observer : largeChangeObservers) {
				var result = observer.changed();
				if(result) callSmallObservers = false;
			}
		}

		for(var item : c) {
			super.add(nextIndex, item);

			if(callObservers && callSmallObservers) {
				for(var observer : addObservers) {
					observer.added((E) item, nextIndex);
				}
			}

			nextIndex++;
		}

		return true;
	}

	@Override
	public void clear(boolean callObservers) {
		super.clear();

		if(callObservers) {
			boolean callSmallObservers = true;

			if(largeChangeObservers != null) {
				for(var observer : largeChangeObservers) {
					var result = observer.changed();
					if(result) callSmallObservers = false;
				}
			}

			if(callSmallObservers) {
				for(var observer : removeObservers) {
					observer.removed(null, 0);
				}
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
	public void removeAdditionObserver(AddObserver<E> observer) {
		addObservers.remove(observer);
	}

	@Override
	public void removeRemovalObserver(RemoveObserver<E> observer) {
		removeObservers.remove(observer);
	}

	@Override
	public void removeChangesObserver(ChangeObserver<E> observer) {
		changeObservers.remove(observer);
	}

	@Override
	public boolean add(E e, boolean callObservers) {
		var wasSize = size();
		boolean result = super.add(e);

		if(callObservers) {
			for(var observer : addObservers) {
				observer.added(e, wasSize);
			}
		}

		return result;
	}

	@Override
	public boolean add(E e) {
		return ObservableList.super.add(e);
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