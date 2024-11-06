package com.mrboomdev.awery.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.copyToClipboard
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.data.Constants
import com.mrboomdev.awery.databinding.ScreenBrowserBinding
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.addOnBackPressedListener
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.applyTheme
import com.mrboomdev.awery.util.extensions.cleanUrl
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.rightMargin
import com.mrboomdev.awery.util.extensions.setHorizontalMargin
import com.mrboomdev.awery.util.extensions.setMargin
import com.mrboomdev.awery.util.ui.ViewUtil
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import com.mrboomdev.safeargsnext.owner.SafeArgsActivity
import eu.kanade.tachiyomi.util.system.WebViewClientCompat
import java.util.concurrent.atomic.AtomicBoolean

class BrowserActivity : AppCompatActivity(), SafeArgsActivity<BrowserActivity.Extras> {
	private lateinit var binding: ScreenBrowserBinding
	data class Extras(val url: String)

	@SuppressLint("SetJavaScriptEnabled")
	override fun onCreate(savedInstanceState: Bundle?) {
		applyTheme()
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		binding = ScreenBrowserBinding.inflate(layoutInflater)
		binding.root.setBackgroundColor(resolveAttrColor(android.R.attr.colorBackground))
		setContentView(binding.root)

		binding.exit.setOnClickListener { finish() }
		binding.back.setOnClickListener { binding.webview.goBack() }
		binding.forward.setOnClickListener { binding.webview.goForward() }

		val inputManager = getSystemService(InputMethodManager::class.java)

		binding.header.setOnClickListener {
			binding.edittext.requestFocus()
			inputManager.showSoftInput(binding.edittext, 0)
		}

		binding.edittext.setOnEditorActionListener { _, actionId, _ ->
			if(actionId == EditorInfo.IME_ACTION_SEARCH) {
				binding.webview.loadUrl(binding.edittext.text.toString())
				binding.edittext.clearFocus()
				inputManager.hideSoftInputFromWindow(binding.edittext.windowToken, 0)
				return@setOnEditorActionListener true
			}

			false
		}

		binding.options.setOnClickListener { v ->
			val menu = PopupMenu(this, v)
			menu.menu.add(0, 0, 0, R.string.copy_link_to_clipboard)
			menu.menu.add(0, 1, 0, R.string.open_link_externally)

			menu.setOnMenuItemClickListener { item ->
				when(item.itemId) {
					0 -> {
						copyToClipboard(binding.webview.url!!)
						true
					}

					1 -> {
						val intent = Intent(Intent.ACTION_VIEW)
						intent.setData(Uri.parse(binding.webview.url))
						val resolved = intent.resolveActivity(packageManager)

						if(resolved == null) {
							toast("No external browser was found :(", 1)
							return@setOnMenuItemClickListener true
						}

						startActivity(intent)
						true
					}

					else -> false
				}
			}

			menu.show()
		}

		binding.header.applyInsets(UI_INSETS, { view, insets ->
			view.setPadding(insets.left, insets.top, insets.right, 0)
			true
		})

		binding.swipeRefresher.applyInsets(UI_INSETS, { view, insets ->
			view.setMargin {
				leftMargin = insets.left
				rightMargin = insets.right
			}

			true
		})

		val settings = binding.webview.settings
		settings.allowContentAccess = true
		settings.allowFileAccess = true
		settings.displayZoomControls = false
		settings.domStorageEnabled = true
		settings.javaScriptCanOpenWindowsAutomatically = true
		settings.javaScriptEnabled = true
		settings.setSupportZoom(true)
		settings.userAgentString = Constants.DEFAULT_UA

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			settings.safeBrowsingEnabled = false
		}

		val url = safeArgs!!.url
		binding.edittext.setText(url)
		binding.webview.loadUrl(url)

		binding.swipeRefresher.setOnRefreshListener { binding.webview.reload() }

		binding.swipeRefresher.setColorSchemeColors(
			resolveAttrColor(android.R.attr.colorPrimary))

		binding.swipeRefresher.setProgressBackgroundColorSchemeColor(
			resolveAttrColor(com.google.android.material.R.attr.colorSurface)
		)

		addOnBackPressedListener {
			if(binding.edittext.hasFocus()) {
				binding.edittext.clearFocus()

				inputManager.hideSoftInputFromWindow(
					binding.edittext.windowToken, 0
				)

				return@addOnBackPressedListener
			}
			if(binding.webview.canGoBack()) {
				binding.webview.goBack()
			} else {
				finish()
			}
		}

		binding.webview.webViewClient = object : WebViewClientCompat() {
			override fun shouldOverrideUrlCompat(view: WebView, url: String): Boolean {
				return !url.startsWith("http")
			}

			override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
				binding.edittext.setText(url.cleanUrl())
				binding.progressBar.setProgressCompat(0, false)
				binding.progressBar.visibility = View.VISIBLE
			}

			override fun onPageFinished(view: WebView, url: String) {
				binding.edittext.setText(url.cleanUrl())
				binding.swipeRefresher.isRefreshing = false
				binding.progressBar.visibility = View.GONE
			}
		}

		binding.webview.webChromeClient = object : WebChromeClient() {
			override fun onProgressChanged(view: WebView, newProgress: Int) {
				binding.progressBar.setProgressCompat(newProgress, true)
			}

			override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
				val didPressButton = AtomicBoolean()

				DialogBuilder(this@BrowserActivity)
					.setTitle("\"$url\" says:")
					.setMessage(message)
					.setOnDismissListener {
						if(!didPressButton.get()) {
							result.cancel()
						}
					}
					.setPositiveButton(R.string.ok) { dialog ->
						result.confirm()
						didPressButton.set(true)
						dialog.dismiss()
					}
					.show()

				return true
			}

			override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
				toast("Prompts are currently not supported")
				return false
			}

			override fun onPermissionRequest(request: PermissionRequest) {
				toast("Permissions are currently not supported")
				request.deny()
			}

			override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
				toast("Geolocation is currently not supported")
				callback.invoke(origin, false, false)
			}

			override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
				toast("Confirmations are currently not supported")
				return false
			}

			override fun onShowFileChooser(
				webView: WebView,
				filePathCallback: ValueCallback<Array<Uri>>,
				fileChooserParams: FileChooserParams
			): Boolean {
				toast("File picker is currently not supported")
				return false
			}
		}
	}
}