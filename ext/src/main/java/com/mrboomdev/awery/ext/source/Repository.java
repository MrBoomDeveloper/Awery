package com.mrboomdev.awery.ext.source;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.ext.constants.AdultContentMode;
import com.mrboomdev.awery.ext.data.Image;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Entity(tableName = "repository")
public class Repository {
	@PrimaryKey
	@NotNull
	private final String manager, url;
	private String title, description, icon;
	@Ignore
	private Collection<Item> items;
	@ColumnInfo(name = "is_enabled", defaultValue = "true")
	private boolean isEnabled = true;

	private Repository(@NotNull String manager, @NotNull String url) {
		this.url = url;
		this.manager = manager;
	}

	@NonNull
	public String getManager() {
		return manager;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getIcon() {
		return icon;
	}

	@NotNull
	public String getUrl() {
		return url;
	}

	public Collection<Item> getItems() {
		return items;
	}

	public static class Builder {
		private final Repository repo;

		public Builder(@NotNull ExtensionsManager manager, @NotNull String url) {
			repo = new Repository(manager.getId(), url);
		}

		public Builder setTitle(String title) {
			repo.title = title;
			return this;
		}

		public Builder setDescription(String description) {
			repo.description = description;
			return this;
		}

		public Builder setIcon(String icon) {
			repo.icon = icon;
			return this;
		}

		public Builder setItems(Collection<Item> items) {
			repo.items = items;
			return this;
		}

		public Repository build() {
			return repo;
		}
	}

	public static class Item {
		private String title, id, version, url;
		private AdultContentMode adultContentMode;
		private Image icon;

		public String getTitle() {
			return title;
		}

		public String getId() {
			return id;
		}

		public Image getIcon() {
			return icon;
		}

		@Nullable
		public AdultContentMode getAdultContentMode() {
			return adultContentMode;
		}

		public String getUrl() {
			return url;
		}

		public String getVersion() {
			return version;
		}

		public static class Builder {
			private final Item item = new Item();

			public Builder setTitle(String title) {
				item.title = title;
				return this;
			}

			public Builder setVersion(String version) {
				item.version = version;
				return this;
			}

			public Builder setIcon(Image image) {
				item.icon = image;
				return this;
			}

			public Builder setUrl(String url) {
				item.url = url;
				return this;
			}

			public Builder setAdultContentMode(AdultContentMode adultContentMode) {
				item.adultContentMode = adultContentMode;
				return this;
			}

			public Builder setId(String id) {
				item.id = id;
				return this;
			}

			public Item build() {
				return item;
			}
		}
	}
}