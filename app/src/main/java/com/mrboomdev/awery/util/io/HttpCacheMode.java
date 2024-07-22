package com.mrboomdev.awery.util.io;

public enum HttpCacheMode {
	NETWORK_ONLY {
		public boolean doCache() {
			return false;
		}
	},

	CACHE_FIRST {
		public boolean doCache() {
			return true;
		}
	},

	/**
	 * Same as {@link #CACHE_FIRST}, but even data-sending requests will be cached.
	 */
	// TODO: 7/20/2024 Make it working! Currently it does nothing.
	CACHE_FIRST_ALL {
		public boolean doCache() {
			return true;
		}
	};

	public abstract boolean doCache();
}