package github.nwn.compose.frame

import androidx.compose.ui.unit.IntSize
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.unix.X11.Window
import com.sun.jna.platform.win32.BaseTSD
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.WPARAM
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.platform.win32.WinUser.*
import com.sun.jna.win32.W32APIOptions
import kotlin.math.abs


interface User32Ex : User32 {
    companion object {
        const val GWLP_WNDPROC = -4
    }

    fun SetWindowLongPtr(hWnd: WinDef.HWND, nIndex: Int, wndProc: WinUser.WindowProc): BaseTSD.LONG_PTR
    fun SetWindowLongPtr(hWnd: WinDef.HWND, nIndex: Int, wndProc: BaseTSD.LONG_PTR): BaseTSD.LONG_PTR
    fun CallWindowProc(
        proc: BaseTSD.LONG_PTR,
        hWnd: WinDef.HWND,
        uMsg: Int,
        uParam: WinDef.WPARAM,
        lParam: WinDef.LPARAM
    ): WinDef.LRESULT
}

fun makeLParam(x: Int, y: Int): LPARAM {

    return LPARAM((x.toLong() and 0xffffL) or (y.toLong() and 0xffffL) shl 16)
}

class ComposeJFrameWindowProc(var borderThickness: Int) : WinUser.WindowProc {
    companion object {
        const val WM_NCCALCSIZE = 131
        const val WM_NCHITTEST = 132
        const val WM_NCLBUTTONDOWN = 0x00A1
        const val WM_NCLBUTTONUP = 0x00A2
        const val WM_NCLBUTTONDBLCLK = 0x00A3
        const val WM_NCRBUTTONDOWN = 0x0204
        const val WM_NCRBUTTONUP = 0x0205
        const val WM_NCRBUTTONDBLCLK = 0x0206
        const val WM_NCMBUTTONDOWN = 0x00A7
        const val WM_NCMBUTTONUP = 0x00A8
        const val WM_NCMBUTTONDBLCLK = 0x00A8
        const val WM_NCMOUSEMOVE = 0x00A0
        const val WM_NCMOUSEHOVER = 0x02A0
        const val WM_NCMOUSELEAVE = 0x02A2
        const val WM_MOVE = 0x0003
        const val WM_WINDOWPOSCHANGING = 0x0046

        const val HTTOPLEFT = 13
        const val HTTOP = 12
        const val HTCAPTION = 2
        const val HTTOPRIGHT = 14
        const val HTLEFT = 10
        const val HTNOWHERE = 0
        const val HTRIGHT = 11
        const val HTBOTTOMLEFT = 16
        const val HTBOTTOM = 15
        const val HTBOTTOMRIGHT = 17
        const val HTSYSMENU = 3

        const val SM_CXFRAME = 32
        const val SM_CXPADDEDBORDER = 92
        const val SM_CYFRAME = 33

        private val hitTestMatrix = listOf(
            HTTOPLEFT, HTNOWHERE, HTTOPRIGHT,
            HTLEFT, HTNOWHERE, HTRIGHT,
            HTBOTTOMLEFT, HTBOTTOM, HTBOTTOMRIGHT
        )

        fun hitTest(
            resizeBorder: Boolean,
            icon: Boolean,
            frameDrag: Boolean,
            uCol: Int,
            uRow: Int

        ): Long {
            return if (uCol == 1 && uRow == 0) {
                when {
                    resizeBorder -> HTTOP
                    frameDrag -> HTCAPTION
                    else -> HTNOWHERE
                }
            } else {
                hitTestMatrix[uRow * 3 + uCol]
            }.toLong()
        }

    }

    val INSTANCEEx: User32Ex = Native.load("user32", User32Ex::class.java, W32APIOptions.DEFAULT_OPTIONS)
    var hWnd: WinDef.HWND = WinDef.HWND()
        private set
    lateinit var defWndProc: BaseTSD.LONG_PTR

    fun init(hWnd: WinDef.HWND) {
        this.hWnd = hWnd
        this.defWndProc = INSTANCEEx.SetWindowLongPtr(hWnd, User32Ex.GWLP_WNDPROC, this)

        INSTANCEEx.SetWindowPos(
            hWnd,
            hWnd,
            0,
            0,
            400,
            400,
            SWP_NOMOVE.or(SWP_NOSIZE).or(SWP_NOZORDER).or(SWP_FRAMECHANGED)
        )

    }

    fun sendMessage(message: Int, wParam: WPARAM, lParam: LPARAM): LRESULT {
        return INSTANCEEx.SendMessage(hWnd, message, wParam, lParam)
    }

    override fun callback(
        hwnd: WinDef.HWND,
        uMsg: Int,
        wParam: WinDef.WPARAM,
        lParam: WinDef.LPARAM
    ): WinDef.LRESULT {

        return when (uMsg) {
            WM_NCCALCSIZE -> {
                if (wParam.toInt() != 0) {
                    WinDef.LRESULT(0)
                }
                else {
                    INSTANCEEx.CallWindowProc(defWndProc, hwnd, uMsg, wParam, lParam)
                }
            }
            WM_NCHITTEST -> {
                val lResult = hitTest(hwnd)
                if (lResult.toInt() == WinDef.LRESULT(0).toInt()) {
                    INSTANCEEx.CallWindowProc(defWndProc, hwnd, uMsg, wParam, lParam)
                } else {
                    lResult
                }
            }
            WM_DESTROY -> {
                INSTANCEEx.SetWindowLongPtr(hwnd, User32Ex.GWLP_WNDPROC, defWndProc)
                WinDef.LRESULT(0)
            }
            else -> {
                INSTANCEEx.CallWindowProc(defWndProc, hwnd, uMsg, wParam, lParam)
            }
        }
    }

    fun hitTest(hWnd: WinDef.HWND): WinDef.LRESULT {
        val mousePoint = WinDef.POINT()
        val windowRect = WinDef.RECT()
        User32.INSTANCE.GetCursorPos(mousePoint)
        User32.INSTANCE.GetWindowRect(hWnd, windowRect)


        var fOnResizeBorder = false
        var fOnFrameDrag = false
        var fOnIcon = false
        val uRow = when {
            (mousePoint.y >= windowRect.top && mousePoint.y < windowRect.top + borderThickness) -> {
                fOnResizeBorder = mousePoint.y < windowRect.top + borderThickness
                if (!fOnResizeBorder) {
                    fOnIcon =
                        mousePoint.y <= windowRect.top + borderThickness && mousePoint.x > windowRect.left &&
                                mousePoint.x < windowRect.left
                    if (!fOnIcon) {
                        fOnFrameDrag =
                            mousePoint.y <= windowRect.top + borderThickness &&
                                    mousePoint.x < (windowRect.right )
                                    && mousePoint.x > (windowRect.left)
                    }
                }
                0
            }
            mousePoint.y < windowRect.bottom && mousePoint.y >= windowRect.bottom - borderThickness -> 2
            else -> 1
        }
        val uCol = when {
            mousePoint.x >= windowRect.left && mousePoint.x < windowRect.left + borderThickness -> 0
            mousePoint.x < windowRect.right && mousePoint.x >= windowRect.right - borderThickness -> 2
            else -> 1
        }
        return WinDef.LRESULT(hitTest(fOnResizeBorder, fOnIcon, fOnFrameDrag, uCol, uRow))
    }
}