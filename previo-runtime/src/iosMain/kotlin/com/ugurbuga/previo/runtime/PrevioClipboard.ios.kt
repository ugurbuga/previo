package com.ugurbuga.previo.runtime

import platform.UIKit.UIPasteboard

actual fun copyToClipboard(text: String) {
    UIPasteboard.generalPasteboard.string = text
}
