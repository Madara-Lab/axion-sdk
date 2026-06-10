/*
 * Copyright (C) 2025-2026 AxionOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.axion.blur

import android.os.SystemProperties

internal object AxBlurProperties {
    private const val PROP_FORCE_ENABLE = "persist.sysui.disableBlur"
    private const val PROP_GLOBAL_BLUR_ENABLED = "ro.custom.blur.enable"

    val forceEnabled: Boolean = SystemProperties.getBoolean(PROP_FORCE_ENABLE, false)
    val defaultGlobalBlurEnabled: Boolean =
        forceEnabled || SystemProperties.getBoolean(PROP_GLOBAL_BLUR_ENABLED, true)
}
