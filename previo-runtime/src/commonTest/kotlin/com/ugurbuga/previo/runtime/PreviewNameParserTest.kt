package com.ugurbuga.previo.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class PreviewNameParserTest {

    @Test
    fun testValidPreviewName() {
        val metadata = PreviewNameParser.parse("[Buttons] [1] [Primary] [Enabled]")
        assertEquals("Buttons", metadata.group)
        assertEquals(1, metadata.order)
        assertEquals("Primary", metadata.alphabeticalOrder)
        assertEquals("Enabled", metadata.customInfo)
    }

    @Test
    fun testInvalidFormatThrows() {
        assertFails {
            PreviewNameParser.parse("Invalid Name Format")
        }
    }

    @Test
    fun testInvalidOrderThrows() {
        assertFails {
            PreviewNameParser.parse("[Buttons] [abc] [Primary] [Enabled]")
        }
    }
}
