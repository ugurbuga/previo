package com.ugurbuga.previo.runtime

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual fun copyToClipboard(text: String) {
    try {
        val selection = StringSelection(text)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
    } catch (_: Exception) {
        // Desktop clipboard may not be available in all JVM environments
    }
}
