package io.wongxd.solution.baseView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.wongxd.solution.compose.composeTheme.AppTheme
import io.wongxd.solution.util.isInNightMode

abstract class BaseComposeFragment : BaseRootFragmentLast() {

    override val layoutId: Int
        get() = 0

    override val autoSetBackgroundColor: Boolean
        get() = false

    override val needSetStatusBarByBase: Boolean
        get() = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(android.graphics.Color.parseColor("#FCF5FF"))
    }

    protected var rootView: ComposeView? = null

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = ComposeView(requireContext()).apply {
            setContent {
                SetComposeRoot()
            }
        }
        return rootView
    }

    @Composable
    protected open fun SetComposeRoot() {
        AppTheme() {
            TransAndInsertStatusBar {
                Render()
            }
        }
    }

    protected open fun reRender() {
        rootView?.setContent { SetComposeRoot() }
    }

    @Composable
    open fun Render() {

    }

    protected var setStatusBarComposeAuto = true
    protected var resetStatusBarCompose by mutableStateOf(0L)
    protected var darkIcon by mutableStateOf(true)
    protected var statusBarColorCompose = Color.Transparent

    @Composable
    protected fun TransAndInsertStatusBar(content: @Composable () -> Unit) {

        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)

        ProvideWindowInsets {
            Box() {
                content()
            }
        }

//        var resetStatusBarCompose by remember { mutableStateOf(0L) }
//        val initDarkIcon = isSystemInDarkTheme().not()
//        var darkIcon by remember { mutableStateOf(initDarkIcon) }

        val systemUiController = rememberSystemUiController()
        LaunchedEffect(key1 = resetStatusBarCompose, block = {
            systemUiController.setStatusBarColor(Color.Transparent, darkIcons = darkIcon)
        })

        val ctx = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        DisposableEffect(lifecycle) {
            // Make MapView follow the current lifecycle
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
//                    darkIcon = ctx.isInNightMode.not()
                    if (setStatusBarComposeAuto) resetStatusBarCompose = System.currentTimeMillis()
                }
            }
            lifecycle.addObserver(lifecycleObserver)
            onDispose {
                lifecycle.removeObserver(lifecycleObserver)
            }
        }

    }

    @Composable
    fun AvoidStatusBar(content: @Composable ColumnScope.() -> Unit) {
        //  WindowCompat.setDecorFitsSystemWindows(window, false)

        // 加入ProvideWindowInsets
        ProvideWindowInsets {

//            rememberSystemUiController().setStatusBarColor(
//                Color.Transparent, darkIcons = MaterialTheme.colors.isLight
//            )

            val systemUiController = rememberSystemUiController()
            LaunchedEffect(key1 = resetStatusBarCompose, block = {
                systemUiController.setStatusBarColor(statusBarColorCompose, darkIcons = darkIcon)
            })

//            val ctx = LocalContext.current
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            DisposableEffect(lifecycle) {
                // Make MapView follow the current lifecycle
                val lifecycleObserver = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
//                    darkIcon = ctx.isInNightMode.not()
                        if (setStatusBarComposeAuto) resetStatusBarCompose = System.currentTimeMillis()
                    }
                }
                lifecycle.addObserver(lifecycleObserver)
                onDispose {
                    lifecycle.removeObserver(lifecycleObserver)
                }
            }

            Column {
                Spacer(
                    modifier = Modifier
                        .statusBarsHeight()
                        .fillMaxWidth()
                )

                content()
            }

        }
    }

}