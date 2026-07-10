package io.github.daisukikaffuchino.ciallo0fff

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

internal object IosRootViewControllerHolder {
    var controller: UIViewController? = null
}

fun MainViewController(): UIViewController {
    val controller = ComposeUIViewController { App() }
    IosRootViewControllerHolder.controller = controller
    return controller
}
