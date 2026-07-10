package io.github.daisukikaffuchino.ciallo0fff

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import ciallo0fff.shared.generated.resources.Res
import ciallo0fff.shared.generated.resources.color_blue
import ciallo0fff.shared.generated.resources.color_brown
import ciallo0fff.shared.generated.resources.color_coral
import ciallo0fff.shared.generated.resources.color_cyan
import ciallo0fff.shared.generated.resources.color_gold
import ciallo0fff.shared.generated.resources.color_gray
import ciallo0fff.shared.generated.resources.color_green
import ciallo0fff.shared.generated.resources.color_indigo
import ciallo0fff.shared.generated.resources.color_lavender
import ciallo0fff.shared.generated.resources.color_magenta
import ciallo0fff.shared.generated.resources.color_maroon
import ciallo0fff.shared.generated.resources.color_navy
import ciallo0fff.shared.generated.resources.color_olive
import ciallo0fff.shared.generated.resources.color_orange
import ciallo0fff.shared.generated.resources.color_pink
import ciallo0fff.shared.generated.resources.color_purple
import ciallo0fff.shared.generated.resources.color_red
import ciallo0fff.shared.generated.resources.color_salmon
import ciallo0fff.shared.generated.resources.color_silver
import ciallo0fff.shared.generated.resources.color_teal
import ciallo0fff.shared.generated.resources.color_turquoise
import ciallo0fff.shared.generated.resources.color_yellow
import ciallo0fff.shared.generated.resources.hyper_mode_off
import ciallo0fff.shared.generated.resources.hyper_mode_on
import ciallo0fff.shared.generated.resources.hyper_mode_toggle
import ciallo0fff.shared.generated.resources.kanban_meguru
import ciallo0fff.shared.generated.resources.kanban_murasame
import ciallo0fff.shared.generated.resources.kanban_yoshino
import ciallo0fff.shared.generated.resources.page_about
import ciallo0fff.shared.generated.resources.page_controller
import ciallo0fff.shared.generated.resources.page_settings
import ciallo0fff.shared.generated.resources.theme_mode_dark
import ciallo0fff.shared.generated.resources.theme_mode_light
import ciallo0fff.shared.generated.resources.theme_mode_system
import ciallo0fff.shared.generated.resources.ua_mode_custom
import ciallo0fff.shared.generated.resources.ua_mode_default
import ciallo0fff.shared.generated.resources.ua_mode_random
import org.jetbrains.compose.resources.StringResource

const val DefaultServerAddress = "wss://ws.0fff.top"
const val DefaultIdentityJson = """{"client_type":"controller","client_id":null}"""
const val DevelopmentModeServerPayload = """{"type":"status","message":"Welcome controller 123"}"""
const val AppVersionName = "2.0.0"
const val AppVersionCode = 260709
const val AppVersionDisplay = "$AppVersionName ($AppVersionCode)"

enum class AppPage(val titleRes: StringResource) {
    Controller(Res.string.page_controller),
    Settings(Res.string.page_settings),
    About(Res.string.page_about),
}

enum class UserAgentMode(val labelRes: StringResource) {
    Default(Res.string.ua_mode_default),
    Random(Res.string.ua_mode_random),
    Custom(Res.string.ua_mode_custom),
}

enum class ThemeMode(val labelRes: StringResource) {
    System(Res.string.theme_mode_system),
    Light(Res.string.theme_mode_light),
    Dark(Res.string.theme_mode_dark),
}

enum class HyperMode(val labelRes: StringResource) {
    On(Res.string.hyper_mode_on),
    Off(Res.string.hyper_mode_off),
    Toggle(Res.string.hyper_mode_toggle),
}

enum class Kanban(val labelRes: StringResource) {
    Yoshino(Res.string.kanban_yoshino),
    Murasame(Res.string.kanban_murasame),
    Meguru(Res.string.kanban_meguru),
}

data class ColorCommand(
    val name: String,
    val labelRes: StringResource,
    val color: Color,
)

val ControllerColors = listOf(
    ColorCommand("Red", Res.string.color_red, Color(0xFFFF0000)),
    ColorCommand("Blue", Res.string.color_blue, Color(0xFF0000FF)),
    ColorCommand("Pink", Res.string.color_pink, Color(0xFFFFC0CB)),
    ColorCommand("Green", Res.string.color_green, Color(0xFF008000)),
    ColorCommand("Yellow", Res.string.color_yellow, Color(0xFFFFFF00)),
    ColorCommand("Purple", Res.string.color_purple, Color(0xFF800080)),
    ColorCommand("Orange", Res.string.color_orange, Color(0xFFFFA500)),
    ColorCommand("Gray", Res.string.color_gray, Color(0xFF808080)),
    ColorCommand("Brown", Res.string.color_brown, Color(0xFFA52A2A)),
    ColorCommand("Cyan", Res.string.color_cyan, Color(0xFF00FFFF)),
    ColorCommand("Magenta", Res.string.color_magenta, Color(0xFFFF00FF)),
    ColorCommand("Gold", Res.string.color_gold, Color(0xFFFFD700)),
    ColorCommand("Silver", Res.string.color_silver, Color(0xFFC0C0C0)),
    ColorCommand("Teal", Res.string.color_teal, Color(0xFF008080)),
    ColorCommand("Lavender", Res.string.color_lavender, Color(0xFFE6E6FA)),
    ColorCommand("Coral", Res.string.color_coral, Color(0xFFFF7F50)),
    ColorCommand("Indigo", Res.string.color_indigo, Color(0xFF4B0082)),
    ColorCommand("Olive", Res.string.color_olive, Color(0xFF808000)),
    ColorCommand("Maroon", Res.string.color_maroon, Color(0xFF800000)),
    ColorCommand("Turquoise", Res.string.color_turquoise, Color(0xFF40E0D0)),
    ColorCommand("Salmon", Res.string.color_salmon, Color(0xFFFA8072)),
    ColorCommand("Navy", Res.string.color_navy, Color(0xFF000080)),
)

data class WebSocketOptions(
    val fullRequestHeaders: Boolean,
    val trustAllCertificates: Boolean,
    val userAgent: String,
)

interface WebSocketEvents {
    fun onOpen()
    fun onClosed(code: Int, reason: String?)
    fun onMessage(text: String)
    fun onError(message: String)
}

expect class PlatformWebSocketClient(
    url: String,
    options: WebSocketOptions,
    events: WebSocketEvents,
) {
    fun connect()
    fun send(text: String): Boolean
    fun close()
    fun isOpen(): Boolean
}

expect object AppSettings {
    fun getString(key: String, defaultValue: String): String
    fun putString(key: String, value: String)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
}

expect fun defaultUserAgent(): String
expect fun userAgentChromeVersion(): String
expect fun userAgentBuildId(): String
expect fun formattedNow(): String
expect fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme?

expect object PlatformActions {
    val isAndroid: Boolean
    val usesDesktopScrollbars: Boolean
    val canOpenLiveRoomInClient: Boolean
    val isTestEnvironment: Boolean
    val supportsDynamicColor: Boolean
    val supportsExtremeDarkMode: Boolean
    fun openLiveRoomWebsite()
    fun openLiveRoomClient()
    fun openUrl(url: String)
    fun requestIgnoreBatteryOptimization(): Boolean
    fun openExtremeDarkModeSettings()
    fun exitApp()
}

fun commandJson(color: String, label: String): String =
    """{"type":"command","command":"set_background","color":"${jsonEscape(color)}","text_label":"${jsonEscape(label)}"}"""

fun randomUserAgent(): String {
    val releases = listOf("9", "10", "11", "12", "13", "14", "15", "16", "17")
    val deviceModels = listOf(
        "SM-N975F",
        "NX712J",
        "GE2AE",
        "V2218A",
        "NOP-AN00",
        "PGT-AN10",
        "V2266A",
        "2203121C",
        "MNA-AL00",
        "M2012K11AC",
        "2210132G",
        "ELS-AN00",
        "CPH2415",
        "CPH2583",
        "PGZ110",
        "LNA-AL00",
    )
    return "Mozilla/5.0 (Linux; Android ${releases.random()}; ${deviceModels.random()} Build/${userAgentBuildId()}) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${userAgentChromeVersion()} Mobile Safari/537.36"
}

fun looksLikeJson(text: String): Boolean {
    val value = text.trim()
    return (value.startsWith("{") && value.endsWith("}")) ||
        (value.startsWith("[") && value.endsWith("]"))
}

fun extractStatusMessage(text: String): String? {
    if (!text.contains(""""type"""") || !text.contains(""""status"""")) return null
    val match = Regex(""""message"\s*:\s*"((?:\\.|[^"\\])*)"""").find(text) ?: return null
    return match.groupValues[1]
        .replace("\\n", "\n")
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
}

private fun jsonEscape(value: String): String =
    buildString {
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
    }
