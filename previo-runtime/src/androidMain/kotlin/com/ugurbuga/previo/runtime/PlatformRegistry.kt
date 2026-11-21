package com.ugurbuga.previo.runtime

actual fun loadGeneratedRegistry() {
    try {
        val clazz = Class.forName("com.ugurbuga.previo.runtime.GeneratedPreviewRegistry")
        clazz.getDeclaredMethod("touch").invoke(null)
    } catch (_: Exception) {
    }
}
