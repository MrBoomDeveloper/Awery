package com.mrboomdev.awery.util;

public class Progress {
	public static final Progress EMPTY = new Progress() {
		@Override
		public void setProgress(long progress) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setMax(long max) {
			throw new UnsupportedOperationException();
		}
	};

	private long progress, max;

	public Progress(long progress, long max) {
		this.progress = progress;
		this.max = max;
	}

	public Progress(long max) {
		this.max = max;
	}

	public Progress() {}

	public long getProgress() {
		return progress;
	}

	public long getMax() {
		return max;
	}

	public void setProgress(long progress) {
		this.progress = progress;
	}

	public void increment() {
		this.progress++;
	}

	public void setCompleted() {
		setProgress(getMax());
	}

	public void setMax(long max) {
		this.max = max;
	}

	public boolean isCompleted() {
		return progress >= max;
	}
}