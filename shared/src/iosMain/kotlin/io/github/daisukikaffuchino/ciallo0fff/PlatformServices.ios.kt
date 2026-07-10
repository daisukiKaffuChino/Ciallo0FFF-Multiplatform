package io.github.daisukikaffuchino.ciallo0fff

import androidx.compose.material3.ColorScheme
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionWebSocketCloseCodeNormalClosure
import platform.Foundation.NSURLSessionWebSocketMessage
import platform.Foundation.NSURLSessionWebSocketTask
import platform.Foundation.NSUserDefaults
import platform.Foundation.setValue
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle

actual object AppSettings {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(
        key: String,
        defaultValue: String
    ): String {
        return defaults.stringForKey(key) ?: defaultValue
    }

    actual fun putString(
        key: String,
        value: String
    ) {
        defaults.setObject(value, key)
    }

    actual fun getBoolean(
        key: String,
        defaultValue: Boolean
    ): Boolean {
        return if (defaults.objectForKey(key) == null) {
            defaultValue
        } else {
            defaults.boolForKey(key)
        }
    }

    actual fun putBoolean(
        key: String,
        value: Boolean
    ) {
        defaults.setBool(value, key)
    }
}


actual fun defaultUserAgent(): String =
    "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) " +
            "AppleWebKit/605.1.15 Version/17.0 Mobile/15E148 Safari/604.1"


actual fun userAgentChromeVersion(): String =
    "126.0.6478.127"


actual fun userAgentBuildId(): String =
    "UP1A.231005.007"


actual fun formattedNow(): String =
    NSDateFormatter().apply {
        dateFormat = "MM/dd - HH:mm:ss"
    }.stringFromDate(NSDate())


actual fun platformDynamicColorScheme(
    darkTheme: Boolean
): ColorScheme? = null


actual fun platformSystemDarkTheme(): Boolean? = null


actual object PlatformActions {

    actual val isAndroid = false
    actual val usesDesktopScrollbars = false
    actual val canOpenLiveRoomInClient = false
    actual val isTestEnvironment = false
    actual val supportsDynamicColor = false
    actual val supportsExtremeDarkMode = false


    actual fun openLiveRoomWebsite() {
        openUrl("https://live.bilibili.com/h5/23049483")
    }


    actual fun openUrl(url: String) {
        NSURL.URLWithString(url)?.let {
            UIApplication.sharedApplication.openURL(
                it,
                options = emptyMap<Any?, Any>(),
                completionHandler = null
            )
        }
    }


    actual fun openLiveRoomClient() = Unit


    actual fun requestIgnoreBatteryOptimization(): Boolean =
        false


    actual fun openExtremeDarkModeSettings() = Unit


    actual fun applyThemeMode(
        mode: ThemeMode
    ) {

        val style = when (mode) {

            ThemeMode.System ->
                UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified

            ThemeMode.Light ->
                UIUserInterfaceStyle.UIUserInterfaceStyleLight

            ThemeMode.Dark ->
                UIUserInterfaceStyle.UIUserInterfaceStyleDark
        }


        IosRootViewControllerHolder.controller
            ?.overrideUserInterfaceStyle = style

    }


    actual fun exitApp() = Unit
}


actual class PlatformWebSocketClient actual constructor(
    url: String,
    options: WebSocketOptions,
    private val events: WebSocketEvents,
) {


    private val request =
        NSURL.URLWithString(url)?.let { nsUrl ->

            NSMutableURLRequest.requestWithURL(nsUrl).apply {

                if (options.fullRequestHeaders) {

                    setValue(
                        "no-cache",
                        forHTTPHeaderField = "Pragma"
                    )

                    setValue(
                        "no-cache",
                        forHTTPHeaderField = "Cache-Control"
                    )

                    setValue(
                        options.userAgent,
                        forHTTPHeaderField = "User-Agent"
                    )

                    setValue(
                        "http://0fff.top",
                        forHTTPHeaderField = "Origin"
                    )

                    setValue(
                        "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
                        forHTTPHeaderField = "Accept-Language"
                    )
                }
            }
        }


    private var task: NSURLSessionWebSocketTask? = null

    private var open = false


    actual fun connect() {

        val nextRequest = request

        if (nextRequest == null) {
            events.onError("Invalid WebSocket URL")
            return
        }


        val nextTask =
            NSURLSession.sharedSession
                .webSocketTaskWithRequest(nextRequest)


        task = nextTask
        open = true


        nextTask.resume()

        events.onOpen()

        receiveNext()
    }


    actual fun send(
        text: String
    ): Boolean {

        val currentTask = task ?: return false

        if (!open) {
            return false
        }


        currentTask.sendMessage(
            NSURLSessionWebSocketMessage(
                string = text
            )
        ) { error ->

            error?.let {
                events.onError(
                    it.localizedDescription
                )
            }

        }


        return true
    }


    actual fun close() {

        open = false

        task?.cancelWithCloseCode(
            NSURLSessionWebSocketCloseCodeNormalClosure,
            reason = null
        )

        task = null
    }


    actual fun isOpen(): Boolean =
        open


    private fun receiveNext() {

        val currentTask = task ?: return


        currentTask.receiveMessageWithCompletionHandler { message,
                                                          error ->


            if (error != null) {

                open = false

                events.onError(
                    error.localizedDescription
                )

                events.onClosed(
                    -1,
                    error.localizedDescription
                )

                return@receiveMessageWithCompletionHandler
            }


            message?.string?.let {
                events.onMessage(it)
            }


            if (open) {
                receiveNext()
            }

        }
    }
}