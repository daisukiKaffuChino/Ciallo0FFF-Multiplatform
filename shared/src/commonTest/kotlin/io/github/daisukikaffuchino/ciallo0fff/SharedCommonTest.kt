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
}
