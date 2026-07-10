package io.github.daisukikaffuchino.ciallo0fff.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ciallo0fff.shared.generated.resources.*
import ciallo0fff.shared.generated.resources.Res
import com.yhz.composetoast.Toast
import io.github.daisukikaffuchino.ciallo0fff.ColorCommand
import io.github.daisukikaffuchino.ciallo0fff.CommandDialog
import io.github.daisukikaffuchino.ciallo0fff.ControllerColors
import io.github.daisukikaffuchino.ciallo0fff.HyperMode
import io.github.daisukikaffuchino.ciallo0fff.Kanban
import io.github.daisukikaffuchino.ciallo0fff.ScrollablePage
import io.github.daisukikaffuchino.ciallo0fff.expressiveContainerColor
import io.github.daisukikaffuchino.ciallo0fff.expressiveShape
import io.github.daisukikaffuchino.ciallo0fff.imageResource
import io.github.daisukikaffuchino.ciallo0fff.looksLikeJson
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ControllerScreen(
    modifier: Modifier,
    connected: Boolean,
    connecting: Boolean,
    userText: String,
    statusText: String,
    connect: () -> Unit,
    disconnect: () -> Unit,
    sendCommand: (String, String) -> Unit,
    sendRaw: (String) -> Boolean,
    autoReconnect: Boolean,
    reconnectCount: Int,
    setAutoReconnect: (Boolean) -> Unit,
    showMoreColors: Boolean,
    setShowMoreColors: (Boolean) -> Unit,
    rainbowActive: Boolean,
    setRainbowActive: (Boolean) -> Unit,
    rainbowRounds: Int,
    hyperActive: Boolean,
    setHyperActive: (Boolean) -> Unit,
    hyperMode: HyperMode,
    setHyperMode: (HyperMode) -> Unit,
    hyperCount: Int,
    kanban: Kanban,
    hideDesktopScrollbar: Boolean,
) {
    var customCommandOpen by remember { mutableStateOf(false) }
    val customCommandInvalidToast = stringResource(Res.string.toast_custom_command_invalid)
    ScrollablePage(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer),
        verticalSpacing = 16.dp,
        hideDesktopScrollbar = hideDesktopScrollbar,
    ) {
        item { KanbanPanel(kanban) }
        item { StatusPanel(connected, connecting, userText, statusText) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(modifier = Modifier.weight(1f), onClick = connect, enabled = !connected && !connecting) {
                    Text(stringResource(Res.string.action_connect), maxLines = 1)
                }
                Button(modifier = Modifier.weight(1f), onClick = disconnect, enabled = connected || connecting) {
                    Text(stringResource(Res.string.action_disconnect), maxLines = 1)
                }
            }
        }
        item {
            ControlPanel(
                enabled = connected,
                sendCommand = sendCommand,
                hyperActive = hyperActive,
                setHyperActive = setHyperActive,
                hyperMode = hyperMode,
                setHyperMode = setHyperMode,
                hyperCount = hyperCount,
                customCommand = { customCommandOpen = true },
                autoReconnect = autoReconnect,
                reconnectCount = reconnectCount,
                setAutoReconnect = setAutoReconnect,
            )
        }
        item {
            ControllerSegmentedGroup {
                ControllerSwitchSegment(
                    title = stringResource(Res.string.rainbow_show_more),
                    summary = stringResource(Res.string.rainbow_show_more_summary),
                    checked = showMoreColors,
                    onCheckedChange = setShowMoreColors,
                )
                if (showMoreColors) {
                    RainbowSegment(connected, rainbowActive, setRainbowActive, rainbowRounds)
                    ColorGridSegment(connected, sendCommand)
                }
            }
        }
    }
    if (customCommandOpen) {
        CommandDialog(
            title = stringResource(Res.string.command_custom),
            initialValue = """{
  "type": "command",
  "command": "",
  "color": "",
  "text_label": ""
}""",
            onDismiss = { customCommandOpen = false },
            confirmText = stringResource(Res.string.action_send),
            onSave = {
                if (looksLikeJson(it)) {
                    sendRaw(it)
                } else {
                    Toast.show(customCommandInvalidToast)
                }
                customCommandOpen = false
            },
        )
    }
}

@Composable
private fun StatusPanel(connected: Boolean, connecting: Boolean, userText: String, statusText: String) {
    val statusFontFamily = FontFamily(Font(Res.font.consola))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = expressiveShape(),
        colors = CardDefaults.cardColors(containerColor = expressiveContainerColor()),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (connected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline),
                )
                Text(userText, style = MaterialTheme.typography.titleMedium)
            }
            Text(statusText, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = statusFontFamily))
            if (connecting) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun KanbanPanel(kanban: Kanban) {
    val kanbanLabel = stringResource(kanban.labelRes)
    Card(
        shape = expressiveShape(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Image(
            painter = painterResource(kanban.imageResource()),
            contentDescription = kanbanLabel,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 7f)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Black,
                            0.68f to Color.Black,
                            1f to Color.Transparent,
                        ),
                        blendMode = BlendMode.DstIn,
                    )
                },
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun ControlPanel(
    enabled: Boolean,
    sendCommand: (String, String) -> Unit,
    hyperActive: Boolean,
    setHyperActive: (Boolean) -> Unit,
    hyperMode: HyperMode,
    setHyperMode: (HyperMode) -> Unit,
    hyperCount: Int,
    customCommand: () -> Unit,
    autoReconnect: Boolean,
    reconnectCount: Int,
    setAutoReconnect: (Boolean) -> Unit,
) {
    var hyperModeMenuOpen by remember { mutableStateOf(false) }
    val commandLightOn = stringResource(Res.string.command_light_on)
    val commandLightOff = stringResource(Res.string.command_light_off)
    val commandToggle = stringResource(Res.string.command_toggle)
    val hyperModeEnabled = enabled && !hyperActive
    ControllerSegmentedGroup {
        Surface(color = expressiveContainerColor(), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    onClick = { sendCommand("white", commandLightOn) },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) { Text(commandLightOn, maxLines = 1) }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    onClick = { sendCommand("black", commandLightOff) },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) { Text(commandLightOff, maxLines = 1) }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    onClick = { sendCommand("toggle", commandToggle) },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) { Text(commandToggle, maxLines = 1) }
            }
        }
        Surface(color = expressiveContainerColor(), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(Res.string.hyper_mode_title),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box {
                        Surface(
                            onClick = { hyperModeMenuOpen = true },
                            enabled = hyperModeEnabled,
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            Text(
                                stringResource(hyperMode.labelRes),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (hyperModeEnabled) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                        DropdownMenu(
                            expanded = hyperModeMenuOpen,
                            onDismissRequest = { hyperModeMenuOpen = false },
                        ) {
                            HyperMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(mode.labelRes)) },
                                    onClick = {
                                        setHyperMode(mode)
                                        hyperModeMenuOpen = false
                                    },
                                )
                            }
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = enabled,
                        onClick = { setHyperActive(!hyperActive) }) {
                        Text(if (hyperActive) stringResource(Res.string.hyper_stop) else stringResource(Res.string.hyper_start))
                    }
                    Text(stringResource(Res.string.format_sent_times, hyperCount), Modifier.weight(1f))
                }
            }
        }
        Surface(color = expressiveContainerColor(), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    onClick = customCommand
                ) {
                    Text(stringResource(Res.string.command_custom))
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.setting_auto_reconnect),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(Res.string.format_times, reconnectCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = autoReconnect,
                        onCheckedChange = setAutoReconnect
                    )
                }
            }
        }
    }
}

@Composable
private fun ControllerSegmentedGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(expressiveShape()),
        shape = expressiveShape(),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content,
        )
    }
}

@Composable
private fun ControllerSwitchSegment(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        modifier = Modifier.fillMaxWidth(),
        color = expressiveContainerColor(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun RainbowSegment(
    enabled: Boolean,
    rainbowActive: Boolean,
    setRainbowActive: (Boolean) -> Unit,
    rainbowRounds: Int
) {
    Surface(color = expressiveContainerColor(), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(modifier = Modifier.weight(1f), enabled = enabled, onClick = { setRainbowActive(!rainbowActive) }) {
                Text(if (rainbowActive) stringResource(Res.string.rainbow_stop) else stringResource(Res.string.rainbow_start))
            }
            Text(stringResource(Res.string.format_sent_rounds, rainbowRounds), Modifier.weight(1f))
        }
    }
}

@Composable
private fun ColorGridSegment(enabled: Boolean, sendCommand: (String, String) -> Unit) {
    Surface(color = expressiveContainerColor(), modifier = Modifier.fillMaxWidth()) {
        ColorGrid(
            enabled = enabled,
            sendCommand = sendCommand,
            modifier = Modifier.padding(18.dp),
        )
    }
}

@Composable
private fun ColorGrid(
    enabled: Boolean,
    sendCommand: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val columns = when {
            maxWidth >= 840.dp -> 6
            maxWidth >= 680.dp -> 5
            maxWidth >= 520.dp -> 4
            else -> 3
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ControllerColors.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowItems.forEach { item ->
                        val label = stringResource(item.labelRes)
                        ColorTile(
                            item = item,
                            label = label,
                            enabled = enabled,
                            modifier = Modifier.weight(1f),
                        ) {
                            sendCommand(item.name, label)
                        }
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorTile(
    item: ColorCommand,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val start = lerp(item.color, Color.White, 0.42f)
    val end = lerp(item.color, Color.Black, 0.08f)
    val labelColor = if (item.color.luminance() < 0.36f) Color.White else Color(0xFF202124)
    Card(
        onClick = { if (enabled) onClick() },
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(8.dp),
    ) {
        Box(
            Modifier
                .height(72.dp)
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(start, item.color.copy(alpha = 0.86f), end),
                    ),
                )
                .padding(8.dp),
        ) {
            Text(label, color = labelColor, maxLines = 1)
        }
    }
}
