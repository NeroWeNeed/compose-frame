package github.nwn.compose.frame

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser.MONITORINFO
import com.sun.jna.platform.win32.WinUser.WINDOWPLACEMENT
import kotlin.math.abs
import kotlin.math.ceil

@ExperimentalComposeUiApi
@Composable
fun ComposeJFrameNonClient(modifier: Modifier = Modifier) {
    val current = LocalComposeJFrame.current
    //val coroutineScope = rememberCoroutineScope()
    var size by remember { mutableStateOf(IntSize.Zero) }
    var sendEnter by remember { mutableStateOf(false) }
    var dragStart by remember { mutableStateOf(IntSize.Zero) }
    var dragOffset by remember { mutableStateOf(IntOffset(-1, -1)) }
    var dragging by remember { mutableStateOf(false) }
    Box(modifier.onSizeChanged {
        size = it
    }.pointerInput(Unit) {

        detectDragGestures(onDragStart = { offset ->
            dragOffset = IntOffset(offset.x.toInt(), offset.y.toInt())
            dragging = true
        }, onDragEnd = {
            dragging = false
        }, onDragCancel = {
            dragging = false
        }) { change, dragAmount ->
            current?.withHWND {
                val cursorPos = WinDef.POINT()
                val windowPlacement = WINDOWPLACEMENT()
                User32.INSTANCE.GetWindowPlacement(it, windowPlacement)
                windowPlacement.length = windowPlacement.size()
                User32.INSTANCE.GetCursorPos(cursorPos)
                if (windowPlacement.showCmd == 3 && dragAmount.y >= 3f) {
                    val monitor = User32.INSTANCE.MonitorFromWindow(it, User32.MONITOR_DEFAULTTONEAREST)
                    val monitorInfo = MONITORINFO()
                    User32.INSTANCE.GetMonitorInfo(monitor, monitorInfo)
                    val xRatio = change.position.x / abs(monitorInfo.rcMonitor.right - monitorInfo.rcMonitor.left)
                    val yRatio = change.position.y / abs(monitorInfo.rcMonitor.top - monitorInfo.rcMonitor.bottom)
                    dragOffset = IntOffset(
                        ((windowPlacement.rcNormalPosition.right - windowPlacement.rcNormalPosition.left) * xRatio).toInt(),
                        ((windowPlacement.rcNormalPosition.bottom - windowPlacement.rcNormalPosition.top) * yRatio).toInt()
                    )
                    windowPlacement.rcNormalPosition.apply {
                        val w = right - left
                        val h = bottom - top
                        left = cursorPos.x - dragOffset.x
                        top = cursorPos.y - dragOffset.y
                        right = left + w
                        bottom = top + h
                    }
                    windowPlacement.flags = 0x0004
                    windowPlacement.write()
                    User32.INSTANCE.ShowWindow(it, 1)

                } else {
                    windowPlacement.rcNormalPosition.apply {
                        val w = right - left
                        val h = bottom - top
                        left = cursorPos.x - dragOffset.x
                        top = cursorPos.y - dragOffset.y
                        right = left + w
                        bottom = top + h
                    }
                    windowPlacement.flags = 0x0004
                    windowPlacement.write()
                    User32.INSTANCE.SetWindowPlacement(it, windowPlacement)
                }
            }
        }


    }.pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = {
                current?.sendMessage(
                    ComposeJFrameWindowProc.WM_NCLBUTTONDBLCLK,
                    WinDef.WPARAM(ComposeJFrameWindowProc.HTCAPTION.toLong()),
                    makeLParam(
                        it.x.toInt(), it.y.toInt()
                    )
                )
            }
        )
    }) {

    }
}