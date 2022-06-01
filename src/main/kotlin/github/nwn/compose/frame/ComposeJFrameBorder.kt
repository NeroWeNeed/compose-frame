package github.nwn.compose.frame


import java.awt.*
import java.awt.geom.Area
import javax.swing.border.Border
import kotlin.math.sqrt

class ComposeJFrameBorder(private val insets: Insets) : Border {
    constructor(borderThickness: Int) : this(Insets(borderThickness, borderThickness, borderThickness, borderThickness))
    constructor(top: Int, left: Int, bottom: Int, right: Int) : this(Insets(top, left, bottom, right))

    companion object {

        private val colors = arrayOf(Color(0, 0, 0, 255), Color(0, 0, 0,0))
        private val colorDistribution = floatArrayOf(0f, 1f)
    }

    override fun paintBorder(c: Component?, g: Graphics?, x: Int, y: Int, width: Int, height: Int) {
        val g2d = g as Graphics2D

        val radius = sqrt(((width / 2) * (width / 2)).toDouble() + ((height / 2) * (height / 2)).toDouble()).toFloat()
        g2d.paint = RadialGradientPaint(
            (x + width / 2).toFloat(),
            (y + height / 2).toFloat(),
            radius,
            colorDistribution,
            colors
        )

        val border = Area(Rectangle(x, y, width, height))
        border.subtract(
            Area(
                Rectangle(
                    x + insets.left,
                    y + insets.top,
                    width - insets.left - insets.right,
                    height - insets.top - insets.bottom
                )
            )
        )
        g2d.fill(border)
    }

    override fun getBorderInsets(c: Component?): Insets {
        return insets
    }

    override fun isBorderOpaque(): Boolean {
        return false
    }
}