package androidx.preference

import android.content.Context
import com.mrboomdev.awery.core.utils.PlatformSdk

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

@PlatformSdk
open class CheckBoxPreference(context: Context): TwoStatePreference(context)