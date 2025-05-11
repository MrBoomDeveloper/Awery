package com.mrboomdev.awery.util.ui;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerItemDecoration extends RecyclerView.ItemDecoration {
	private final int width, height;

	public RecyclerItemDecoration(int size) {
		this.width = size;
		this.height = size;
	}

	public RecyclerItemDecoration(int width, int height) {
		this.width = width;
		this.height = height;
	}


	@Override
	public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
		var manager = parent.getLayoutManager();
		var position = parent.getChildAdapterPosition(view);
		outRect.setEmpty();

		if(manager instanceof GridLayoutManager gridLayoutManager) {
			if(gridLayoutManager.getOrientation() == RecyclerView.HORIZONTAL) {
				throw new UnsupportedOperationException("Unsupported layout manager orientation!");
			}

			var columnsCount = gridLayoutManager.getSpanCount();
			var row = position / columnsCount;
			var column = position % columnsCount;

			outRect.top = row == 0 ? 0 : height;
			outRect.left = column == 0 ? 0 : width;
		} else if(manager instanceof LinearLayoutManager linearLayoutManager) {
			if(linearLayoutManager.getOrientation() == RecyclerView.HORIZONTAL) {
				outRect.left = position == 0 ? 0 : width;
			} else {
				outRect.top = position == 0 ? 0 : height;
			}
		} else {
			throw new UnsupportedOperationException("Unsupported layout manager! " + manager.getClass().getName());
		}
	}
}