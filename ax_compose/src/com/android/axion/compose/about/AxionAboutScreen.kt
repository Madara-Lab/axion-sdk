/*
 * Copyright (C) 2025 AxionOS & Project Matrixx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.axion.compose.about

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.SystemProperties
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.android.axion.compose.R
import com.android.axion.compose.scaffold.AxionScaffold
import com.android.axion.deviceinfo.DeviceInfoProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Stable
private class StableImageBitmap(val bitmap: ImageBitmap?)

@Composable
fun MatrixxAboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDeviceInfo: () -> Unit,
    onEditDeviceName: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600
    var deviceInfo by remember { mutableStateOf(DeviceInfoProvider.getDeviceInfo(context)) }
    var showEditDialog by remember { mutableStateOf(false) }
    var wallpaperHolder by remember { mutableStateOf(StableImageBitmap(null)) }
    val density = LocalDensity.current

    val matrixxVersion = remember {
        SystemProperties.get("ro.matrixx.version", "16.2")
    }

    val prettyModel = remember {
        val userDeviceName = deviceInfo.deviceName
        if (userDeviceName.isNotEmpty() && userDeviceName != Build.MODEL) {
            userDeviceName
        } else {
            SystemProperties.get("ro.product.model", Build.MODEL)
        }
    }

    LaunchedEffect(Unit) {
        val result = withContext(Dispatchers.IO) {
            try {
                val drawable = WallpaperManager.getInstance(context).drawable
                val original = (drawable as? BitmapDrawable)?.bitmap ?: return@withContext null
                val illustrationHeightPx = with(density) { (320.dp * 0.75f).toPx() }.toInt()
                val aspectRatio = if (isTablet) 1.4f else 0.48f
                val targetW = if (isTablet) (illustrationHeightPx * aspectRatio).toInt() else (illustrationHeightPx * aspectRatio).toInt()
                val targetH = illustrationHeightPx
                val scale = minOf(targetW.toFloat() / original.width, targetH.toFloat() / original.height)
                Bitmap.createScaledBitmap(
                    original,
                    (original.width * scale).toInt(),
                    (original.height * scale).toInt(),
                    true,
                ).asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
        wallpaperHolder = StableImageBitmap(result)
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainer) {
        AxionScaffold(
            title = stringResource(R.string.about_phone_title),
            onBackClick = onNavigateBack,
            collapsedByDefault = false,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                BannerSection(
                    version = matrixxVersion,
                    buildType = deviceInfo.axionBuildType,
                    maintainer = deviceInfo.maintainer,
                    deviceName = prettyModel,
                    wallpaperHolder = wallpaperHolder,
                    isTablet = isTablet,
                    onDeviceNameClick = { showEditDialog = true },
                )

                SpecsGrid(
                    processor = deviceInfo.processor,
                    rearCamera = "Rear: ${deviceInfo.rearCamera}",
                    frontCamera = "Front: ${deviceInfo.frontCamera}",
                    ram = deviceInfo.totalRam,
                    storage = deviceInfo.storageTotal,
                    battery = deviceInfo.batteryCapacity,
                    screen = deviceInfo.screenSize.ifEmpty { deviceInfo.screenResolution },
                    isTablet = isTablet,
                )

                DeviceDetailsSection(
                    androidVersion = deviceInfo.androidVersion,
                    onSeeAll = onNavigateToDeviceInfo,
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showEditDialog) {
        DeviceNameDialog(
            currentName = deviceInfo.deviceName.ifEmpty { SystemProperties.get("ro.product.model", Build.MODEL) },
            onConfirm = { name ->
                DeviceInfoProvider.setDeviceName(context, name)
                deviceInfo = deviceInfo.copy(deviceName = name)
                onEditDeviceName(name)
            },
            onDismiss = { showEditDialog = false },
        )
    }
}

@Composable
private fun BannerSection(
    version: String,
    buildType: String,
    maintainer: String,
    deviceName: String,
    wallpaperHolder: StableImageBitmap,
    isTablet: Boolean,
    onDeviceNameClick: () -> Unit,
) {
    val cardRadius = 20.dp
    val gap = 4.dp
    val surfaceColor = MaterialTheme.colorScheme.surfaceBright

    val density = LocalDensity.current
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .onSizeChanged { boxSize = it },
        ) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(cardRadius),
                        color = surfaceColor,
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    "MATRIXX",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    version,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            StatusChip(buildType = buildType, maintainer = maintainer)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable(onClick = onDeviceNameClick),
                        shape = RoundedCornerShape(cardRadius),
                        color = surfaceColor,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            contentAlignment = Alignment.BottomStart,
                        ) {
                            Text(
                                deviceName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(cardRadius),
                    color = surfaceColor,
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        DeviceIllustration(
                            wallpaperHolder = wallpaperHolder,
                            isTablet = isTablet,
                            modifier = Modifier
                                .fillMaxHeight(0.92f)
                                .padding(horizontal = 8.dp),
                        )
                    }
                }
            }

            if (boxSize != IntSize.Zero) {
                val widthDp = with(density) { boxSize.width.toDp() }
                val heightDp = with(density) { boxSize.height.toDp() }

                Canvas(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = -(widthDp / 4), y = 0.dp)
                        .width(48.dp)
                        .height(gap + 8.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val r = h / 2
                    val path = Path().apply {
                        moveTo(r, 0f)
                        quadraticTo(0f, h / 2, r, h)
                        lineTo(w - r, h)
                        quadraticTo(w, h / 2, w - r, 0f)
                        lineTo(r, 0f)
                        close()
                    }
                    drawPath(path, color = surfaceColor)
                }

                Canvas(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 0.dp, y = -(heightDp / 4))
                        .width(gap + 8.dp)
                        .height(48.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val r = w / 2
                    val path = Path().apply {
                        moveTo(0f, r)
                        quadraticTo(w / 2, 0f, w, r)
                        lineTo(w, h - r)
                        quadraticTo(w / 2, h, 0f, h - r)
                        lineTo(0f, r)
                        close()
                    }
                    drawPath(path, color = surfaceColor)
                }
            }
        }
    }
}

@Composable
private fun DeviceIllustration(
    wallpaperHolder: StableImageBitmap,
    isTablet: Boolean,
    modifier: Modifier = Modifier,
) {
    val deviceAspectRatio = if (isTablet) 1.4f else 0.48f
    val frameRadius = if (isTablet) 18.dp else 20.dp
    val bezelSize = if (isTablet) 2.dp else 3.dp
    val screenRadius = frameRadius - bezelSize

    Surface(
        modifier = modifier.aspectRatio(deviceAspectRatio),
        shape = RoundedCornerShape(frameRadius),
        color = Color(0xFF1A1A1A),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bezelSize),
                shape = RoundedCornerShape(screenRadius),
                color = Color(0xFF1A1A2E),
            ) {
                wallpaperHolder.bitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            AnimatedDeviceIllustration(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
        }
    }
}

@Composable
private fun SpecsGrid(
    processor: String,
    rearCamera: String,
    frontCamera: String,
    ram: String,
    storage: String,
    battery: String,
    screen: String,
    isTablet: Boolean,
) {
    val gap = 1.5.dp
    val large = 28.dp
    val small = 4.dp
    val surfaceColor = MaterialTheme.colorScheme.surfaceBright

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        if (isTablet) {
            SpecRowCard(surfaceColor, RoundedCornerShape(topStart = large, topEnd = large, bottomStart = small, bottomEnd = small)) {
                SpecItem(Modifier.weight(1f), Icons.Outlined.Memory, stringResource(R.string.about_processor), processor)
                SpecItem(Modifier.weight(1f), Icons.Outlined.CameraAlt, stringResource(R.string.about_camera), "$rearCamera\n$frontCamera")
                SpecItem(Modifier.weight(1f), Icons.Outlined.DeveloperBoard, "RAM", ram)
            }
            SpecRowCard(surfaceColor, RoundedCornerShape(topStart = small, topEnd = small, bottomStart = large, bottomEnd = large)) {
                SpecItem(Modifier.weight(1f), Icons.Outlined.GridView, stringResource(R.string.about_storage), storage)
                SpecItem(Modifier.weight(1f), Icons.Outlined.BatteryStd, stringResource(R.string.about_battery), battery)
                SpecItem(Modifier.weight(1f), Icons.Outlined.Smartphone, stringResource(R.string.about_screen), screen)
            }
        } else {
            SpecRowCard(surfaceColor, RoundedCornerShape(topStart = large, topEnd = large, bottomStart = small, bottomEnd = small)) {
                SpecItem(Modifier.weight(1f), Icons.Outlined.Memory, stringResource(R.string.about_processor), processor)
                SpecItem(Modifier.weight(1f), Icons.Outlined.CameraAlt, stringResource(R.string.about_camera), "$rearCamera\n$frontCamera")
            }
            SpecRowCard(surfaceColor, RoundedCornerShape(small)) {
                SpecItem(Modifier.weight(1f), Icons.Outlined.DeveloperBoard, "RAM", ram)
                SpecItem(Modifier.weight(1f), Icons.Outlined.GridView, stringResource(R.string.about_storage), storage)
            }
            SpecRowCard(surfaceColor, RoundedCornerShape(topStart = small, topEnd = small, bottomStart = large, bottomEnd = large)) {
                SpecItem(Modifier.weight(1f), Icons.Outlined.BatteryStd, stringResource(R.string.about_battery), battery)
                SpecItem(Modifier.weight(1f), Icons.Outlined.Smartphone, stringResource(R.string.about_screen), screen)
            }
        }
    }
}

@Composable
private fun SpecRowCard(
    color: Color,
    shape: RoundedCornerShape,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = shape, color = color) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SpecItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
) {
    Column(modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeviceDetailsSection(androidVersion: String, onSeeAll: () -> Unit) {
    val cardRadius = 20.dp
    val surfaceColor = MaterialTheme.colorScheme.surfaceBright

    Column {
        Text(
            stringResource(R.string.about_device_details),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(1.5.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(88.dp),
                shape = RoundedCornerShape(topStart = cardRadius, bottomStart = cardRadius, topEnd = 4.dp, bottomEnd = 4.dp),
                color = surfaceColor,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        stringResource(R.string.about_android_version),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        androidVersion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(88.dp)
                    .clickable(onClick = onSeeAll),
                shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = cardRadius, bottomEnd = cardRadius),
                color = surfaceColor,
            ) {
                Row(
					modifier = Modifier
						.fillMaxSize()
						.padding(16.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            stringResource(R.string.about_see_all),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "More info",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    buildType: String,
    maintainer: String,
) {
    val isOfficial = buildType.lowercase() in listOf("official", "stable")
    val containerColor = if (isOfficial) MaterialTheme.colorScheme.secondaryContainer
    else MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (isOfficial) MaterialTheme.colorScheme.onSecondaryContainer
    else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isOfficial) {
                Icon(
                    imageVector = Icons.Outlined.Verified,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = contentColor,
                )
            }
            Text(
                text = if (isOfficial) "$buildType · $maintainer" else maintainer,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DeviceNameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit device name") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Device name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text.trim())
                    }
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
