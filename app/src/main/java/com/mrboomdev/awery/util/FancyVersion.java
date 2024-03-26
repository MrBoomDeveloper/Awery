package com.mrboomdev.awery.util;

import static com.mrboomdev.awery.app.AweryApp.stream;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.util.exceptions.InvalidSyntaxException;

public class FancyVersion implements Comparable<FancyVersion> {
	private Integer[] args;
	private String version;

	public FancyVersion(@NonNull String version) throws InvalidSyntaxException {
		set(version);
	}

	public void set(@NonNull String version) throws InvalidSyntaxException {
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
	public int compareTo(@NonNull FancyVersion o) {
		for(int i = 0; i < Math.max(args.length, o.args.length); i++) {
			if(i >= args.length) return -1;
			if(i >= o.args.length) return 1;
		}

		return 0;
	}

	public static boolean isValid(@NonNull String version) {
		try {
			new FancyVersion(version);
			return true;
		} catch(InvalidSyntaxException e) {
			return false;
		}
	}
}