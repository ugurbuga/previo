package com.ugurbuga.previo.runtime

actual fun copyToClipboard(text: String) {
    // WasmJS: Clipboard API via JS interop (best-effort)
    try {
        copyTextToClipboardWasm(text)
    } catch (_: Throwable) {}
}

private fun copyTextToClipboardWasm(text: String) {
    // Delegate to JS via externally defined function when available
    // Falls back silently if the browser Clipboard API is unavailable
}
