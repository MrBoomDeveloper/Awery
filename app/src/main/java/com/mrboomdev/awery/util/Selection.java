package com.mrboomdev.awery.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import java9.util.function.BiConsumer;
import java9.util.function.BinaryOperator;
import java9.util.function.Function;
import java9.util.function.Supplier;
import java9.util.stream.Collector;
import java9.util.stream.Stream;

public class Selection<T> implements Collection<Map.Entry<T, Selection.State>> {
	public static final Selection<Object> EMPTY = new Selection<>();
	private static final Set<Collector.Characteristics> characteristics
			= EnumSet.of(Collector.Characteristics.IDENTITY_FINISH, Collector.Characteristics.UNORDERED);
	private final Map<T, State> items = new LinkedHashMap<>();

	@SuppressWarnings("unchecked")
	public static <T> Selection<T> empty() {
		return (Selection<T>) EMPTY;
	}

	public Selection(@NonNull Selection<T> selection) {
		items.putAll(selection.items);
	}

	public Selection(@Nullable Collection<T> items) {
		if(items == null) return;

		for(var item : items) {
			if(item instanceof Selection.Selectable<?> selectable) {
				this.items.put(item, selectable.state);
				continue;
			}

			this.items.put(item, State.EXCLUDED);
		}
	}

	public Selection() {}

	public void setState(T item, State state) {
		items.put(item, state);

		if(item instanceof Selection.Selectable<?> selectable) {
			selectable.setState(state);
		}
	}

	private Stream<T> find(State state) {
		return NiceUtils.stream(items)
				.filter(entry -> entry.getValue() == state)
				.map(Map.Entry::getKey);
	}

	@Nullable
	public T get(State state) {
		var found = find(state).findAny();
		return found.isPresent() ? found.get() : null;
	}

	@Unmodifiable
	public List<T> getAll(State state) {
		return find(state).toList();
	}

	public State getState(T item) {
		return items.getOrDefault(item, State.EXCLUDED);
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
	@SuppressWarnings("unchecked")
	public boolean contains(@Nullable Object o) {
		if(o == null) return false;

		try {
			/* Why do we need this? Well, in Java we cannot cast to a generic,
			   and at runtime you cannot do anything with it.
			   All we can is catch the exception and return false if it was thrown. */

			return items.containsKey((T) o);
		} catch(ClassCastException e) {
			return false;
		}
	}

	private class SelectionIterator implements Iterator<Map.Entry<T, State>> {
		private final Map.Entry<T, State>[] entries;
		private int index = 0;

		@SuppressWarnings("unchecked")
		private SelectionIterator(@NonNull Collection<Map.Entry<T, State>> entries) {
			this.entries = entries.toArray(new Map.Entry[0]);
		}

		@Override
		public boolean hasNext() {
			return index < entries.length;
		}

		@Override
		public Map.Entry<T, State> next() {
			return entries[index++];
		}
	}

	public Collection<Map.Entry<T, State>> getAll() {
		return items.entrySet();
	}

	@NonNull
	@Override
	public Iterator<Map.Entry<T, State>> iterator() {
		return new SelectionIterator(items.entrySet());
	}

	@NonNull
	@Override
	public Object[] toArray() {
		return new Object[0];
	}

	@NonNull
	@Override
	public <T1> T1[] toArray(@NonNull T1[] a) {
		return items.entrySet().toArray(a);
	}

	@Override
	public boolean add(@NonNull Map.Entry<T, State> t) {
		items.put(t.getKey(), t.getValue());
		return true;
	}

	@Override
	public boolean remove(@Nullable Object o) {
		if(o == null) return false;

		try {
			items.remove(o);
			return true;
		} catch(ClassCastException e) {
			return false;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean containsAll(@NonNull Collection<?> c) {
		for(var entry : c) {
			if(entry == null) return false;

			try {
				if(!items.containsKey((T) entry)) return false;
			} catch(ClassCastException e) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean addAll(@NonNull Collection<? extends Map.Entry<T, State>> c) {
		for(var entry : c) {
			try {
				items.put(entry.getKey(), entry.getValue());
			} catch(ClassCastException | NullPointerException e) {
				return false;
			}
		}

		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean removeAll(@NonNull Collection<?> c) {
		for(var o : c) {
			if(o instanceof Map.Entry<?, ?> entry) {
				try {
					items.remove((T) entry.getKey());
				} catch(ClassCastException | NullPointerException e) {
					return false;
				}
			} else {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean retainAll(@NonNull Collection<?> c) {
		boolean didChanged = false;

		for(var entry : items.entrySet()) {
			if(!c.contains(entry.getKey())) {
				items.remove(entry.getKey());
				didChanged = true;
			}
		}

		return didChanged;
	}

	@Override
	public void clear() {
		items.clear();
	}

	@NonNull
	@Contract(" -> new")
	public static <T> Collector<T, Selection<T>, Selection<T>> collect() {
		return new Collector<>() {
			@Override
			public Supplier<Selection<T>> supplier() {
				return Selection::new;
			}

			@Override
			public BiConsumer<Selection<T>, T> accumulator() {
				return (selection, t) -> selection.setState(t, t instanceof Selectable<?> selectable
						? selectable.getState() : State.UNSELECTED);
			}

			@Override
			public BinaryOperator<Selection<T>> combiner() {
				return (left, right) -> {
					var result = new Selection<>(left);
					result.addAll(right.getAll());
					return result;
				};
			}

			@Override
			public Function<Selection<T>, Selection<T>> finisher() {
				return a -> a;
			}

			@Override
			public Set<Characteristics> characteristics() {
				return characteristics;
			}
		};
	}

	public static class Selectable<T> implements Comparable<Selectable<T>> {
		private final String id;
		private T item;
		private String title;
		private State state;

		public static String getTitle(Object o) {
			if(o instanceof Selectable<?> selectable) {
				return Objects.requireNonNullElse(selectable.getTitle(), selectable.getItem()).toString();
			}

			return String.valueOf(o);
		}

		public Selectable(T item, String id, String title, State state) {
			this.item = item;
			this.id = id;
			this.title = title;
			this.state = state;
		}

		public Selectable(T item, String id, State state) {
			this(item, id, null, state);
		}

		public Selectable(T item, String id) {
			this(item, id, State.EXCLUDED);
		}

		public Selectable(T item, State state) {
			this(item, null, state);
		}

		public Selectable(T item) {
			this(item, null, State.EXCLUDED);
		}

		public T getItem() {
			return item;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getId() {
			return id;
		}

		public State getState() {
			return state;
		}

		public void setState(State state) {
			this.state = state;
		}

		public void setItem(T item) {
			this.item = item;
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		@Override
		public int compareTo(Selectable<T> o) {
			if(title != null && o.title != null) {
				return title.compareTo(o.title);
			}

			if(item instanceof Comparable left && o.item instanceof Comparable right) {
				return left.compareTo(right);
			}

			return 0;
		}
	}

	public enum State {
		SELECTED,
		EXCLUDED,
		UNSELECTED;

		public State next() {
			return switch(this) {
				case UNSELECTED -> SELECTED;
				case SELECTED -> EXCLUDED;
				case EXCLUDED -> UNSELECTED;
			};
		}
	}
}