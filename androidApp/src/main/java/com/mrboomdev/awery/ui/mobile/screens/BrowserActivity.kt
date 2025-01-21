package com.mrboomdev.awery.ui.mobile.screens

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App
import com.mrboomdev.awery.app.theme.ThemeManager.applyTheme
import com.mrboomdev.awery.data.Constants
import com.mrboomdev.awery.databinding.ScreenBrowserBinding
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.android.AndroidGlobals.toast
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.cleanUrl
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.isValidUrl
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.setMargin
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import com.mrboomdev.awery.utils.addOnBackPressedListener
import com.mrboomdev.awery.utils.startActivityForResult
import com.mrboomdev.safeargsnext.owner.SafeArgsActivity
import eu.kanade.tachiyomi.util.system.WebViewClientCompat
import java.net.URISyntaxException

private const val TAG = "BrowserActivity"

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
				binding.edittext.text.toString().let { input ->
					if(input.isValidUrl()) {
						binding.webview.loadUrl(input)
					} else {
						binding.webview.loadUrl("https://www.google.com/search?q=${Uri.encode(input)}")
					}
				}

				binding.edittext.clearFocus()
				inputManager.hideSoftInputFromWindow(binding.edittext.windowToken, 0)
				return@setOnEditorActionListener true
			}

			false
		}

		binding.options.setOnClickListener { v ->
			PopupMenu(this, v).apply {
				menu.add(0, 0, 0, i18n(Res.string.copy_link_to_clipboard))
				menu.add(0, 1, 0, i18n(Res.string.open_link_externally))

				setOnMenuItemClickListener { item -> when(item.itemId) {
					0 -> {
						App.copyToClipboard(binding.webview.url!!)
						true
					}

					1 -> {
						startActivity(Intent(Intent.ACTION_VIEW).apply {
							data = Uri.parse(binding.webview.url)

							if(resolveActivity(packageManager) == null) {
								toast("No external browser was found :(", 1)
								return@setOnMenuItemClickListener true
							}
						})

						true
					}

					else -> false
				}}

				show()
			}
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

		binding.webview.settings.apply {
			userAgentString = Constants.DEFAULT_UA
			allowContentAccess = true
			allowFileAccess = true
			displayZoomControls = false
			builtInZoomControls = true
			domStorageEnabled = true
			javaScriptCanOpenWindowsAutomatically = true
			javaScriptEnabled = true
			setSupportZoom(true)

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				safeBrowsingEnabled = false
			}
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
				if(url.startsWith("http://") || url.startsWith("https://")) return false

				try {
					startActivity(Intent.parseUri(url, 0))
					return true
				} catch(e: URISyntaxException) {
					Log.e(TAG, "Failed to parse intent!", e)
					return false
				} catch(e: ActivityNotFoundException) {
					Log.e(TAG, "No activity was found!", e)
					return false
				}
			}

			override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
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
			override fun onProgressChanged(
				view: WebView,
				newProgress: Int
			) {
				binding.progressBar.setProgressCompat(newProgress, true)
			}

			override fun onJsAlert(
				view: WebView,
				url: String,
				message: String,
				result: JsResult
			): Boolean {
				var didPressButton = false

				DialogBuilder(this@BrowserActivity).apply {
					setTitle("\"$url\" says:")
					setMessage(message)

					setPositiveButton(i18n(Res.string.ok)) { dialog ->
						result.confirm()
						didPressButton = true
						dialog.dismiss()
					}

					setOnDismissListener {
						if(!didPressButton) {
							result.cancel()
						}
					}

					show()
				}

				return true
			}

			override fun onJsPrompt(
				view: WebView,
				url: String,
				message: String,
				defaultValue: String,
				result: JsPromptResult
			): Boolean {
				toast("Prompts are currently not supported")
				return false
			}

			override fun onPermissionRequest(
				request: PermissionRequest
			) {
				toast("Permissions are currently not supported")
				request.deny()
			}

			override fun onGeolocationPermissionsShowPrompt(
				origin: String,
				callback: GeolocationPermissions.Callback
			) {
				toast("Geolocation is currently not supported")
				callback.invoke(origin, false, false)
			}

			override fun onJsConfirm(
				view: WebView,
				url: String,
				message: String,
				result: JsResult
			): Boolean {
				toast("Confirmations are currently not supported")
				return false
			}

			override fun onShowFileChooser(
				webView: WebView,
				callback: ValueCallback<Array<Uri>>,
				params: FileChooserParams
			): Boolean {
				startActivityForResult(params.createIntent(), { resultCode, result ->
					callback.onReceiveValue(FileChooserParams.parseResult(resultCode, result))
				})

				return true
			}
		}
	}
}