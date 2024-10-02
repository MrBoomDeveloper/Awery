package com.mrboomdev.awery.ui.activity;

import static com.mrboomdev.awery.app.App.addOnBackPressedListener;
import static com.mrboomdev.awery.app.App.copyToClipboard;
import static com.mrboomdev.awery.app.App.enableEdgeToEdge;
import static com.mrboomdev.awery.app.App.resolveAttrColor;
import static com.mrboomdev.awery.app.App.toast;
import static com.mrboomdev.awery.util.NiceUtils.cleanUrl;
import static com.mrboomdev.awery.util.NiceUtils.requireArgument;
import static com.mrboomdev.awery.util.ui.ViewUtil.setHorizontalMargin;
import static com.mrboomdev.awery.util.ui.ViewUtil.setOnApplyUiInsetsListener;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mrboomdev.awery.R;
import com.mrboomdev.awery.data.Constants;
import com.mrboomdev.awery.databinding.ScreenBrowserBinding;
import com.mrboomdev.awery.ui.ThemeManager;
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder;

import java.util.concurrent.atomic.AtomicBoolean;

import eu.kanade.tachiyomi.util.system.WebViewClientCompat;

public class BrowserActivity extends AppCompatActivity {
	public static final String EXTRA_URL = "url";
	private ScreenBrowserBinding binding;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		ThemeManager.apply(this);
		enableEdgeToEdge(this);
		super.onCreate(savedInstanceState);

		binding = ScreenBrowserBinding.inflate(getLayoutInflater());
		binding.getRoot().setBackgroundColor(resolveAttrColor(this, android.R.attr.colorBackground));
		setContentView(binding.getRoot());

		binding.exit.setOnClickListener(v -> finish());
		binding.back.setOnClickListener(v -> binding.webview.goBack());
		binding.forward.setOnClickListener(v -> binding.webview.goForward());

		var inputManager = getSystemService(InputMethodManager.class);

		binding.header.setOnClickListener(v -> {
			binding.edittext.requestFocus();
			inputManager.showSoftInput(binding.edittext, 0);
		});

		binding.edittext.setOnEditorActionListener((v, actionId, event) -> {
			if(actionId == EditorInfo.IME_ACTION_SEARCH) {
				binding.webview.loadUrl(binding.edittext.getText().toString());
				binding.edittext.clearFocus();

				inputManager.hideSoftInputFromWindow(
						binding.edittext.getWindowToken(), 0);

				return true;
			}

			return false;
		});

		binding.options.setOnClickListener(v -> {
			var menu = new PopupMenu(this, v);
			menu.getMenu().add(0, 0, 0, "Copy link to clipboard");
			menu.getMenu().add(0, 1, 0, "Open in external browser");

			menu.setOnMenuItemClickListener(item -> switch(item.getItemId()) {
				case 0 -> {
					copyToClipboard(binding.webview.getUrl(), binding.webview.getUrl());
					yield true;
				}

				case 1 -> {
					var intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(binding.webview.getUrl()));
					var resolved = intent.resolveActivity(getPackageManager());

					if(resolved == null) {
						toast("No external browser was found :(", 1);
						yield true;
					}

					startActivity(intent);
					yield true;
				}

				default -> false;
			});

			menu.show();
		});

		setOnApplyUiInsetsListener(binding.header, insets -> {
			binding.header.setPadding(insets.left, insets.top, insets.right, 0);
			return true;
		});

		setOnApplyUiInsetsListener(binding.swipeRefresher, insets -> {
			setHorizontalMargin(binding.swipeRefresher, insets.left, insets.right);
			return true;
		});

		var settings = binding.webview.getSettings();
		settings.setAllowContentAccess(true);
		settings.setAllowFileAccess(true);
		settings.setDatabaseEnabled(true);
		settings.setDisplayZoomControls(false);
		settings.setDomStorageEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setJavaScriptEnabled(true);
		settings.setSupportZoom(true);
		settings.setUserAgentString(Constants.DEFAULT_UA);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			settings.setSafeBrowsingEnabled(false);
		}

		var url = requireArgument(getIntent(), EXTRA_URL, String.class);
		binding.edittext.setText(url);
		binding.webview.loadUrl(url);

		binding.swipeRefresher.setOnRefreshListener(() -> binding.webview.reload());

		binding.swipeRefresher.setColorSchemeColors(resolveAttrColor(
				this, android.R.attr.colorPrimary));

		binding.swipeRefresher.setProgressBackgroundColorSchemeColor(resolveAttrColor(
				this, com.google.android.material.R.attr.colorSurface));

		addOnBackPressedListener(this, () -> {
			if(binding.edittext.hasFocus()) {
				binding.edittext.clearFocus();

				inputManager.hideSoftInputFromWindow(
						binding.edittext.getWindowToken(), 0);

				return;
			}

			if(binding.webview.canGoBack()) {
				binding.webview.goBack();
			} else {
				finish();
			}
		});

		binding.webview.setWebViewClient(new WebViewClientCompat() {

			@Override
			public boolean shouldOverrideUrlCompat(@NonNull WebView view, @NonNull String url) {
				return !url.startsWith("http");
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				binding.edittext.setText(cleanUrl(url));
				binding.progressBar.setProgressCompat(0, false);
				binding.progressBar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				binding.edittext.setText(cleanUrl(url));
				binding.swipeRefresher.setRefreshing(false);
				binding.progressBar.setVisibility(View.GONE);
			}
		});

		binding.webview.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				binding.progressBar.setProgressCompat(newProgress, true);
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				var didPressButton = new AtomicBoolean();

				new DialogBuilder(BrowserActivity.this)
						.setTitle("\"" + url + "\" says:")
						.setMessage(message)
						.setOnDismissListener(dialog -> {
							if(!didPressButton.get()) {
								result.cancel();
							}
						})
						.setPositiveButton(R.string.ok, dialog -> {
							result.confirm();
							didPressButton.set(true);
							dialog.dismiss();
						})
						.show();

				return true;
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
				toast("Prompts currently aren's supported");
				return false;
			}

			@Override
			public void onPermissionRequest(PermissionRequest request) {
				toast("Permissions currently aren's supported");
				request.deny();
			}

			@Override
			public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
				toast("Geolocation currently isn't supported");
				callback.invoke(origin, false, false);
			}

			@Override
			public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
				toast("Confirmations currently aren's supported");
				return false;
			}

			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
				toast("Files picker currently isn't supported");
				return false;
			}
		});
	}
}