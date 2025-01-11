package com.mrboomdev.awery.ui.mobile.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.NestedScrollView
import app.futured.hauler.HaulerView
import app.futured.hauler.setOnDragDismissedListener
import com.github.piasy.biv.loader.ImageLoader
import com.github.piasy.biv.view.BigImageView
import com.github.piasy.biv.view.GlideImageViewFactory
import com.github.piasy.biv.view.ImageSaveCallback
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.mrboomdev.awery.app.App
import com.mrboomdev.awery.ext.util.exceptions.ZeroResultsException
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.safeargsnext.owner.SafeArgsActivity
import com.mrboomdev.safeargsnext.util.rememberSafeArgs
import java.io.File

private const val TAG = "GalleryActivity"

class GalleryActivity : AppCompatActivity(), SafeArgsActivity<GalleryActivity.Extras> {
	class Extras(val urls: Array<String>)

	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		if(rememberSafeArgs!!.urls.isEmpty()) {
			throw ZeroResultsException("You didn't provide any urls!")
		}

		val hauler = HaulerView(this)
		hauler.setOnDragDismissedListener { finish() }
		hauler.setDragEnabled(true)
		hauler.setFadeSystemBars(true)
		setContentView(hauler)

		val scrollView = NestedScrollView(this)
		scrollView.isFillViewport = true
		hauler.addView(scrollView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

		val frame = FrameLayout(this)
		scrollView.addView(frame, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

		val linear = LinearLayoutCompat(this)
		frame.addView(linear, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

		val imageView = BigImageView(this)
		imageView.setImageViewFactory(GlideImageViewFactory())
		imageView.transitionName = "poster"
		linear.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, resources.displayMetrics.heightPixels)

		val progress = CircularProgressIndicator(this)
		progress.foregroundGravity = Gravity.CENTER
		frame.addView(progress)

		imageView.setImageLoaderCallback(object : ImageLoader.Callback {
			override fun onCacheHit(imageType: Int, image: File) {}
			override fun onCacheMiss(imageType: Int, image: File) {}
			override fun onProgress(progress: Int) {}
			override fun onStart() {}
			override fun onFinish() {}

			@SuppressLint("ClickableViewAccessibility")
			override fun onSuccess(image: File) {
				frame.removeView(progress)
				imageView.ssiv.maxScale = 15f
			}

			override fun onFail(error: Exception) {
				App.toast("Failed to load an image")
				finish()
			}
		})

		imageView.setImageSaveCallback(object : ImageSaveCallback {
			override fun onSuccess(uri: String) {
				App.toast("Saved successfully!")
			}

			override fun onFail(t: Throwable) {
				Log.e(TAG, "Failed to save an image!", t)
				App.toast("Failed to save an image")
			}
		})

		imageView.showImage(Uri.parse(rememberSafeArgs!!.urls[0]))
	}
}