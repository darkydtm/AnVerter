package com.anverter.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FloatingNavSwipeTest {

    @Test
    fun `maps horizontal position to tab from left to right`() {
        assertEquals(0, tabIndexForOffset(0f, width = 300, tabCount = 3))
        assertEquals(0, tabIndexForOffset(99.9f, width = 300, tabCount = 3))
        assertEquals(1, tabIndexForOffset(100f, width = 300, tabCount = 3))
        assertEquals(1, tabIndexForOffset(199.9f, width = 300, tabCount = 3))
        assertEquals(2, tabIndexForOffset(200f, width = 300, tabCount = 3))
        assertEquals(2, tabIndexForOffset(300f, width = 300, tabCount = 3))
    }

    @Test
    fun `clamps positions outside the floating bar`() {
        assertEquals(0, tabIndexForOffset(-20f, width = 300, tabCount = 3))
        assertEquals(2, tabIndexForOffset(320f, width = 300, tabCount = 3))
    }

    @Test
    fun `returns first tab for invalid dimensions`() {
        assertEquals(0, tabIndexForOffset(10f, width = 0, tabCount = 3))
        assertEquals(0, tabIndexForOffset(10f, width = 300, tabCount = 0))
    }
}
