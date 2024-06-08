package com.mrboomdev.awery.util;

public class IconStateful {
	private String active, inActive;
	public String[] names;

	public String getActive() {
		return active != null ? active : inActive;
	}

	public String getInActive() {
		return inActive != null ? inActive : active;
	}
}