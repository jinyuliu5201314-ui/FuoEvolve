package org.feeluown.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppRoot(
    appViewModel: FuoAppViewModel,
    uiGraph: AppUiGraph,
    platform: AppPlatformBindings,
    windowContentWrapper: @Composable (@Composable () -> Unit) -> Unit = { content -> content() },
) {
    val appUiState by appViewModel.uiState.collectAsStateWithLifecycle()
    val backgroundImage = appUiState.backgroundImage
    val backgroundBitmap = rememberBackgroundImageBitmap(
        path = if (backgroundImage.isActive) backgroundImage.path else null,
    )
    val backgroundVisible = backgroundImage.isActive && backgroundBitmap != null

    DisposableEffect(appViewModel) {
        val token = installPlaylistMigrationNotificationNavigator { request ->
            appViewModel.openPlaylistMigrationTask(request.taskId, request.target)
        }
        onDispose { uninstallPlaylistMigrationNotificationNavigator(token) }
    }

    FuoTheme(
        themeMode = appUiState.themeMode,
        themeColorScheme = appUiState.themeColorScheme,
        themePaletteStyle = appUiState.themePaletteStyle,
        themeColorSpec = appUiState.themeColorSpec,
        contentSurfaceAlpha = if (backgroundVisible) backgroundImage.contentAlpha else 1f,
    ) {
        windowContentWrapper {
            Box(Modifier.fillMaxSize()) {
                if (backgroundVisible && backgroundBitmap != null) {
                    PageBackgroundImage(
                        bitmap = backgroundBitmap,
                        settings = backgroundImage,
                    )
                }
                Box(Modifier.fillMaxSize()) {
                    when {
                        !appUiState.isInitialized -> AppInitializationLoadingScreen()
                        !appUiState.onboardingCompleted -> {
                            val onboarding = requireNotNull(uiGraph.onboarding) {
                                "Onboarding feature owner is not installed"
                            }
                            OnboardingFeatureScreen(
                                onboarding = onboarding,
                                providerCatalog = uiGraph.providerCatalog,
                                providerAuth = uiGraph.providerAuth,
                                onOpenProviderWebLogin = platform.onOpenProviderWebLogin,
                                onLogoutProvider = platform.onLogoutProvider,
                                onImportYtmusicHeaderFile = platform.onImportYtmusicHeaderFile,
                                onImportYtmusicOAuthFile = platform.onImportYtmusicOAuthFile,
                                onStartYtmusicOAuth = platform.onStartYtmusicOAuth,
                            )
                        }
                        else -> AppShell(
                            appViewModel = appViewModel,
                            uiGraph = uiGraph,
                            appUiState = appUiState,
                            platform = platform,
                        )
                    }
                }
            }
        }
    }
}
