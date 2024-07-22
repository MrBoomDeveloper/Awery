package com.mrboomdev.awery.util.io;

public enum HttpMethod {
	POST {
		public boolean doSendData() {
			return true;
		}
	},

	PUT {
		public boolean doSendData() {
			return true;
		}
	},

	PATCH {
		public boolean doSendData() {
			return true;
		}
	},

	DELETE {
		public boolean doSendData() {
			return false;
		}
	},

	HEAD {
		public boolean doSendData() {
			return false;
		}
	},

	GET {
		public boolean doSendData() {
			return false;
		}
	};

	public abstract boolean doSendData();
}