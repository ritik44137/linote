package com.linote

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class ComposerChromeTest {
    @Test fun textInsetMatchesOnBothSides_andClearsTheCircleBy3dp() {
        val chrome = composerChrome(16.dp, 20.dp, 40.dp)
        assertEquals(49f, chrome.textStart.value, 0.05f)
        assertEquals(49f, chrome.textEnd.value, 0.05f)
        assertEquals(6f, chrome.circleEnd.value, 0.05f)
    }
}
