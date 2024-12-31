package com.mrboomdev.awery.util.exceptions;

import java.io.IOException;

public class BotSecurityBypassException extends IOException {
	public static final String CLOUDFLARE = "Cloudflare";
	public static final String OTHER = "Other";
	private final String blocker;

	public BotSecurityBypassException(String blockerName) {
		super();
		this.blocker = blockerName;
	}

	public BotSecurityBypassException(String blockerName, String message) {
		super(message);
		this.blocker = blockerName;
	}

	public BotSecurityBypassException(String blockerName, String message, Throwable cause) {
		super(message, cause);
		this.blocker = blockerName;
	}

	public BotSecurityBypassException(String blockerName, Throwable cause) {
		super(cause);
		this.blocker = blockerName;
	}

	public String getBlockerName() {
		return blocker;
	}
}