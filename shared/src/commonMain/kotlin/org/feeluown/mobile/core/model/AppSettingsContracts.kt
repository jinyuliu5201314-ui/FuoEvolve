package org.feeluown.mobile

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
enum class LyricFontSize(
    val label: String,
) {
    Small("小"),
    Medium("中"),
    Large("大"),
}

@Serializable
enum class ThemeMode(
    val label: String,
) {
    System("跟随系统"),
    Light("亮色"),
    Dark("暗色"),
}

@Serializable
enum class ThemeColorScheme(
    val label: String,
) {
    Dynamic("系统动态"),
    ExpressiveDefault("Expressive 默认"),
    FuoGreen("青绿"),
    OceanBlue("海蓝"),
    Violet("紫罗兰"),
    Rose("蔷薇"),
    Amber("琥珀"),
}

@Serializable
enum class ThemePaletteStyle(
    val label: String,
) {
    TonalSpot("Tonal Spot"),
    Neutral("Neutral"),
    Vibrant("Vibrant"),
    Expressive("Expressive"),
    Rainbow("Rainbow"),
    FruitSalad("FruitSalad"),
    Monochrome("Monochrome"),
    Fidelity("Fidelity"),
    Content("Content"),
}

@Serializable
enum class ThemeColorSpec(
    val label: String,
) {
    Material3_2021("Material 3 (2021)"),
    Expressive_2025("Expressive (2025)"),
}

@Serializable
enum class DesktopVideoDecodeMode(
    val label: String,
    val description: String,
) {
    HardwareCompatible("兼容模式", "默认模式，优先保证画面正常"),
    HardwareDirect("性能模式", "减少画面复制，部分设备可能不兼容"),
    Software("软件解码", "使用 CPU 解码，用于硬件解码绿屏或兼容性问题"),
}

/**
 * 用户自定义的页面背景（来自系统相册）。
 *
 * [path] 指向应用私有目录内的图片副本：由平台层在用户选图后复制进来，
 * 因此背景功能不需要额外的存储读取权限，卸载应用或清除数据后自然失效。
 */
@Serializable
data class BackgroundImageSettings(
    val enabled: Boolean = false,
    val path: String? = null,
    /** 背景图之上的主题色遮罩强度，越大背景越暗/越淡。 */
    val dim: Float = DEFAULT_BACKGROUND_IMAGE_DIM,
    /** 背景图模糊半径，0 表示不模糊。 */
    val blurDp: Float = DEFAULT_BACKGROUND_IMAGE_BLUR_DP,
    /** 页面内容面板的不透明度，越小越能透出背景图。 */
    val contentAlpha: Float = DEFAULT_BACKGROUND_IMAGE_CONTENT_ALPHA,
) {
    /** 只有开关打开且已经选过图时，背景才真正参与渲染。 */
    val isActive: Boolean get() = enabled && !path.isNullOrBlank()
}

const val DEFAULT_BACKGROUND_IMAGE_DIM = 0.35f
const val DEFAULT_BACKGROUND_IMAGE_BLUR_DP = 0f
const val DEFAULT_BACKGROUND_IMAGE_CONTENT_ALPHA = 0.88f
const val MAX_BACKGROUND_IMAGE_DIM = 0.8f
const val MAX_BACKGROUND_IMAGE_BLUR_DP = 30f
const val MIN_BACKGROUND_IMAGE_CONTENT_ALPHA = 0.4f

@Serializable
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val homeSection: HomeSection = HomeSection.Recommend,
    val mineSection: MineSection = MineSection.Playlists,
    val playlistFilter: PlaylistFilter = PlaylistFilter.All,
    val localMusicViewMode: LocalMusicViewMode = LocalMusicViewMode.All,
    val excludedLocalMusicDirectoryIds: Set<String> = emptySet(),
    val includedLocalMusicDirectoryIds: Set<String> = emptySet(),
    val localMusicMinDurationSeconds: Int = DEFAULT_LOCAL_MUSIC_MIN_DURATION_SECONDS,
    val searchScope: SearchScope = SearchScope.All,
    val selectedSearchProviderId: String? = null,
    val selectedSettingsProviderId: String? = null,
    val providerLoginMode: ProviderLoginMode = ProviderLoginMode.WebView,
    val providerCookieInputs: Map<String, String> = emptyMap(),
    val providerHeaderInputs: Map<String, ProviderHeaderInput> = emptyMap(),
    val enabledProviderIds: Set<String> = DEFAULT_ENABLED_PROVIDER_IDS,
    val providerOrderIds: List<String> = DEFAULT_PROVIDER_ORDER_IDS,
    val searchProviderIds: Set<String> = emptySet(),
    val recommendProviderIds: Set<String> = emptySet(),
    val exploreProviderIds: Set<String> = emptySet(),
    val mineProviderIds: Set<String> = emptySet(),
    val playbackReportingProviderIds: Set<String> = emptySet(),
    val audioCacheLimitMb: Int = DEFAULT_AUDIO_CACHE_LIMIT_MB,
    val imageCacheLimitMb: Int = DEFAULT_IMAGE_CACHE_LIMIT_MB,
    val downloadParallelism: Int = DEFAULT_DOWNLOAD_PARALLELISM,
    val wifiAudioQualityPolicy: AudioQualityPolicy = DEFAULT_WIFI_AUDIO_QUALITY_POLICY,
    val cellularAudioQualityPolicy: AudioQualityPolicy = DEFAULT_CELLULAR_AUDIO_QUALITY_POLICY,
    val unavailablePlaybackPolicy: UnavailablePlaybackPolicy = DEFAULT_UNAVAILABLE_PLAYBACK_POLICY,
    val smartReplacementProviderIds: Set<String> = emptySet(),
    val smartReplacementMinScore: Double = DEFAULT_SMART_REPLACEMENT_MIN_SCORE,
    val smartReplacementSelections: Map<String, SmartReplacementSelection> = emptyMap(),
    val lyricsAssociations: Map<String, String> = emptyMap(),
    val lyricsAlignmentOffsetsMs: Map<String, Long> = emptyMap(),
    val pauseOnOtherAppPlayback: Boolean = DEFAULT_PAUSE_ON_OTHER_APP_PLAYBACK,
    val desktopVideoDecodeMode: DesktopVideoDecodeMode = DesktopVideoDecodeMode.HardwareCompatible,
    val lyricFontSize: LyricFontSize = LyricFontSize.Small,
    val statusBarLyricsEnabled: Boolean = false,
    val bydInstrumentLyricsEnabled: Boolean = false,
    val bluetoothLyricsEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
    val themeColorScheme: ThemeColorScheme = ThemeColorScheme.Dynamic,
    val themePaletteStyle: ThemePaletteStyle = ThemePaletteStyle.Expressive,
    val themeColorSpec: ThemeColorSpec = ThemeColorSpec.Expressive_2025,
    val dynamicCoverColorEnabled: Boolean = false,
    val backgroundImage: BackgroundImageSettings = BackgroundImageSettings(),
    val waveformAnimationDisabled: Boolean = false,
    val appUpdateChannel: AppUpdateChannel = AppUpdateChannel.Stable,
    val autoCheckAppUpdates: Boolean = true,
    val playlistPlaybackStatsVersion: Int = 0,
    val playlistPlaybackStats: Map<String, PlaylistPlaybackStat> = emptyMap(),
)

@Serializable
data class PlaylistPlaybackStat(
    val playCount: Long = 0,
    val lastPlayedAtMillis: Long = 0,
)

data class YtMusicOAuthFlowUiState(
    val userCode: String,
    val verificationUrl: String,
    val verificationUrlWithCode: String,
    val statusMessage: String = "请在浏览器中完成授权",
    val browserOpened: Boolean = false,
)

const val DEFAULT_AUDIO_CACHE_LIMIT_MB = 512
const val DEFAULT_IMAGE_CACHE_LIMIT_MB = 128
const val DEFAULT_DOWNLOAD_PARALLELISM = 2
val DEFAULT_ENABLED_PROVIDER_IDS = setOf("netease")
val DEFAULT_PROVIDER_ORDER_IDS = listOf("netease", "qqmusic", "bilibili", "ytmusic")

data class SettingsState(
    val isLoaded: Boolean = false,
    val settings: AppSettings = AppSettings(),
    val errorMessage: String? = null,
)

interface AppSettingsRepository {
    val state: StateFlow<SettingsState>
    suspend fun awaitSettings(): AppSettings
    suspend fun update(transform: (AppSettings) -> AppSettings)
    suspend fun updateThemePaletteStyle(value: ThemePaletteStyle) {
        update { settings -> settings.copy(themePaletteStyle = value) }
    }
    suspend fun updateThemeColorSpec(value: ThemeColorSpec) {
        update { settings -> settings.copy(themeColorSpec = value) }
    }
}

class InMemoryAppSettingsRepository(
    initialSettings: AppSettings = AppSettings(),
) : AppSettingsRepository {
    private val mutableState = MutableStateFlow(SettingsState(isLoaded = true, settings = initialSettings))
    override val state: StateFlow<SettingsState> = mutableState

    override suspend fun awaitSettings(): AppSettings = mutableState.value.settings

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        val current = mutableState.value.settings
        val transformed = transform(current)
        mutableState.value = SettingsState(
            isLoaded = true,
            settings = transformed.copy(
                themePaletteStyle = current.themePaletteStyle,
                themeColorSpec = current.themeColorSpec,
            ),
        )
    }

    override suspend fun updateThemePaletteStyle(value: ThemePaletteStyle) {
        val current = mutableState.value.settings
        mutableState.value = SettingsState(
            isLoaded = true,
            settings = current.copy(themePaletteStyle = value),
        )
    }

    override suspend fun updateThemeColorSpec(value: ThemeColorSpec) {
        val current = mutableState.value.settings
        mutableState.value = SettingsState(
            isLoaded = true,
            settings = current.copy(themeColorSpec = value),
        )
    }
}
