package com.mrboomdev.awery.sdk.util;

/**
 * A helper-class which helps to generate random ids which doesn't collide with other ids.
 * @author MrBoomDev
 */
public class UniqueIdGenerator {
	private long usedLongs;
	private int usedIntegers;

	/**
	 * @return A new id of type Long
	 * @see #getInteger()
	 * @author MrBoomDev
	 */
	public long getLong() {
		return usedLongs++;
	}

	/**
	 * @return A new id of type Integer
	 * @see #getLong()
	 * @author MrBoomDev
	 */
	public int getInteger() {
		return usedIntegers++;
	}

	/**
	 * Clears the pool of used ids. Next call to getLong() and getInteger()
	 * may return values that do collide with those that were generated before this method call.
	 * @author MrBoomDev
	 */
	public void clear() {
		usedLongs = 0;
		usedIntegers = 0;
	}
}