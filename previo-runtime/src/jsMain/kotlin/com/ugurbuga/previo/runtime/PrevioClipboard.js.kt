package com.ugurbuga.previo.runtime

import kotlinx.browser.window

actual fun copyToClipboard(text: String) {
    // Best-effort: use the Clipboard API if available
    try {
        window.navigator.clipboard.writeText(text)
    } catch (_: Exception) {
        // Clipboard API not available
    }
}
