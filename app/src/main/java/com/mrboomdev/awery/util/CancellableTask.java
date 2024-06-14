package com.mrboomdev.awery.util;

import androidx.annotation.NonNull;
public class CancellableTask {
	private boolean isCancelled;

	public CancellableTask(@NonNull TaskRunner task) {
		task.run(this);
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public void cancel() {
		this.isCancelled = true;
	}

	public interface TaskRunner {
		void run(CancellableTask task);
	}
}