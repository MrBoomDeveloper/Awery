package com.mrboomdev.awery.sdk.util;


import static com.mrboomdev.awery.sdk.PlatformApi.stream;

import org.jetbrains.annotations.NotNull;

public class FancyVersion implements Comparable<FancyVersion> {
	private Integer[] args;
	private String version;

	public FancyVersion(@NotNull String version) throws InvalidSyntaxException {
		set(version);
	}

	public void set(@NotNull String version) throws InvalidSyntaxException {
		this.version = version;

		try {
			this.args = stream(version.split("\\."))
					.map(Integer::parseInt)
					.toArray(Integer[]::new);
		} catch(NumberFormatException e) {
			throw new InvalidSyntaxException("Invalid version syntax: " + version, e);
		}
	}

	@Override
	public int compareTo(@NotNull FancyVersion o) {
		for(int i = 0; i < Math.max(args.length, o.args.length); i++) {
			if(i >= args.length) return -1;
			if(i >= o.args.length) return 1;
		}

		return 0;
	}

	public static boolean isValid(@NotNull String version) {
		try {
			new FancyVersion(version);
			return true;
		} catch(InvalidSyntaxException e) {
			return false;
		}
	}
}