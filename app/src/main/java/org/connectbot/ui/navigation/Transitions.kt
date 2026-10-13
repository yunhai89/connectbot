/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2026 Kenny Root
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

/**
 * Shared page transitions modeled on iOS navigation: the incoming screen
 * glides in from the trailing edge while the outgoing screen slides a shorter
 * distance and dims, producing a subtle parallax.
 */
private const val PAGE_DURATION = 420
private const val PAGE_FADE_DURATION = 300

/** Deceleration curve close to UIKit's default easing. */
val IosEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

val PageEnter: EnterTransition = slideInHorizontally(
    animationSpec = tween(PAGE_DURATION, easing = IosEasing),
    initialOffsetX = { it },
) + fadeIn(animationSpec = tween(PAGE_FADE_DURATION, delayMillis = 80))

val PageExit: ExitTransition = slideOutHorizontally(
    animationSpec = tween(PAGE_DURATION, easing = IosEasing),
    targetOffsetX = { -it / 4 },
) + fadeOut(animationSpec = tween(PAGE_FADE_DURATION))

val PagePopEnter: EnterTransition = slideInHorizontally(
    animationSpec = tween(PAGE_DURATION, easing = IosEasing),
    initialOffsetX = { -it / 4 },
) + fadeIn(animationSpec = tween(PAGE_FADE_DURATION))

val PagePopExit: ExitTransition = slideOutHorizontally(
    animationSpec = tween(PAGE_DURATION, easing = IosEasing),
    targetOffsetX = { it },
) + fadeOut(animationSpec = tween(PAGE_FADE_DURATION))

/** Modal presentation for the console: rises from the bottom, recedes upward when covered. */
val ModalEnter: EnterTransition = slideInVertically(
    animationSpec = tween(PAGE_DURATION, easing = IosEasing),
    initialOffsetY = { it / 4 },
) + fadeIn(animationSpec = tween(PAGE_FADE_DURATION))

val ModalExit: ExitTransition = slideOutVertically(
    animationSpec = tween(PAGE_DURATION, easing = IosEasing),
    targetOffsetY = { -it / 6 },
) + fadeOut(animationSpec = tween(PAGE_FADE_DURATION))

val ModalPopEnter: EnterTransition = slideInVertically(
    animationSpec = tween(PAGE_DURATION, easing = IosEasing),
    initialOffsetY = { -it / 6 },
) + fadeIn(animationSpec = tween(PAGE_FADE_DURATION))

val ModalPopExit: ExitTransition = slideOutVertically(
    animationSpec = tween(PAGE_DURATION, easing = IosEasing),
    targetOffsetY = { it },
) + fadeOut(animationSpec = tween(PAGE_FADE_DURATION))

/** Gentle cross-fade used between full-screen app states (loading, migration, auth, main). */
val AppStateEnter: EnterTransition = fadeIn(animationSpec = tween(PAGE_DURATION, delayMillis = 60)) +
    scaleIn(initialScale = 0.98f, animationSpec = tween(PAGE_DURATION, easing = IosEasing))

val AppStateExit: ExitTransition = fadeOut(animationSpec = tween(PAGE_FADE_DURATION)) +
    scaleOut(targetScale = 1.02f, animationSpec = tween(PAGE_DURATION, easing = IosEasing))
