package com.mrboomdev.awery.util.observable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Deprecated
public class ObservableEmptyList<E> implements ObservableList<E> {
	private static final ObservableEmptyList<?> instance = new ObservableEmptyList<>();

	@SuppressWarnings("unchecked")
	public static <E> ObservableEmptyList<E> getInstance() {
		return (ObservableEmptyList<E>) instance;
	}

	@Override
	public boolean addAll(Collection<? extends E> list, boolean callObservers) {
		return false;
	}

	@Override
	public void clear(boolean callObservers) {}

	@Override
	public boolean addAll(int index, Collection<? extends E> list, boolean callObservers) {
		return false;
	}

	@Override
	public boolean remove(Object o, boolean callObservers) {
		return false;
	}

	@Override
	public E remove(int index, boolean callObservers) {
		return null;
	}

	@Override
	public void add(int index, E element, boolean callObservers) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(E e, boolean callObservers) {
		return false;
	}

	@Override
	public E set(int index, E element, boolean callObservers) {
		return null;
	}

	@Override
	public void observeAdditions(AddObserver<E> callback) {}

	@Override
	public void observeChanges(ChangeObserver<E> callback) {}

	@Override
	public void observeLargeChanges(LargeChangeObserver callback) {}

	@Override
	public void removeLargeChangeObserver(LargeChangeObserver callback) {}

	@Override
	public void observeRemovals(RemoveObserver<E> callback) {}

	@Override
	public void removeAdditionObserver(AddObserver<E> observer) {}

	@Override
	public void removeRemovalObserver(RemoveObserver<E> observer) {}

	@Override
	public void removeChangesObserver(ChangeObserver<E> observer) {}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean contains(@Nullable Object o) {
		return false;
	}

	@NonNull
	@Override
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException();
	}

	@NonNull
	@Override
	public Object[] toArray() {
		return new Object[0];
	}

	@NonNull
	@Override
	public <T> T[] toArray(@NonNull T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(@NonNull Collection<?> c) {
		return false;
	}

	@Override
	public boolean removeAll(@NonNull Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(@NonNull Collection<?> c) {
		return false;
	}

	@Override
	public E get(int index) {
		return null;
	}

	@Override
	public int indexOf(@Nullable Object o) {
		return 0;
	}

	@Override
	public int lastIndexOf(@Nullable Object o) {
		return 0;
	}

	@NonNull
	@Override
	public ListIterator<E> listIterator() {
		throw new UnsupportedOperationException();
	}

	@NonNull
	@Override
	public ListIterator<E> listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	@NonNull
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}
}