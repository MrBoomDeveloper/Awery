package com.mrboomdev.awery.ext.source;

import com.mrboomdev.awery.ext.data.Comment;
import com.mrboomdev.awery.ext.data.Media;
import com.mrboomdev.awery.ext.data.SearchResults;
import com.mrboomdev.awery.ext.data.Settings;
import com.mrboomdev.awery.ext.data.Subtitle;

public class ExtensionProvider {

	public String getChangelog() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ExtensionProvider.getChangelog() isn't implemented!");
	}

	public Settings getSettings() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ExtensionProvider.getSettings() isn't implemented!");
	}

	public SearchResults<Media> searchMedia(Settings filters) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ExtensionProvider.searchMedia() isn't implemented!");
	}

	public SearchResults<Subtitle> searchSubtitles(Settings filters) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ExtensionProvider.searchSubtitles() isn't implemented!");
	}

	public Settings getMediaSearchFilters() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ExtensionProvider.getMediaSearchFilters() isn't implemented!");
	}

	public Settings getSubtitlesSearchFilters() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ExtensionProvider.getSubtitlesSearchFilters() isn't implemented!");
	}

	public SearchResults<Comment> searchComments(Settings filters) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ExtensionProvider.searchComments() isn't implemented!");
	}

	public Settings getCommentsSearchFilters() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ExtensionProvider.getCommentsSearchFilters() isn't implemented!");
	}

	public Settings getTrackingFilters() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ExtensionProvider.getTrackingFilters() isn't implemented!");
	}

	public void track(Settings filters) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("ExtensionProvider.track() isn't implemented!");
	}

	public void postComment(Settings filters) {
		throw new UnsupportedOperationException("ExtensionProvider.postComment() isn't implemented!");
	}

	public void deleteComment(Comment comment) {
		throw new UnsupportedOperationException("ExtensionProvider.deleteComment() isn't implemented!");
	}

	public void editComment(Comment oldComment, Comment newComment) {
		throw new UnsupportedOperationException("ExtensionProvider.editComment() isn't implemented!");
	}
}