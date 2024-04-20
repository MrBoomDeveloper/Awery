package com.mrboomdev.awery.extensions.support.anilist.data;

import java.util.List;

public class AnilistPage<T> {
	public List<T> media;
	public Info pageInfo;

	public static class Info {
		public boolean hasNextPage;
	}
}