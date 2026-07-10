package io.github.daisukikaffuchino.ciallo0fff

import androidx.compose.material3.ColorScheme
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.InputStream
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Base64
import java.util.prefs.Preferences
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.extensions.permessage_deflate.PerMessageDeflateExtension
import org.java_websocket.handshake.ServerHandshake

actual object AppSettings {
    private val prefs = Preferences.userRoot().node("io/github/daisukikaffuchino/ciallo0fff")

    actual fun getString(key: String, defaultValue: String): String = prefs.get(key, defaultValue)

    actual fun putString(key: String, value: String) {
        prefs.put(key, value)
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean = prefs.getBoolean(key, defaultValue)

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.putBoolean(key, value)
    }
}

actual fun defaultUserAgent(): String =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36"

actual fun userAgentChromeVersion(): String = "126.0.6478.127"

actual fun userAgentBuildId(): String = "UP1A.231005.007"

actual fun formattedNow(): String =
    LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd - HH:mm:ss"))

actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

actual fun platformSystemDarkTheme(): Boolean? {
    val osName = System.getProperty("os.name").lowercase()
    return when {
        "windows" in osName -> windowsSystemDarkTheme()
        "mac" in osName || "darwin" in osName -> macOsSystemDarkTheme()
        else -> null
    }
}

private fun windowsSystemDarkTheme(): Boolean? {
    val output = commandOutput(
        "reg",
        "query",
        "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
        "/v",
        "AppsUseLightTheme",
    ) ?: return null
    val value = Regex("""AppsUseLightTheme\s+REG_DWORD\s+0x([0-9a-fA-F]+)""")
        .find(output)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull(16)
        ?: return null
    return value == 0
}

private fun macOsSystemDarkTheme(): Boolean? {
    val output = commandOutput("defaults", "read", "-g", "AppleInterfaceStyle")
    return output?.trim()?.equals("Dark", ignoreCase = true) ?: false
}

private fun commandOutput(vararg command: String): String? =
    runCatching {
        val process = ProcessBuilder(*command)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val finished = process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            null
        } else {
            output
        }
    }.getOrNull()

actual object PlatformActions {
    actual val isAndroid: Boolean = false
    actual val usesDesktopScrollbars: Boolean = true
    actual val canOpenLiveRoomInClient: Boolean = false
    actual val supportsDynamicColor: Boolean = false
    actual val supportsExtremeDarkMode: Boolean = false
    actual val isTestEnvironment: Boolean
        get() = System.getProperty("ciallo0fff.testEnvironment") == "true"

    actual fun openLiveRoomWebsite() {
        openUrl("https://live.bilibili.com/h5/23049483")
    }

    actual fun openUrl(url: String) {
        runCatching {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
            }
        }
    }

    actual fun openLiveRoomClient() = Unit
    actual fun requestIgnoreBatteryOptimization(): Boolean = false
    actual fun openExtremeDarkModeSettings() = Unit
    actual fun applyThemeMode(mode: ThemeMode) = Unit
    actual fun exitApp() {
        exitProcess(0)
    }
}

actual class PlatformWebSocketClient actual constructor(
    url: String,
    options: WebSocketOptions,
    private val events: WebSocketEvents,
) {
    private val uri = URI(url)
    private val socketClient = object : WebSocketClient(uri, perMessageDeflateDraft(), webSocketHeaders(options)) {
        override fun onOpen(handshakedata: ServerHandshake?) {
            events.onOpen()
        }

        override fun onMessage(message: String?) {
            message?.let(events::onMessage)
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            events.onClosed(code, reason)
        }

        override fun onError(ex: Exception?) {
            events.onError(ex?.message ?: ex?.javaClass?.simpleName ?: "WebSocket error")
        }
    }.apply {
        if (uri.scheme.equals("wss", ignoreCase = true) && options.trustAllCertificates) {
            setSocketFactory(trustAllSocketFactory())
        }
    }

    actual fun connect() {
        socketClient.connect()
    }

    actual fun send(text: String): Boolean =
        socketClient.isOpen && runCatching {
            socketClient.send(text)
            true
        }.getOrElse {
            events.onError(it.message ?: "send failed")
            false
        }

    actual fun close() {
        runCatching { socketClient.close() }
    }

    actual fun isOpen(): Boolean = socketClient.isOpen
}

private fun perMessageDeflateDraft(): Draft =
    Draft_6455(PerMessageDeflateExtension())

private fun webSocketHeaders(options: WebSocketOptions): Map<String, String> =
    if (options.fullRequestHeaders) {
        mapOf(
            "Pragma" to "no-cache",
            "Cache-Control" to "no-cache",
            "User-Agent" to options.userAgent,
            "Origin" to "http://0fff.top",
            "Accept-Language" to "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
        )
    } else {
        emptyMap()
    }

private class BasicWebSocketClient(
    url: String,
    private val options: WebSocketOptions,
    private val events: WebSocketEvents,
) {
    private val uri = URI(url)
    private val outputLock = Any()
    private val random = SecureRandom()
    @Volatile private var socket: Socket? = null
    @Volatile private var input: BufferedInputStream? = null
    @Volatile private var output: BufferedOutputStream? = null
    @Volatile private var open = false
    @Volatile private var closeNotified = false

    fun connect() {
        thread(name = "Ciallo0FFF-WebSocket", isDaemon = true) {
            runCatching {
                val nextSocket = createSocket()
                socket = nextSocket
                input = BufferedInputStream(nextSocket.getInputStream())
                output = BufferedOutputStream(nextSocket.getOutputStream())
                writeHandshake()
                readHandshakeResponse()
                open = true
                events.onOpen()
                readLoop()
            }.onFailure {
                if (!closeNotified) events.onError(it.message ?: it::class.simpleName ?: "connect failed")
                notifyClosed(-1, it.message)
            }
        }
    }

    fun send(text: String): Boolean {
        if (!open) return false
        return runCatching {
            writeFrame(0x1, text.encodeToByteArray())
            true
        }.getOrElse {
            events.onError(it.message ?: "send failed")
            false
        }
    }

    fun close() {
        runCatching {
            if (open) writeFrame(0x8, ByteArray(0))
            socket?.close()
        }
        open = false
    }

    fun isOpen(): Boolean = open

    private fun createSocket(): Socket {
        val secure = uri.scheme.equals("wss", ignoreCase = true)
        val port = if (uri.port > 0) uri.port else if (secure) 443 else 80
        val nextSocket = if (secure) {
            val factory = if (options.trustAllCertificates) trustAllSocketFactory() else SSLSocketFactory.getDefault() as SSLSocketFactory
            factory.createSocket()
        } else {
            Socket()
        }
        nextSocket.connect(InetSocketAddress(uri.host, port), 10_000)
        if (nextSocket is SSLSocket) nextSocket.startHandshake()
        return nextSocket
    }

    private fun writeHandshake() {
        val path = buildString {
            append(if (uri.rawPath.isNullOrBlank()) "/" else uri.rawPath)
            if (!uri.rawQuery.isNullOrBlank()) append("?").append(uri.rawQuery)
        }
        val port = if (uri.port > 0) uri.port else if (uri.scheme == "wss") 443 else 80
        val host = if ((uri.scheme == "wss" && port == 443) || (uri.scheme == "ws" && port == 80)) uri.host else "${uri.host}:$port"
        val keyBytes = ByteArray(16).also(random::nextBytes)
        val key = Base64.getEncoder().encodeToString(keyBytes)
        val request = buildString {
            append("GET ").append(path).append(" HTTP/1.1\r\n")
            append("Host: ").append(host).append("\r\n")
            append("Upgrade: websocket\r\n")
            append("Connection: Upgrade\r\n")
            append("Sec-WebSocket-Key: ").append(key).append("\r\n")
            append("Sec-WebSocket-Version: 13\r\n")
            if (options.fullRequestHeaders) {
                append("Pragma: no-cache\r\n")
                append("Cache-Control: no-cache\r\n")
                append("User-Agent: ").append(options.userAgent).append("\r\n")
                append("Origin: http://0fff.top\r\n")
                append("Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7\r\n")
            }
            append("\r\n")
        }
        output?.write(request.encodeToByteArray())
        output?.flush()
    }

    private fun readHandshakeResponse() {
        val stream = input ?: throw EOFException("missing input")
        val status = stream.readHttpLine()
        if (!status.contains(" 101 ")) throw IllegalStateException(status)
        while (stream.readHttpLine().isNotEmpty()) {
            // Skip headers.
        }
    }

    private fun readLoop() {
        val stream = input ?: return
        while (open) {
            val b0 = stream.readRequired()
            val b1 = stream.readRequired()
            val opcode = b0 and 0x0f
            val masked = (b1 and 0x80) != 0
            var length = (b1 and 0x7f).toLong()
            if (length == 126L) length = stream.readUInt16().toLong()
            if (length == 127L) length = stream.readUInt64()
            if (length > Int.MAX_VALUE) throw IllegalStateException("frame too large")
            val mask = if (masked) stream.readExact(4) else null
            val payload = stream.readExact(length.toInt())
            if (mask != null) {
                for (index in payload.indices) payload[index] = (payload[index].toInt() xor mask[index % 4].toInt()).toByte()
            }
            when (opcode) {
                0x1 -> events.onMessage(payload.decodeToString())
                0x8 -> {
                    open = false
                    notifyClosed(1000, "remote closed")
                }
                0x9 -> writeFrame(0xA, payload)
            }
        }
    }

    private fun writeFrame(opcode: Int, payload: ByteArray) {
        val stream = output ?: throw EOFException("missing output")
        val mask = ByteArray(4).also(random::nextBytes)
        synchronized(outputLock) {
            stream.write(0x80 or opcode)
            when {
                payload.size <= 125 -> stream.write(0x80 or payload.size)
                payload.size <= 0xffff -> {
                    stream.write(0x80 or 126)
                    stream.write((payload.size shr 8) and 0xff)
                    stream.write(payload.size and 0xff)
                }
                else -> {
                    stream.write(0x80 or 127)
                    val size = payload.size.toLong()
                    for (shift in 56 downTo 0 step 8) stream.write(((size shr shift) and 0xff).toInt())
                }
            }
            stream.write(mask)
            payload.forEachIndexed { index, byte ->
                stream.write(byte.toInt() xor mask[index % 4].toInt())
            }
            stream.flush()
        }
    }

    private fun notifyClosed(code: Int, reason: String?) {
        if (!closeNotified) {
            closeNotified = true
            open = false
            events.onClosed(code, reason)
        }
    }
}

private fun InputStream.readHttpLine(): String {
    val bytes = ByteArrayOutputStream()
    while (true) {
        val value = read()
        if (value < 0) throw EOFException("unexpected end of stream")
        if (value == '\n'.code) break
        if (value != '\r'.code) bytes.write(value)
    }
    return bytes.toString("ISO-8859-1")
}

private fun InputStream.readRequired(): Int {
    val value = read()
    if (value < 0) throw EOFException("unexpected end of stream")
    return value
}

private fun InputStream.readExact(size: Int): ByteArray {
    val data = ByteArray(size)
    var offset = 0
    while (offset < size) {
        val read = read(data, offset, size - offset)
        if (read < 0) throw EOFException("unexpected end of stream")
        offset += read
    }
    return data
}

private fun InputStream.readUInt16(): Int =
    (readRequired() shl 8) or readRequired()

private fun InputStream.readUInt64(): Long {
    var value = 0L
    repeat(8) {
        value = (value shl 8) or readRequired().toLong()
    }
    return value
}

private fun trustAllSocketFactory(): SSLSocketFactory {
    val trustManagers = arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        },
    )
    return SSLContext.getInstance("TLS").apply {
        init(null, trustManagers, SecureRandom())
    }.socketFactory
}
