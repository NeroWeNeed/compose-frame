package github.nwn.compose.frame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.WPARAM
import java.awt.BorderLayout
import java.awt.GraphicsConfiguration
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.plaf.metal.MetalLookAndFeel

class ComposeJFrame : JFrame {

    private val content: @Composable () -> Unit
    private val proc: ComposeJFrameWindowProc = ComposeJFrameWindowProc(12)

    constructor(content: @Composable () -> Unit) : super() {
        this.content = content
        initialize()
    }

    constructor(gc: GraphicsConfiguration?, content: @Composable () -> Unit) : super(
        gc
    ) {
        this.content = content
        initialize()
    }

    constructor(title: String?, content: @Composable () -> Unit) : super(title) {
        this.content = content
        initialize()
    }

    constructor(
        title: String?,
        gc: GraphicsConfiguration?,
        content: @Composable () -> Unit
    ) : super(title, gc) {
        this.content = content
        initialize()
    }

    internal fun getHwnd(): HWND {
        return HWND(Native.getComponentPointer(this))
    }

    override fun setVisible(b: Boolean) {
        super.setVisible(b)
        proc.init(getHwnd())
        val r = User32.INSTANCE.GetWindowLong(proc.hWnd, -16).toUInt() or 0x00040000u or 0x80000000u or 0x00C00000u
        User32.INSTANCE.SetWindowLong(proc.hWnd, -16, r.toInt())
    }

    override fun isUndecorated(): Boolean {
        return true
    }

    fun sendMessage(message: Int, wparam: WPARAM, lparam: LPARAM): LRESULT {
        return proc.sendMessage(message, wparam, lparam)
    }

    fun withHWND(op: (HWND) -> Unit) {
        SwingUtilities.invokeLater {
            op(proc.hWnd)
        }
    }


    private fun initialize() {


        layout = BorderLayout()
        val frameContentPane = JPanel().apply {
            layout = BorderLayout()
            isOpaque = false
            background = java.awt.Color(0, 0, 0, 0)
        }
        val clientContentPane = JPanel().apply {
            layout = BorderLayout()
            isOpaque = false
            background = java.awt.Color(0, 0, 0, 0)
        }
        val composePanel = ComposePanel().apply {
            isOpaque = true

            setContent {
                CompositionLocalProvider(LocalComposeJFrame provides this@ComposeJFrame) {
                    content()
                }
            }
        }
        clientContentPane.add(composePanel)
        frameContentPane.add(clientContentPane)
        rootPane.isOpaque = false
        background = java.awt.Color(0, 0, 0, 0)

        rootPane.background = java.awt.Color(0, 0, 0, 0)
        rootPane.border = ComposeJFrameBorder(4, 4, 4, 4)
        contentPane = frameContentPane
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                val src = e.source as ComposeJFrame
                if (src.extendedState and JFrame.MAXIMIZED_BOTH != 0) {
                    val z = User32.INSTANCE.GetSystemMetrics(ComposeJFrameWindowProc.SM_CXPADDEDBORDER)
                    val x = User32.INSTANCE.GetSystemMetrics(ComposeJFrameWindowProc.SM_CXFRAME) + z
                    val y = User32.INSTANCE.GetSystemMetrics(ComposeJFrameWindowProc.SM_CYFRAME) + z
                    rootPane.border = BorderFactory.createEmptyBorder(y, x, y, x)
                } else if (src.extendedState and MAXIMIZED_HORIZ != 0) {

                    val x =
                        User32.INSTANCE.GetSystemMetrics(ComposeJFrameWindowProc.SM_CXFRAME) + User32.INSTANCE.GetSystemMetrics(
                            ComposeJFrameWindowProc.SM_CXPADDEDBORDER
                        )
                    rootPane.border = ComposeJFrameBorder(0, x, 4, x)
                } else if (src.extendedState and MAXIMIZED_VERT != 0) {

                    val y =
                        User32.INSTANCE.GetSystemMetrics(ComposeJFrameWindowProc.SM_CYFRAME) + User32.INSTANCE.GetSystemMetrics(
                            ComposeJFrameWindowProc.SM_CXPADDEDBORDER
                        )
                    rootPane.border = ComposeJFrameBorder(y, 4, y, 4)
                } else {
                    rootPane.border = ComposeJFrameBorder(4, 4, 4, 4)
                }
            }
        })
        SwingUtilities.invokeLater {
            setSize(800, 600)
            setLocation(50, 50)
            isVisible = true
        }
    }
}

