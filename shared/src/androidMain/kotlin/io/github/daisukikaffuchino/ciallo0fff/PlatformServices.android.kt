package io.github.daisukikaffuchino.ciallo0fff

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Base64
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import java.lang.ref.WeakReference
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.concurrent.thread

private object AndroidContextHolder {
    var context: Context? = null
    var activity: WeakReference<Activity>? = null
}

fun initializeAndroidApp(context: Context) {
    AndroidContextHolder.context = context.applicationContext
    AndroidContextHolder.activity = (context as? Activity)?.let(::WeakReference)
}

actual object AppSettings {
    private val prefs
        get() = requireNotNull(AndroidContextHolder.context) {
            "initializeAndroidApp(context) must be called before App()"
        }.getSharedPreferences("ciallo0fff", Context.MODE_PRIVATE)

    actual fun getString(key: String, defaultValue: String): String =
        prefs.getString(key, defaultValue) ?: defaultValue

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}

actual fun defaultUserAgent(): String =
    AndroidContextHolder.context?.let { WebSettings.getDefaultUserAgent(it) }
        ?: "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 Chrome/126.0 Mobile Safari/537.36"

actual fun userAgentChromeVersion(): String {
    val webViewVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WebView.getCurrentWebViewPackage()?.versionName
    } else {
        null
    }
    return webViewVersion
        ?: Regex("""Chrome/([^\s]+)""").find(defaultUserAgent())?.groupValues?.get(1)
        ?: "126.0.0.0"
}

actual fun userAgentBuildId(): String = Build.ID

actual fun formattedNow(): String =
    LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd - HH:mm:ss"))

actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    val context = AndroidContextHolder.context ?: return null
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    return if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
}

actual object PlatformActions {
    actual val isAndroid: Boolean = true
    actual val usesDesktopScrollbars: Boolean = false
    actual val supportsDynamicColor: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    actual val supportsExtremeDarkMode: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    actual val isTestEnvironment: Boolean
        get() = AndroidContextHolder.context?.let { context ->
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } ?: false

    actual val canOpenLiveRoomInClient: Boolean
        get() = AndroidContextHolder.context?.let { context ->
            runCatching {
                context.packageManager.getPackageInfo("tv.danmaku.bili", 0)
                true
            }.getOrDefault(false)
        } ?: false

    actual fun openLiveRoomWebsite() {
        openUrl("https://live.bilibili.com/h5/23049483")
    }

    actual fun openLiveRoomClient() {
        if (!canOpenLiveRoomInClient) return
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("bilibili://live/23049483")
                setPackage("tv.danmaku.bili")
            },
        )
    }

    actual fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    actual fun requestIgnoreBatteryOptimization(): Boolean {
        val context = AndroidContextHolder.context ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) return false
        return startActivity(
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            },
        )
    }

    actual fun openExtremeDarkModeSettings() {
        if (!supportsExtremeDarkMode) return
        startActivity(Intent("android.settings.REDUCE_BRIGHT_COLORS_SETTINGS"))
    }

    actual fun exitApp() {
        AndroidContextHolder.activity?.get()?.finishAndRemoveTask()
    }

    private fun startActivity(intent: Intent): Boolean {
        val context = AndroidContextHolder.context ?: return false
        return runCatching {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.isSuccess
    }
}

actual class PlatformWebSocketClient actual constructor(
    url: String,
    options: WebSocketOptions,
    private val events: WebSocketEvents,
) {
    private val socketClient = BasicWebSocketClient(url, options, events)

    actual fun connect() = socketClient.connect()
    actual fun send(text: String): Boolean = socketClient.send(text)
    actual fun close() = socketClient.close()
    actual fun isOpen(): Boolean = socketClient.isOpen()
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
        val key = Base64.encodeToString(keyBytes, Base64.NO_WRAP)
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
