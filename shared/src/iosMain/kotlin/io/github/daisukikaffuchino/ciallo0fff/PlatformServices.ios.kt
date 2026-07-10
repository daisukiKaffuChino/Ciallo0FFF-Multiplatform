package io.github.daisukikaffuchino.ciallo0fff

import androidx.compose.material3.ColorScheme
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSURL
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.UIKit.UIApplication

actual object AppSettings {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String, defaultValue: String): String =
        defaults.stringForKey(key) ?: defaultValue

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, key)
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        if (defaults.objectForKey(key) == null) defaultValue else defaults.boolForKey(key)

    actual fun putBoolean(key: String, value: Boolean) {
        defaults.setBool(value, key)
    }
}

actual fun defaultUserAgent(): String =
    "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 Version/17.0 Mobile/15E148 Safari/604.1"

actual fun userAgentChromeVersion(): String = "126.0.6478.127"

actual fun userAgentBuildId(): String = "UP1A.231005.007"

actual fun formattedNow(): String =
    NSDateFormatter().apply {
        dateFormat = "MM/dd - HH:mm:ss"
    }.stringFromDate(NSDate())

actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

actual object PlatformActions {
    actual val isAndroid: Boolean = false
    actual val usesDesktopScrollbars: Boolean = false
    actual val canOpenLiveRoomInClient: Boolean = false
    actual val isTestEnvironment: Boolean = false
    actual val supportsDynamicColor: Boolean = false
    actual val supportsExtremeDarkMode: Boolean = false
    actual fun openLiveRoomWebsite() {
        openUrl("https://live.bilibili.com/h5/23049483")
    }

    actual fun openUrl(url: String) {
        NSURL.URLWithString(url)?.let { UIApplication.sharedApplication.openURL(it) }
    }

    actual fun openLiveRoomClient() = Unit
    actual fun requestIgnoreBatteryOptimization(): Boolean = false
    actual fun openExtremeDarkModeSettings() = Unit
    actual fun exitApp() = Unit
}

actual class PlatformWebSocketClient actual constructor(
    url: String,
    options: WebSocketOptions,
    private val events: WebSocketEvents,
) {
    actual fun connect() {
        events.onError("iOS WebSocket 尚未接入；当前迁移目标为 Android 手机和 Desktop PC")
    }

    actual fun send(text: String): Boolean = false

    actual fun close() = Unit

    actual fun isOpen(): Boolean = false
}
