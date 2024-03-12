package com.mrboomdev.awery.util;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

public class UniqueIdGenerator {
	private final Random random = new Random();
	private Set<Long> usedLongs;
	private Set<Integer> usedIntegers;

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

	public void clear() {
		if(usedLongs != null) usedLongs.clear();
		if(usedIntegers != null) usedIntegers.clear();

		usedLongs = null;
		usedIntegers = null;
	}
}