/*
 * Copyright (C) 2025-2026 AxionOS & Project Matrixx
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

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

@Composable
fun AnimatedDeviceIllustration(
        modifier: Modifier = Modifier,
        deviceName: String = "OnePlus 9 Pro",
        version: String = "16",
        atomColor: androidx.compose.ui.graphics.Color? = null
) {
        val infiniteTransition = rememberInfiniteTransition(label = "matrixx_animation")

        val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(com.android.axion.compose.R.raw.matrixx_illustration)
        )

        val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
        )

        val floatOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 6f,
                animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                ),
                label = "float"
        )

        val rotationAngle by infiniteTransition.animateFloat(
                initialValue = -1.5f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                ),
                label = "rotation"
        )

        Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
        ) {
                Box(
                        modifier = Modifier
                                .offset(y = (-floatOffset).dp)
                                .rotate(rotationAngle)
                                .width(260.dp)
                                .height(130.dp),
                        contentAlignment = Alignment.Center
                ) {
                        LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier.fillMaxSize()
                        )
                }
        }
}

private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
