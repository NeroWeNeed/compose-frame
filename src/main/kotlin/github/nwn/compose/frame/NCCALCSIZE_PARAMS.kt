package github.nwn.compose.frame

import com.sun.jna.Pointer
import com.sun.jna.Structure.*
import com.sun.jna.Structure
import com.sun.jna.platform.win32.WinDef.RECT

@FieldOrder("rgrc", "lppos")
class NCCALCSIZE_PARAMS : Structure, ByReference {
    constructor() : super()
    constructor(pointer: Pointer) : super(pointer) {
        read()
    }
    @JvmField
    var rgrc: Array<RECT> = arrayOf(RECT(), RECT(), RECT())
    @JvmField
    var lppos: WINDOWPOS.ByReference = WINDOWPOS.ByReference()

}