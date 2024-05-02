package com.mrboomdev.awery.desktop.ui.components;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public class NavigationBar extends JComponent {
	private final List<Item> items = new ArrayList<>();

	public void addItem(Item item) {
		items.add(item);
	}

	public static class Item {
		private String title;

		public Item(String title) {
			setTitle(title);
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}
}