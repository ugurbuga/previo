package com.ugurbuga.previo.runtime

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

// Context is provided lazily via PrevioApplication or Previo.launch(context)
internal var _previoAppContext: Context? = null

actual fun copyToClipboard(text: String) {
    val ctx = _previoAppContext ?: return
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    cm.setPrimaryClip(ClipData.newPlainText("Previo Tree", text))
}

/**
 * Call this once with your Application context (e.g. in Application.onCreate or Previo.launch)
 * so that clipboard operations work on Android.
 */
fun initPrevioAndroid(context: Context) {
    _previoAppContext = context.applicationContext
}
