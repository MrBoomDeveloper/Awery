package com.mrboomdev.awery.extensions.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class CatalogComment implements CatalogSearchResults<CatalogComment> {
	public static final int HIDDEN = -1;
	public static final int DISABLED = -2;
	public static final int VOTE_STATE_LIKED = 1;
	public static final int VOTE_STATE_DISLIKED = -1;
	public static final int VOTE_STATE_NONE = 0;
	@Json(name = "author_name")
	public String authorName;
	@Json(name = "author_avatar")
	public String authorAvatar;
	public String text;
	public List<CatalogComment> items = new ArrayList<>();
	@Json(name = "has_next_page")
	public boolean hasNextPage = false;
	@Json(name = "can_comment")
	public boolean canComment = false;
	/**
	 * If this value equal to -1, then likes aren't shown
	 * If -2, then you can't use them
	 */
	@Json(name = "likes")
	public int likes;
	/**
	 * If this value equal to -1, then dislikes aren't shown
	 * If -2, then you can't use them
	 */
	@Json(name = "dislikes")
	public int dislikes;
	@Json(name = "comments")
	public int comments;
	/**
	 * If this value equal to -1, then votes aren't shown
	 */
	@Json(name = "votes")
	public Integer votes;
	public String date;

	/**
	 * Used only for the Frontend
	 * Do not use in the Backend!
	 * <p>Used to identify this comment among others</p>
	 */
	@Json(ignore = true)
	public long visualId;
	@Json(ignore = true)
	public int voteState = VOTE_STATE_NONE;

	@Override
	public boolean hasNextPage() {
		return hasNextPage;
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public boolean contains(@Nullable Object o) {
		return items.contains(o);
	}

	@NonNull
	@Override
	public Iterator<CatalogComment> iterator() {
		return items.iterator();
	}

	@NonNull
	@Override
	public Object[] toArray() {
		return items.toArray();
	}

	@NonNull
	@Override
	public <T> T[] toArray(@NonNull T[] a) {
		return items.toArray(a);
	}

	@Override
	public boolean add(CatalogComment catalogComment) {
		return items.add(catalogComment);
	}

	@Override
	public boolean remove(@Nullable Object o) {
		return items.remove(o);
	}

	@Override
	public boolean containsAll(@NonNull Collection<?> c) {
		return new HashSet<>(items).containsAll(c);
	}

	@Override
	public boolean addAll(@NonNull Collection<? extends CatalogComment> c) {
		return items.addAll(c);
	}

	@Override
	public boolean addAll(int index, @NonNull Collection<? extends CatalogComment> c) {
		return items.addAll(index, c);
	}

	@Override
	public boolean removeAll(@NonNull Collection<?> c) {
		return items.removeAll(c);
	}

	@Override
	public boolean retainAll(@NonNull Collection<?> c) {
		return items.retainAll(c);
	}

	@Override
	public void clear() {
		items.clear();
	}

	@Override
	public CatalogComment get(int index) {
		return items.get(index);
	}

	@Override
	public CatalogComment set(int index, CatalogComment element) {
		return items.set(index, element);
	}

	@Override
	public void add(int index, CatalogComment element) {
		items.add(index, element);
	}

	@Override
	public CatalogComment remove(int index) {
		return items.remove(index);
	}

	@Override
	public int indexOf(@Nullable Object o) {
		return items.indexOf(o);
	}

	@Override
	public int lastIndexOf(@Nullable Object o) {
		return items.lastIndexOf(o);
	}

	@NonNull
	@Override
	public ListIterator<CatalogComment> listIterator() {
		return items.listIterator();
	}

	@NonNull
	@Override
	public ListIterator<CatalogComment> listIterator(int index) {
		return items.listIterator(index);
	}

	@NonNull
	@Override
	public List<CatalogComment> subList(int fromIndex, int toIndex) {
		return items.subList(fromIndex, toIndex);
	}
}