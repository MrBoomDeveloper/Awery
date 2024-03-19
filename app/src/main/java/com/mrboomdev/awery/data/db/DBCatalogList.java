package com.mrboomdev.awery.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mrboomdev.awery.extensions.support.template.CatalogList;

@Entity(tableName = "list")
public class DBCatalogList {
	@PrimaryKey
	@NonNull
	private String id;
	private String name;

	public DBCatalogList(@NonNull String id, String name) {
		this.id = id;
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(@NonNull String id) {
		this.id = id;
	}

	@NonNull
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@NonNull
	public static DBCatalogList fromCatalogList(@NonNull CatalogList list) {
		return new DBCatalogList(list.getId(), list.getTitle());
	}

	public CatalogList toCatalogList() {
		return new CatalogList(name, id);
	}
}