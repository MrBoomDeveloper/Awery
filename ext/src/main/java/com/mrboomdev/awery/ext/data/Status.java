package com.mrboomdev.awery.ext.data;

public enum Status {
	ONGOING,
	CANCELLED,
	PAUSED,
	COMPLETED,
	COMING_SOON;

	/**
	 * @return Whatever new episodes will be released in the future.
	 * @author MrBoomDev
	 */
	public boolean isFinished() {
		return this == CANCELLED || this == COMPLETED;
	}

	public boolean canWatch() {
		return this != COMING_SOON;
	}
}