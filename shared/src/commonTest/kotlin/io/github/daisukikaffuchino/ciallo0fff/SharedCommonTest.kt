package io.github.daisukikaffuchino.ciallo0fff

import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {

    @Test
    fun commandJsonMatchesOriginalProtocol() {
        assertEquals(
            """{"type":"command","command":"set_background","color":"white","text_label":"开灯"}""",
            commandJson("white", "开灯"),
        )
    }

    @Test
    fun statusMessageCanBeReadFromServerPayload() {
        assertEquals(
            "Welcome controller 123",
            extractStatusMessage("""{"type":"status","message":"Welcome controller 123"}"""),
        )
    }

    @Test
    fun webSocketAddressAddsDefaultSchemeWhenMissing() {
        assertEquals("ws://127.0.0.1:8080", normalizedWebSocketAddress("127.0.0.1:8080"))
        assertEquals("ws://localhost:8080", normalizedWebSocketAddress("localhost:8080"))
        assertEquals("ws://ws.0fff.top", normalizedWebSocketAddress("ws.0fff.top"))
    }

    @Test
    fun webSocketAddressConvertsHttpSchemes() {
        assertEquals("ws://127.0.0.1:8080", normalizedWebSocketAddress("http://127.0.0.1:8080"))
        assertEquals("wss://ws.0fff.top", normalizedWebSocketAddress("https://ws.0fff.top"))
        assertEquals("wss://ws.0fff.top", normalizedWebSocketAddress("wss://ws.0fff.top"))
    }
}
