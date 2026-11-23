package com.ugurbuga.previo.runtime

/**
 * Platform-agnostic clipboard text setter.
 * Implementations are provided per-platform via actual declarations.
 */
expect fun copyToClipboard(text: String)
