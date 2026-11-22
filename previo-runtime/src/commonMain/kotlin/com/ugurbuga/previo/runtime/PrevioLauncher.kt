package com.ugurbuga.previo.runtime

fun launchPrevio(context: Any? = null) {
    Previo.setOpen(true)
    Previo.triggerLaunchCallback()
}
