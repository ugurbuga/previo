package com.ugurbuga.previo.runtime

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Previo {
    private val _isPrevioOpen = MutableStateFlow(false)
    val isPrevioOpen: StateFlow<Boolean> = _isPrevioOpen.asStateFlow()

    internal var onLaunchCallback: (() -> Unit)? = null

    fun launch(context: Any? = null) {
        setOpen(true)
        launchPrevio(context)
    }

    fun dismiss() {
        _isPrevioOpen.value = false
    }

    internal fun setOpen(open: Boolean) {
        _isPrevioOpen.value = open
    }

    internal fun triggerLaunchCallback() {
        onLaunchCallback?.invoke()
    }
}
