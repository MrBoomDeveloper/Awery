package com.mrboomdev.awery.extensions.support.yomi;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.mrboomdev.awery.app.data.AndroidImage;
import com.mrboomdev.awery.ext.source.Extension;
import com.mrboomdev.awery.ext.constants.AdultContentMode;
import com.mrboomdev.awery.ext.source.ExtensionProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class YomiExtension extends Extension {
	protected final List<YomiProvider> providers = new ArrayList<>();
	protected AdultContentMode adultContent;
	protected boolean isLoaded;
	private final String version, name, id;
	private AndroidImage icon;
	private Throwable throwable;

	public YomiExtension(
			PackageManager pm,
			@NotNull PackageInfo packageInfo,
			String label
	) {
		this.id = packageInfo.packageName;
		this.version = packageInfo.versionName;
		this.name = label;

		if(packageInfo.applicationInfo != null) {
			this.icon = new AndroidImage(packageInfo.applicationInfo.loadIcon(pm));
		}
	}

	public void setThrowable(Throwable t) {
		this.throwable = t;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public ExtensionProvider getProvider() {
		return providers.get(0);
	}

	@NonNull
	@Override
	public Collection<String> getFeatures() {
		return Collections.emptyList();
	}

	@Override
	public AdultContentMode getAdultContentMode() {
		return adultContent;
	}

	@NonNull
	@Override
	public String getId() {
		return id;
	}

	@Override
	public AndroidImage getIcon() {
		return icon;
	}

	@Override
	public Throwable getError() {
		return throwable;
	}
}