package com.mrboomdev.awery.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class SubscriptionsService extends Service {

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}