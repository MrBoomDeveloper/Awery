package com.mrboomdev.awery.ui.activity;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.view.BigImageView;
import com.github.piasy.biv.view.GlideImageViewFactory;
import com.github.piasy.biv.view.ImageSaveCallback;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.mrboomdev.awery.util.exceptions.ZeroResultsException;
import com.mrboomdev.awery.util.extensions.ActivityExtensionsKt;

import java.io.File;

import kotlin.NotImplementedError;

public class GalleryActivity extends AppCompatActivity {
	public static final String EXTRA_URLS = "urls";
	private static final String TAG = "GalleryActivity";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ActivityExtensionsKt.enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		var urls = requireArgument(getIntent().getStringArrayExtra(EXTRA_URLS), EXTRA_URLS);

		if(urls.length == 0) {
			throw new ZeroResultsException("You didn't provide any urls!");
		}

		if(urls.length > 1) {
			throw new NotImplementedError("TODO!");
		}

		var frame = new FrameLayout(this);
		frame.setBackgroundColor(0xff000000);
		setContentView(frame);

		var linear = new LinearLayoutCompat(this);
		frame.addView(linear, MATCH_PARENT, MATCH_PARENT);

		var imageView = new BigImageView(this);
		imageView.setImageViewFactory(new GlideImageViewFactory());
		imageView.setTransitionName("poster");
		linear.addView(imageView, MATCH_PARENT, MATCH_PARENT);

		var progress = new CircularProgressIndicator(this);
		progress.setForegroundGravity(Gravity.CENTER);
		frame.addView(progress);

		imageView.setImageLoaderCallback(new ImageLoader.Callback() {

			@Override
			public void onCacheHit(int imageType, File image) {}

			@Override
			public void onCacheMiss(int imageType, File image) {}

			@Override
			public void onStart() {}

			@Override
			public void onProgress(int progress) {}

			@Override
			public void onFinish() {}

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public void onSuccess(File image) {
				frame.removeView(progress);
				imageView.getSSIV().setMaxScale(15);

				/*imageView.getSSIV().setOnTouchListener(new View.OnTouchListener() {
					private float startY;
					private int pointers;

					private void returnBack(View v, @NonNull MotionEvent event) {
						ObjectAnimator.ofFloat(v, "translationY", event.getRawY() - startY, 0)
								.setDuration(100)
								.start();
					}

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						imageView.getSSIV().onTouchEvent(event);

						switch(event.getAction()) {
							case MotionEvent.ACTION_DOWN -> {
								pointers++;

								if(pointers > 1) {
									returnBack(v, event);
									return true;
								}

								startY = event.getRawY();
							}

							case MotionEvent.ACTION_MOVE -> {
								if(pointers > 1) {
									return true;
								}

								v.setTranslationY(event.getRawY() - startY);
							}

							case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
								pointers--;

								if(pointers > 1) {
									return true;
								}

								returnBack(v, event);

								if(Math.abs(event.getRawY() - startY) > 250) {
									finishAfterTransition();
								}
							}
						}

						return true;
					}
				});*/
			}

			@Override
			public void onFail(Exception error) {
				toast("Failed to load an image");
				finish();
			}
		});

		imageView.setImageSaveCallback(new ImageSaveCallback() {
			@Override
			public void onSuccess(String uri) {
				toast("Saved successfully!");
			}

			@Override
			public void onFail(Throwable t) {
				Log.e(TAG, "Failed to save an image!", t);
				toast("Failed to save an image");
			}
		});

		imageView.showImage(Uri.parse(urls[0]));
	}
}