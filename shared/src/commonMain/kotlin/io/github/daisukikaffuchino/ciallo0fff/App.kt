package io.github.daisukikaffuchino.ciallo0fff

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ciallo0fff.shared.generated.resources.*
import com.yhz.composetoast.ProvideToastManager
import com.yhz.composetoast.Toast
import io.github.daisukikaffuchino.ciallo0fff.pages.AboutScreen
import io.github.daisukikaffuchino.ciallo0fff.pages.ControllerScreen
import io.github.daisukikaffuchino.ciallo0fff.pages.SettingsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private val LocalSegmentedGroup = staticCompositionLocalOf { false }

@Composable
@Preview
fun App() {
    val appFontFamily = FontFamily(Font(Res.font.harmony_os_sans_sc_regular))
    var themeMode by remember {
        mutableStateOf(
            runCatching {
                ThemeMode.valueOf(AppSettings.getString("themeMode", ThemeMode.System.name))
            }.getOrDefault(ThemeMode.System),
        )
    }
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    MaterialTheme(
        colorScheme = sakuraColorScheme(darkTheme),
        typography = Typography().withFontFamily(appFontFamily),
    ) {
        ProvideToastManager {
            ControllerApp(
                themeMode = themeMode,
                setThemeMode = {
                    themeMode = it
                    AppSettings.putString("themeMode", it.name)
                },
            )
        }
    }
}

private fun Typography.withFontFamily(fontFamily: FontFamily): Typography =
    Typography(
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = titleLarge.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
        labelSmall = labelSmall.copy(fontFamily = fontFamily),
    )

private fun sakuraColorScheme(darkTheme: Boolean): ColorScheme =
    if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFFFB1C8),
            onPrimary = Color(0xFF65002F),
            primaryContainer = Color(0xFF8F1648),
            onPrimaryContainer = Color(0xFFFFD9E2),
            secondary = Color(0xFFE4BDC7),
            onSecondary = Color(0xFF432932),
            secondaryContainer = Color(0xFF5B3F48),
            onSecondaryContainer = Color(0xFFFFD9E2),
            tertiary = Color(0xFFF1BE95),
            onTertiary = Color(0xFF4A280D),
            tertiaryContainer = Color(0xFF653E21),
            onTertiaryContainer = Color(0xFFFFDCC4),
            background = Color(0xFF171114),
            onBackground = Color(0xFFEDE0E4),
            surface = Color(0xFF171114),
            onSurface = Color(0xFFEDE0E4),
            surfaceVariant = Color(0xFF514348),
            onSurfaceVariant = Color(0xFFD6C2C8),
            surfaceBright = Color(0xFF3F3438),
            surfaceContainer = Color(0xFF24191E),
            surfaceContainerHigh = Color(0xFF2F2328),
            surfaceContainerHighest = Color(0xFF3A2E33),
            outline = Color(0xFF9E8C92),
            outlineVariant = Color(0xFF514348),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFFB82F62),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFD9E2),
            onPrimaryContainer = Color(0xFF3F001D),
            secondary = Color(0xFF75565F),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFD9E2),
            onSecondaryContainer = Color(0xFF2B151C),
            tertiary = Color(0xFF7E5538),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFDCC4),
            onTertiaryContainer = Color(0xFF301400),
            background = Color(0xFFFFF8FA),
            onBackground = Color(0xFF21191C),
            surface = Color(0xFFFFF8FA),
            onSurface = Color(0xFF21191C),
            surfaceVariant = Color(0xFFF2DDE4),
            onSurfaceVariant = Color(0xFF514348),
            surfaceBright = Color(0xFFFFF8FA),
            surfaceContainer = Color(0xFFF7EDF1),
            surfaceContainerHigh = Color(0xFFF1E7EB),
            surfaceContainerHighest = Color(0xFFECE1E6),
            outline = Color(0xFF837379),
            outlineVariant = Color(0xFFD6C2C8),
        )
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControllerApp(
    themeMode: ThemeMode,
    setThemeMode: (ThemeMode) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var selectedPage by remember { mutableStateOf(AppPage.Controller) }
    var serverAddress by remember { mutableStateOf(AppSettings.getString("serverAddress", DefaultServerAddress)) }
    var identityJson by remember { mutableStateOf(AppSettings.getString("identityJson", DefaultIdentityJson)) }
    var customUserAgent by remember { mutableStateOf(AppSettings.getString("customUserAgent", "")) }
    var userAgentMode by remember {
        mutableStateOf(
            UserAgentMode.valueOf(
                AppSettings.getString(
                    "userAgentMode",
                    UserAgentMode.Default.name
                )
            )
        )
    }
    var kanban by remember { mutableStateOf(Kanban.valueOf(AppSettings.getString("kanban", Kanban.Yoshino.name))) }
    var fullRequestHeaders by remember { mutableStateOf(AppSettings.getBoolean("fullRequestHeaders", true)) }
    var trustAllCertificates by remember { mutableStateOf(AppSettings.getBoolean("trustAllCertificates", true)) }
    var autoReconnect by remember { mutableStateOf(false) }
    var reconnectCount by remember { mutableStateOf(0) }
    var connected by remember { mutableStateOf(false) }
    var connecting by remember { mutableStateOf(false) }
    val disconnectedText = stringResource(Res.string.status_disconnected)
    val idleText = stringResource(Res.string.status_idle)
    val connectingText = stringResource(Res.string.status_connecting)
    val connectedText = stringResource(Res.string.status_connected)
    val sendSuccessText = stringResource(Res.string.status_send_success)
    val sendFailureText = stringResource(Res.string.status_send_failure)
    val identitySentText = stringResource(Res.string.status_identity_sent)
    val connectionClosedPrefix = stringResource(Res.string.status_connection_closed_prefix)
    val errorPrefix = stringResource(Res.string.status_error_prefix)
    val sendFailedToast = stringResource(Res.string.status_send_failed)
    val commandLightOn = stringResource(Res.string.command_light_on)
    val commandLightOff = stringResource(Res.string.command_light_off)
    val commandToggle = stringResource(Res.string.command_toggle)
    val controllerColorLabels = ControllerColors.map { stringResource(it.labelRes) }
    var userText by remember { mutableStateOf(disconnectedText) }
    var statusText by remember { mutableStateOf(idleText) }
    var showMoreColors by remember { mutableStateOf(false) }
    var rainbowActive by remember { mutableStateOf(false) }
    var rainbowIndex by remember { mutableStateOf(0) }
    var rainbowRounds by remember { mutableStateOf(0) }
    var hyperActive by remember { mutableStateOf(false) }
    var hyperMode by remember { mutableStateOf(HyperMode.Toggle) }
    var hyperToggleLight by remember { mutableStateOf(false) }
    var hyperCount by remember { mutableStateOf(0) }
    var dynamicColor by remember { mutableStateOf(AppSettings.getBoolean("dynamicColor", false)) }
    var hideDesktopScrollbar by remember { mutableStateOf(AppSettings.getBoolean("hideDesktopScrollbar", false)) }
    var developmentMode by remember { mutableStateOf(AppSettings.getBoolean("developmentMode", false)) }
    var client by remember { mutableStateOf<PlatformWebSocketClient?>(null) }
    val pageStateHolder = rememberSaveableStateHolder()
    val effectiveDevelopmentMode = PlatformActions.isTestEnvironment && developmentMode

    fun timedStatus(label: String): String = "$label - ${formattedNow()}"

    fun selectedUserAgent(): String = when (userAgentMode) {
        UserAgentMode.Default -> defaultUserAgent()
        UserAgentMode.Random -> randomUserAgent()
        UserAgentMode.Custom -> customUserAgent.ifBlank { defaultUserAgent() }
    }

    fun sendRaw(text: String, handshake: Boolean = false): Boolean {
        val sent = if (effectiveDevelopmentMode && connected) true else client?.send(text) == true
        if (!handshake) {
            statusText = if (sent) timedStatus(sendSuccessText) else timedStatus(sendFailureText)
        } else if (!sent) {
            statusText = timedStatus(sendFailureText)
        }
        if (!sent) {
            Toast.show(sendFailedToast)
        }
        return sent
    }

    fun sendCommand(color: String, label: String) {
        sendRaw(commandJson(color, label))
    }

    fun connect() {
        if (connecting || connected) return
        if (effectiveDevelopmentMode) {
            connecting = false
            connected = true
            reconnectCount = 0
            val fakeStatus = extractStatusMessage(DevelopmentModeServerPayload) ?: DevelopmentModeServerPayload
            userText = fakeStatus
            statusText = timedStatus(connectedText)
            client = null
            return
        }
        connecting = true
        userText = connectingText
        statusText = serverAddress
        val nextClient = PlatformWebSocketClient(
            url = serverAddress,
            options = WebSocketOptions(fullRequestHeaders, trustAllCertificates, selectedUserAgent()),
            events = object : WebSocketEvents {
                override fun onOpen() {
                    scope.launch {
                        connected = true
                        connecting = false
                        reconnectCount = 0
                        userText = connectedText
                        statusText = timedStatus(connectedText)
                        sendRaw(identityJson, handshake = true)
                    }
                }

                override fun onClosed(code: Int, reason: String?) {
                    scope.launch {
                        connected = false
                        connecting = false
                        userText = disconnectedText
                        statusText = "$connectionClosedPrefix$code${reason?.let { " $it" } ?: ""}"
                        rainbowActive = false
                        hyperActive = false
                        if (autoReconnect) {
                            reconnectCount += 1
                            delay(900)
                            connect()
                        }
                    }
                }

                override fun onMessage(text: String) {
                    scope.launch {
                        val status = extractStatusMessage(text)
                        if (status != null) {
                            statusText = status
                            if (status.contains("Welcome controller")) userText = status
                        } else {
                            statusText = text.take(160)
                        }
                    }
                }

                override fun onError(message: String) {
                    scope.launch {
                        connecting = false
                        statusText = "$errorPrefix$message"
                    }
                }
            },
        )
        client = nextClient
        nextClient.connect()
    }

    fun disconnect() {
        autoReconnect = false
        if (effectiveDevelopmentMode) {
            connecting = false
            connected = false
            userText = disconnectedText
            statusText = idleText
            return
        }
        connecting = true
        client?.close()
    }

    LaunchedEffect(rainbowActive, connected) {
        while (isActive && rainbowActive && connected) {
            val command = ControllerColors[rainbowIndex]
            sendCommand(command.name, controllerColorLabels[rainbowIndex])
            val next = rainbowIndex + 1
            if (next >= ControllerColors.size) {
                rainbowIndex = 0
                rainbowRounds += 1
            } else {
                rainbowIndex = next
            }
            delay(400)
        }
    }

    LaunchedEffect(hyperActive, connected, hyperMode) {
        while (isActive && hyperActive && connected) {
            when (hyperMode) {
                HyperMode.On -> sendCommand("white", commandLightOn)
                HyperMode.Off -> sendCommand("black", commandLightOff)
                HyperMode.Toggle -> {
                    val nextLight = !hyperToggleLight
                    sendCommand(if (nextLight) "white" else "black", if (nextLight) commandLightOn else commandLightOff)
                    hyperToggleLight = nextLight
                }
            }
            hyperCount += 1
            delay(10)
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 800.dp
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            if (useNavigationRail) {
                AppNavigationRail(selectedPage) { selectedPage = it }
            }
            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                topBar = {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .height(52.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(Res.string.app_name),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                            )
                            ConnectionStatusChip(connected)
                            AppBarMenu()
                        }
                    }
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        AppNavigationBar(selectedPage) { selectedPage = it }
                    }
                },
            ) { padding ->
                AnimatedContent(
                    targetState = selectedPage,
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    transitionSpec = {
                        val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                        val offsetFactor = 0.10f
                        val enter = fadeIn(tween(220)) +
                                scaleIn(initialScale = 0.98f, animationSpec = tween(220)) +
                                slideInHorizontally(animationSpec = tween(260)) {
                                    (it * offsetFactor).toInt() * direction
                                }
                        val exit = fadeOut(tween(120)) +
                                scaleOut(targetScale = 0.99f, animationSpec = tween(120)) +
                                slideOutHorizontally(animationSpec = tween(260)) {
                                    -(it * offsetFactor).toInt() * direction
                                }
                        enter togetherWith exit
                    },
                    label = "PageTransition",
                ) { page ->
                    pageStateHolder.SaveableStateProvider(page) {
                        val modifier = Modifier.fillMaxSize()
                        when (page) {
                            AppPage.Controller -> ControllerScreen(
                                modifier = modifier,
                                connected = connected,
                                connecting = connecting,
                                userText = userText,
                                statusText = statusText,
                                connect = ::connect,
                                disconnect = ::disconnect,
                                sendCommand = ::sendCommand,
                                sendRaw = ::sendRaw,
                                autoReconnect = autoReconnect,
                                reconnectCount = reconnectCount,
                                setAutoReconnect = { autoReconnect = it },
                                showMoreColors = showMoreColors,
                                setShowMoreColors = { showMoreColors = it },
                                rainbowActive = rainbowActive,
                                setRainbowActive = {
                                    rainbowActive = it
                                    if (!it) {
                                        rainbowIndex = 0
                                        rainbowRounds = 0
                                    }
                                },
                                rainbowRounds = rainbowRounds,
                                hyperActive = hyperActive,
                                setHyperActive = { hyperActive = it },
                                hyperMode = hyperMode,
                                setHyperMode = { hyperMode = it },
                                hyperCount = hyperCount,
                                kanban = kanban,
                                hideDesktopScrollbar = hideDesktopScrollbar,
                            )

                            AppPage.Settings -> SettingsScreen(
                                modifier = modifier,
                                serverAddress = serverAddress,
                                setServerAddress = {
                                    serverAddress = it
                                    AppSettings.putString("serverAddress", it)
                                    client = null
                                },
                                identityJson = identityJson,
                                setIdentityJson = {
                                    identityJson = it
                                    AppSettings.putString("identityJson", it)
                                },
                                userAgentMode = userAgentMode,
                                setUserAgentMode = {
                                    userAgentMode = it
                                    AppSettings.putString("userAgentMode", it.name)
                                },
                                customUserAgent = customUserAgent,
                                setCustomUserAgent = {
                                    customUserAgent = it
                                    AppSettings.putString("customUserAgent", it)
                                },
                                kanban = kanban,
                                setKanban = {
                                    kanban = it
                                    AppSettings.putString("kanban", it.name)
                                },
                                dynamicColor = dynamicColor,
                                setDynamicColor = {
                                    dynamicColor = it
                                    AppSettings.putBoolean("dynamicColor", it)
                                },
                                themeMode = themeMode,
                                setThemeMode = setThemeMode,
                                developmentMode = developmentMode,
                                setDevelopmentMode = {
                                    developmentMode = it
                                    AppSettings.putBoolean("developmentMode", it)
                                    if (connected || connecting) {
                                        autoReconnect = false
                                        client?.close()
                                        client = null
                                        connected = false
                                        connecting = false
                                        userText = disconnectedText
                                        statusText = idleText
                                    }
                                },
                                hideDesktopScrollbar = hideDesktopScrollbar,
                                setHideDesktopScrollbar = {
                                    hideDesktopScrollbar = it
                                    AppSettings.putBoolean("hideDesktopScrollbar", it)
                                },
                                fullRequestHeaders = fullRequestHeaders,
                                setFullRequestHeaders = {
                                    fullRequestHeaders = it
                                    AppSettings.putBoolean("fullRequestHeaders", it)
                                },
                                trustAllCertificates = trustAllCertificates,
                                setTrustAllCertificates = {
                                    trustAllCertificates = it
                                    AppSettings.putBoolean("trustAllCertificates", it)
                                },
                            )

                            AppPage.About -> AboutScreen(modifier, hideDesktopScrollbar)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppNavigationBar(selectedPage: AppPage, onSelected: (AppPage) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        AppPage.entries.forEach { page ->
            val pageTitle = stringResource(page.titleRes)
            NavigationBarItem(
                selected = selectedPage == page,
                onClick = { onSelected(page) },
                icon = { ResourceIcon(page.iconResource(), pageTitle) },
                label = { Text(pageTitle) },
            )
        }
    }
}

@Composable
private fun AppNavigationRail(selectedPage: AppPage, onSelected: (AppPage) -> Unit) {
    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .windowInsetsPadding(WindowInsets.systemBars),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Spacer(Modifier.height(12.dp))
        AppPage.entries.forEach { page ->
            val pageTitle = stringResource(page.titleRes)
            NavigationRailItem(
                selected = selectedPage == page,
                onClick = { onSelected(page) },
                icon = { ResourceIcon(page.iconResource(), pageTitle) },
                label = { Text(pageTitle) },
            )
        }
    }
}

@Composable
private fun ConnectionStatusChip(connected: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceBright,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (connected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline),
            )
            Text(
                if (connected) stringResource(Res.string.status_online) else stringResource(Res.string.status_offline),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AppBarMenu() {
    var expanded by remember { mutableStateOf(false) }
    val moreText = stringResource(Res.string.action_more)
    Box {
        IconButton(onClick = { expanded = true }) {
            ResourceIcon(
                resource = Res.drawable.ic_more_vert,
                contentDescription = moreText,
                tintColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_live_room)) },
                onClick = {
                    expanded = false
                    PlatformActions.openLiveRoomWebsite()
                },
            )
            if (PlatformActions.isAndroid && PlatformActions.canOpenLiveRoomInClient) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.action_open_in_client)) },
                    onClick = {
                        expanded = false
                        PlatformActions.openLiveRoomClient()
                    },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_exit_run)) },
                onClick = {
                    expanded = false
                    PlatformActions.exitApp()
                },
            )
        }
    }
}

@Composable
internal fun ScrollablePage(
    modifier: Modifier,
    verticalSpacing: Dp,
    hideDesktopScrollbar: Boolean,
    content: ResponsivePageScope.() -> Unit,
) {
    val state = rememberScrollState()
    val pageScope = ResponsivePageScope().apply(content)
    val pageItems = pageScope.items
    Box(modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val columns = if (maxWidth >= 720.dp) 2 else 1
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state)
                    .padding(16.dp),
            ) {
                if (columns == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
                        pageItems.forEach { pageItem -> pageItem() }
                    }
                } else {
                    val splitIndex = (pageItems.size + 1) / 2
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                        ) {
                            pageItems.take(splitIndex).forEach { pageItem -> pageItem() }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                        ) {
                            pageItems.drop(splitIndex).forEach { pageItem -> pageItem() }
                        }
                    }
                }
            }
        }
        if (PlatformActions.usesDesktopScrollbars && !hideDesktopScrollbar) {
            DesktopScrollbar(
                state = state,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 16.dp, horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun DesktopScrollbar(state: ScrollState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var trackHeightPx by remember { mutableStateOf(1) }
    val maxValue = state.maxValue
    if (maxValue <= 0) return

    val contentHeightPx = trackHeightPx + maxValue
    val thumbHeightFraction = (trackHeightPx.toFloat() / contentHeightPx.toFloat()).coerceIn(0.12f, 1f)
    val thumbHeightPx = (trackHeightPx * thumbHeightFraction).roundToInt().coerceAtLeast(36)
    val thumbHeightDp = with(density) { thumbHeightPx.toDp() }
    val thumbTravelPx = (trackHeightPx - thumbHeightPx).coerceAtLeast(1)
    val scrollFraction = state.value.toFloat() / maxValue.toFloat()
    val thumbOffsetPx = scrollFraction * thumbTravelPx

    fun scrollToPointer(pointerY: Float) {
        val thumbTop = (pointerY - thumbHeightPx / 2f).coerceIn(0f, thumbTravelPx.toFloat())
        val nextValue = (thumbTop / thumbTravelPx * maxValue).roundToInt().coerceIn(0, maxValue)
        scope.launch { state.scrollTo(nextValue) }
    }

    Box(
        modifier = modifier
            .width(12.dp)
            .onSizeChanged { trackHeightPx = it.height.coerceAtLeast(1) }
            .pointerInput(maxValue, trackHeightPx, thumbHeightPx) {
                detectDragGestures(
                    onDragStart = { offset -> scrollToPointer(offset.y) },
                    onDrag = { change, _ ->
                        change.consume()
                        scrollToPointer(change.position.y)
                    },
                )
            },
    ) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxHeight()
                .width(4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE6E6E6)),
        )
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .height(thumbHeightDp)
                .width(8.dp)
                .graphicsLayer {
                    translationY = thumbOffsetPx
                }
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFC7C7C7)),
        )
    }
}

internal class ResponsivePageScope {
    internal val items = mutableListOf<@Composable () -> Unit>()

    fun item(content: @Composable () -> Unit) {
        items += content
    }
}


@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

internal fun ResponsivePageScope.segmentedSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionTitle(title)
            SegmentedGroup(content)
            Spacer(Modifier.size(4.dp))
        }
    }
}

internal fun ResponsivePageScope.segmentedSection(
    title: StringResource,
    content: @Composable ColumnScope.() -> Unit,
) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionTitle(stringResource(title))
            SegmentedGroup(content)
            Spacer(Modifier.size(4.dp))
        }
    }
}

private fun ResponsivePageScope.segmentedGroup(
    content: @Composable ColumnScope.() -> Unit,
) {
    item {
        SegmentedGroup(content)
    }
}

@Composable
private fun SegmentedGroup(content: @Composable ColumnScope.() -> Unit) {
    CompositionLocalProvider(LocalSegmentedGroup provides true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(expressiveShape()),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content,
        )
    }
}

@Composable
internal fun expressiveShape(): RoundedCornerShape =
    RoundedCornerShape(28.dp)

@Composable
internal fun expressiveContainerColor(): Color =
    MaterialTheme.colorScheme.surfaceBright

@Composable
private fun ExpressiveItem(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    headlineContent: @Composable () -> Unit,
    supportingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val grouped = LocalSegmentedGroup.current
    val corner by animateDpAsState(
        targetValue = when {
            pressed -> 16.dp
            grouped -> 4.dp
            else -> 28.dp
        },
        animationSpec = spring(),
    )
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(corner),
        color = expressiveContainerColor(),
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            leadingIcon?.invoke()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                headlineContent()
                supportingContent?.invoke()
            }
            trailingContent?.invoke()
        }
    }
}

@Composable
private fun ExpressiveTitle(text: String) {
    Text(
        text = text,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun ExpressiveSummary(text: String) {
    Text(
        text = text,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun SettingValueRow(
    title: String,
    summary: String,
    icon: DrawableResource? = null,
    iconTint: Boolean = true,
    iconSize: Dp = 24.dp,
    trailingIcon: DrawableResource? = null,
    trailingIconTint: Boolean = true,
    trailingIconSize: Dp = 24.dp,
    onClick: () -> Unit,
) {
    ExpressiveItem(
        leadingIcon = icon?.let {
            {
                ResourceIcon(it, title, modifier = Modifier.size(iconSize), tint = iconTint)
            }
        },
        headlineContent = { ExpressiveTitle(title) },
        supportingContent = { ExpressiveSummary(summary) },
        trailingContent = trailingIcon?.let {
            {
                ResourceIcon(it, title, modifier = Modifier.size(trailingIconSize), tint = trailingIconTint)
            }
        },
        onClick = onClick,
    )
}

@Composable
internal fun SettingSwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
) {
    ExpressiveItem(
        modifier = modifier,
        leadingIcon = icon?.let {
            {
                ResourceIcon(it, title)
            }
        },
        headlineContent = { ExpressiveTitle(title) },
        supportingContent = { ExpressiveSummary(summary) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(start = 8.dp),
            )
        },
        onClick = { onCheckedChange(!checked) },
    )
}

@Composable
internal fun CommandDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    singleLine: Boolean = false,
    confirmText: String? = null,
    neutralText: String? = null,
    onNeutral: (() -> Unit)? = null,
) {
    var value by remember { mutableStateOf(initialValue) }
    val resolvedConfirmText = confirmText ?: stringResource(Res.string.action_save)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                minLines = if (singleLine) 1 else 5,
            )
        },
        confirmButton = { TextButton(onClick = { onSave(value) }) { Text(resolvedConfirmText) } },
        dismissButton = {
            Row {
                neutralText?.let { TextButton(onClick = { onNeutral?.invoke() }) { Text(it) } }
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
            }
        },
    )
}

@Composable
private fun ResourceIcon(
    resource: DrawableResource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Boolean = true,
    tintColor: Color = MaterialTheme.colorScheme.primary,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = contentDescription,
        modifier = modifier.size(24.dp),
        colorFilter = if (tint) ColorFilter.tint(tintColor) else null,
    )
}

internal fun Kanban.imageResource(): DrawableResource =
    when (this) {
        Kanban.Yoshino -> Res.drawable.kanban_yoshino
        Kanban.Murasame -> Res.drawable.kanban_murasame
        Kanban.Meguru -> Res.drawable.kanban_meguru
    }

private fun AppPage.iconResource(): DrawableResource =
    when (this) {
        AppPage.Controller -> Res.drawable.ic_terminal
        AppPage.Settings -> Res.drawable.ic_settings
        AppPage.About -> Res.drawable.ic_info
    }

@Composable
internal fun <T> ChoiceDialog(
    title: String,
    values: List<T>,
    label: @Composable (T) -> String,
    selected: (T) -> Boolean,
    onDismiss: () -> Unit,
    onSelected: (T) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                values.forEach { value ->
                    Surface(onClick = { onSelected(value) }, color = Color.Transparent) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(selected = selected(value), onClick = { onSelected(value) })
                            Text(label(value), modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) } },
    )
}
