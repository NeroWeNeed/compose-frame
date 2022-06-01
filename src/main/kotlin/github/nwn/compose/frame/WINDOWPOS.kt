package github.nwn.compose.frame

import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder
import com.sun.jna.platform.win32.WinDef.HWND


@FieldOrder("hwnd", "hwndInsertAfter", "x", "y", "cx", "cy", "flags")
open class WINDOWPOS : Structure, Structure.ByReference {
    constructor(pointer: Pointer) : super(pointer) {
        read()
    }
    constructor() : super()

    class ByReference : WINDOWPOS, Structure.ByReference {
        constructor(pointer: Pointer) : super(pointer)
        constructor() : super()
    }

    @JvmField
    var hwnd: HWND? = null
    @JvmField
    var hwndInsertAfter: HWND? = null
    @JvmField
    var x = 0
    @JvmField
    var y = 0
    @JvmField
    var cx = 0
    @JvmField
    var cy = 0
    @JvmField
    var flags = 0

    init {

    }


}