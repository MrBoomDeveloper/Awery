package com.mrboomdev.awery.extensions.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Deprecated(forRemoval = true)
public class CatalogVideoFile implements Serializable {
	@Serial
	private static final long serialVersionUID = 1;
	private final String url, title;
	private final Map<String, String> headers;
	private List<CatalogSubtitle> subtitles;

	public CatalogVideoFile(String title, String url, Map<String, String> headers, List<CatalogSubtitle> subtitles) {
		this.title = title;
		this.url = url;
		this.headers = headers;
		this.subtitles = subtitles;
	}

	public void setSubtitles(List<CatalogSubtitle> subtitles) {
		this.subtitles = subtitles;
	}

	public List<CatalogSubtitle> getSubtitles() {
		return subtitles;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}
}