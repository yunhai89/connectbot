/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2025-2026 Kenny Root
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

@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package org.connectbot.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.connectbot.data.entity.Host
import org.connectbot.service.TerminalManager
import org.connectbot.ui.navigation.AppStateEnter
import org.connectbot.ui.navigation.AppStateExit
import org.connectbot.ui.navigation.ConnectBotNavHost
import org.connectbot.ui.navigation.NavDestinations
import org.connectbot.ui.theme.ConnectBotTheme
import org.connectbot.util.IconStyle
import org.connectbot.util.ThemeMode

val LocalTerminalManager = compositionLocalOf<TerminalManager?> {
    null
}

/** Stable keys for the top-level app states so transitions only run when the screen actually changes. */
private enum class AppScreen { Loading, Migration, Authentication, Main }

@Composable
fun ConnectBotApp(
    appUiState: AppUiState,
    navController: NavHostController,
    makingShortcut: Boolean,
    authRequired: Boolean,
    isAuthenticated: Boolean,
    themeMode: ThemeMode,
    onAuthenticationSuccess: () -> Unit,
    onRetryMigration: () -> Unit,
    onSelectShortcut: (Host, String?, IconStyle) -> Unit,
    onNavigateToConsole: (Host) -> Unit,
    modifier: Modifier = Modifier,
) {
    ConnectBotTheme(themeMode = themeMode) {
        val screen = when (appUiState) {
            is AppUiState.Loading -> AppScreen.Loading

            is AppUiState.MigrationInProgress, is AppUiState.MigrationFailed -> AppScreen.Migration

            is AppUiState.Ready ->
                if (authRequired && !isAuthenticated && !makingShortcut) {
                    AppScreen.Authentication
                } else {
                    AppScreen.Main
                }
        }

        AnimatedContent(
            targetState = screen,
            transitionSpec = { AppStateEnter togetherWith AppStateExit },
            contentKey = { it },
            label = "AppScreenTransition",
        ) { target ->
            when (target) {
                AppScreen.Loading -> {
                    LoadingScreen(modifier = modifier)
                }

                AppScreen.Migration -> {
                    when (val state = appUiState) {
                        is AppUiState.MigrationInProgress -> {
                            MigrationScreen(
                                uiState = MigrationUiState.InProgress(state.state),
                                onRetry = onRetryMigration,
                                modifier = modifier,
                            )
                        }

                        is AppUiState.MigrationFailed -> {
                            MigrationScreen(
                                uiState = MigrationUiState.Failed(
                                    state.error,
                                    state.debugLog,
                                ),
                                onRetry = onRetryMigration,
                                modifier = modifier,
                            )
                        }

                        else -> Unit
                    }
                }

                AppScreen.Authentication -> {
                    AuthenticationScreen(
                        onAuthenticationSuccess = onAuthenticationSuccess,
                        modifier = modifier,
                    )
                }

                AppScreen.Main -> {
                    val readyState = appUiState as? AppUiState.Ready ?: return@AnimatedContent
                    CompositionLocalProvider(LocalTerminalManager provides readyState.terminalManager) {
                        ConnectBotNavHost(
                            navController = navController,
                            startDestination = NavDestinations.HOST_LIST,
                            makingShortcut = makingShortcut,
                            onSelectShortcut = onSelectShortcut,
                            onNavigateToConsole = onNavigateToConsole,
                            modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}
