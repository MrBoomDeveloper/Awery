package com.mrboomdev.awery.util;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

/**
 * A helper-class which helps to generate random ids which doesn't collide with other ids.
 * @author MrBoomDev
 */
public class UniqueIdGenerator {
	private final Random random = new Random();
	private Set<Long> usedLongs;
	private Set<Integer> usedIntegers;

	/**
	 * @return A new id of type Long
	 * @see #getInteger()
	 * @author MrBoomDev
	 */
	public long getLong() {
		if(usedLongs == null) {
			usedLongs = new LinkedHashSet<>();
		}

		while(true) {
			long id = random.nextLong();

			if(!usedLongs.contains(id)) {
				usedLongs.add(id);
				return id;
			}
		}
	}

	/**
	 * @return A new id of type Integer
	 * @see #getLong()
	 * @author MrBoomDev
	 */
	public int getInteger() {
		if(usedIntegers == null) {
			usedIntegers = new LinkedHashSet<>();
		}

		while(true) {
			int id = random.nextInt();

			if(!usedIntegers.contains(id)) {
				usedIntegers.add(id);
				return id;
			}
		}
	}

	/**
	 * Clears the pool of used ids. Next call to getLong() and getInteger()
	 * may return values that do collide with those that were generated before this method call.
	 * @author MrBoomDev
	 */
	public void clear() {
		if(usedLongs != null) usedLongs.clear();
		if(usedIntegers != null) usedIntegers.clear();

		usedLongs = null;
		usedIntegers = null;
	}
}