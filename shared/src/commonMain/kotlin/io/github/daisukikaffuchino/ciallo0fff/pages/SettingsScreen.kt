package io.github.daisukikaffuchino.ciallo0fff.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ciallo0fff.shared.generated.resources.*
import ciallo0fff.shared.generated.resources.Res
import ciallo0fff.shared.generated.resources.ic_battery_error
import ciallo0fff.shared.generated.resources.ic_brightness_4
import ciallo0fff.shared.generated.resources.ic_dns
import ciallo0fff.shared.generated.resources.ic_explore
import ciallo0fff.shared.generated.resources.ic_lock
import ciallo0fff.shared.generated.resources.ic_mood_heart
import ciallo0fff.shared.generated.resources.ic_moon_stars
import ciallo0fff.shared.generated.resources.ic_palette
import ciallo0fff.shared.generated.resources.ic_person_edit
import ciallo0fff.shared.generated.resources.ic_router
import ciallo0fff.shared.generated.resources.ic_swipe_vertical
import com.yhz.composetoast.Toast
import io.github.daisukikaffuchino.ciallo0fff.ChoiceDialog
import io.github.daisukikaffuchino.ciallo0fff.CommandDialog
import io.github.daisukikaffuchino.ciallo0fff.DefaultIdentityJson
import io.github.daisukikaffuchino.ciallo0fff.Kanban
import io.github.daisukikaffuchino.ciallo0fff.PlatformActions
import io.github.daisukikaffuchino.ciallo0fff.ScrollablePage
import io.github.daisukikaffuchino.ciallo0fff.SettingSwitchRow
import io.github.daisukikaffuchino.ciallo0fff.SettingValueRow
import io.github.daisukikaffuchino.ciallo0fff.ThemeMode
import io.github.daisukikaffuchino.ciallo0fff.UserAgentMode
import io.github.daisukikaffuchino.ciallo0fff.looksLikeJson
import io.github.daisukikaffuchino.ciallo0fff.segmentedSection
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsScreen(
    modifier: Modifier,
    serverAddress: String,
    setServerAddress: (String) -> Unit,
    identityJson: String,
    setIdentityJson: (String) -> Unit,
    userAgentMode: UserAgentMode,
    setUserAgentMode: (UserAgentMode) -> Unit,
    customUserAgent: String,
    setCustomUserAgent: (String) -> Unit,
    kanban: Kanban,
    setKanban: (Kanban) -> Unit,
    dynamicColor: Boolean,
    setDynamicColor: (Boolean) -> Unit,
    themeMode: ThemeMode,
    setThemeMode: (ThemeMode) -> Unit,
    developmentMode: Boolean,
    setDevelopmentMode: (Boolean) -> Unit,
    hideDesktopScrollbar: Boolean,
    setHideDesktopScrollbar: (Boolean) -> Unit,
    fullRequestHeaders: Boolean,
    setFullRequestHeaders: (Boolean) -> Unit,
    trustAllCertificates: Boolean,
    setTrustAllCertificates: (Boolean) -> Unit,
) {
    var serverDialog by remember { mutableStateOf(false) }
    var identityDialog by remember { mutableStateOf(false) }
    var uaDialog by remember { mutableStateOf(false) }
    var customUaDialog by remember { mutableStateOf(false) }
    var kanbanDialog by remember { mutableStateOf(false) }
    var themeModeDialog by remember { mutableStateOf(false) }
    val serverAddressEmptyToast = stringResource(Res.string.toast_server_address_empty)
    val identityJsonInvalidToast = stringResource(Res.string.toast_identity_json_invalid)
    val customUserAgentEmptyToast = stringResource(Res.string.toast_custom_user_agent_empty)
    val batteryOptimizationNotNeededToast = stringResource(Res.string.toast_battery_optimization_not_needed)
    ScrollablePage(
        modifier = modifier,
        verticalSpacing = 4.dp,
        hideDesktopScrollbar = hideDesktopScrollbar
    ) {
        segmentedSection(Res.string.section_connection) {
            SettingValueRow(stringResource(Res.string.setting_server_address), serverAddress, Res.drawable.ic_router) { serverDialog = true }
            SettingValueRow(
                stringResource(Res.string.setting_identity_request),
                if (identityJson == DefaultIdentityJson) stringResource(Res.string.setting_default) else stringResource(Res.string.setting_custom),
                Res.drawable.ic_person_edit
            ) { identityDialog = true }
            SettingValueRow(stringResource(Res.string.setting_user_agent), stringResource(userAgentMode.labelRes), Res.drawable.ic_explore) { uaDialog = true }
            SettingSwitchRow(
                stringResource(Res.string.setting_full_headers),
                stringResource(Res.string.setting_full_headers_summary),
                fullRequestHeaders,
                setFullRequestHeaders,
                icon = Res.drawable.ic_dns
            )
            SettingSwitchRow(
                stringResource(Res.string.setting_trust_ssl),
                stringResource(Res.string.setting_trust_ssl_summary),
                trustAllCertificates,
                setTrustAllCertificates,
                icon = Res.drawable.ic_lock
            )
        }
        segmentedSection(Res.string.section_theme) {
            SettingValueRow(stringResource(Res.string.setting_kanban), stringResource(kanban.labelRes), Res.drawable.ic_mood_heart) { kanbanDialog = true }
            SettingValueRow(stringResource(Res.string.setting_dark_mode), stringResource(themeMode.labelRes), Res.drawable.ic_moon_stars) { themeModeDialog = true }
            if (PlatformActions.isAndroid) {
                SettingSwitchRow(
                    stringResource(Res.string.setting_dynamic_color),
                    stringResource(Res.string.setting_dynamic_color_summary),
                    dynamicColor && PlatformActions.supportsDynamicColor,
                    setDynamicColor,
                    icon = Res.drawable.ic_palette,
                    enabled = PlatformActions.supportsDynamicColor,
                )
            }
        }
        if (PlatformActions.isAndroid) {
            segmentedSection(Res.string.section_external_options) {
                SettingValueRow(
                    stringResource(Res.string.setting_ignore_battery),
                    stringResource(Res.string.setting_ignore_battery_summary),
                    Res.drawable.ic_battery_error,
                ) {
                    if (!PlatformActions.requestIgnoreBatteryOptimization()) {
                        Toast.show(batteryOptimizationNotNeededToast)
                    }
                }
                SettingValueRow(
                    stringResource(Res.string.setting_extreme_dark_mode),
                    stringResource(Res.string.setting_extreme_dark_mode_summary),
                    Res.drawable.ic_brightness_4,
                    enabled = PlatformActions.supportsExtremeDarkMode,
                ) { PlatformActions.openExtremeDarkModeSettings() }
            }
        }
        if (PlatformActions.usesDesktopScrollbars) {
            segmentedSection(Res.string.section_desktop) {
                SettingSwitchRow(
                    title = stringResource(Res.string.setting_hide_scrollbar),
                    summary = stringResource(Res.string.setting_hide_scrollbar_summary),
                    checked = hideDesktopScrollbar,
                    onCheckedChange = setHideDesktopScrollbar,
                    icon = Res.drawable.ic_swipe_vertical,
                )
            }
        }
        if (PlatformActions.isTestEnvironment) {
            segmentedSection(Res.string.section_developer) {
                SettingSwitchRow(
                    title = stringResource(Res.string.setting_development_mode),
                    summary = stringResource(Res.string.setting_development_mode_summary),
                    checked = developmentMode,
                    onCheckedChange = setDevelopmentMode,
                    icon = Res.drawable.ic_code,
                )
            }
        }
    }
    if (serverDialog) {
        CommandDialog(stringResource(Res.string.dialog_server_address), serverAddress, { serverDialog = false }, {
            if (it.isNotBlank()) {
                setServerAddress(it.trim())
            } else {
                Toast.show(serverAddressEmptyToast)
            }
            serverDialog = false
        }, singleLine = true)
    }
    if (identityDialog) {
        CommandDialog(
            title = stringResource(Res.string.dialog_identity_request),
            initialValue = identityJson,
            onDismiss = { identityDialog = false },
            onSave = {
                if (looksLikeJson(it)) {
                    setIdentityJson(it)
                } else {
                    Toast.show(identityJsonInvalidToast)
                }
                identityDialog = false
            },
            neutralText = stringResource(Res.string.setting_restore_default),
            onNeutral = {
                setIdentityJson(DefaultIdentityJson)
                identityDialog = false
            },
        )
    }
    if (uaDialog) {
        ChoiceDialog(
            title = stringResource(Res.string.setting_user_agent),
            values = UserAgentMode.entries,
            label = { stringResource(it.labelRes) },
            selected = { it == userAgentMode },
            onDismiss = { uaDialog = false },
            onSelected = {
                if (it == UserAgentMode.Custom) {
                    customUaDialog = true
                } else {
                    setUserAgentMode(it)
                }
                uaDialog = false
            },
        )
    }
    if (customUaDialog) {
        CommandDialog(stringResource(Res.string.dialog_custom_user_agent), customUserAgent, { customUaDialog = false }, {
            val ua = it.trim()
            if (ua.isNotEmpty()) {
                setCustomUserAgent(ua)
                setUserAgentMode(UserAgentMode.Custom)
            } else {
                Toast.show(customUserAgentEmptyToast)
            }
            customUaDialog = false
        }, singleLine = true)
    }
    if (kanbanDialog) {
        ChoiceDialog(
            title = stringResource(Res.string.dialog_select_kanban),
            values = Kanban.entries,
            label = { stringResource(it.labelRes) },
            selected = { it == kanban },
            onDismiss = { kanbanDialog = false },
            onSelected = {
                setKanban(it)
                kanbanDialog = false
            },
        )
    }
    if (themeModeDialog) {
        ChoiceDialog(
            title = stringResource(Res.string.setting_dark_mode),
            values = ThemeMode.entries,
            label = { stringResource(it.labelRes) },
            selected = { it == themeMode },
            onDismiss = { themeModeDialog = false },
            onSelected = {
                setThemeMode(it)
                themeModeDialog = false
            },
        )
    }
}
