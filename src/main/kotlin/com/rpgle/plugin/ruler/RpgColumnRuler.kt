package com.rpgle.plugin.ruler

import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent

/**
 * A one-row column ruler painted above an RPG editor as its header component
 * (see [RpgRulerEditorListener]). Geometry is read live from the editor on every paint,
 * so the ruler stays aligned with the characters below it.
 */
class RpgColumnRuler(private val editor: EditorEx) : JComponent() {

    init {
        isOpaque = true
    }

    override fun getPreferredSize(): Dimension =
        Dimension(0, editor.lineHeight + JBUI.scale(1))

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        val scheme = editor.colorsScheme

        g2.color = scheme.defaultBackground
        g2.fillRect(0, 0, width, height)

        val font = scheme.getFont(EditorFontType.PLAIN)
        g2.font = font
        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
        )

        val originX = editor.logicalPositionToXY(LogicalPosition(0, 0)).x
        val charWidth = editor.logicalPositionToXY(LogicalPosition(0, 1)).x - originX
        if (charWidth <= 0) return

        val gutterWidth = editor.gutterComponentEx.width
        val scrollX = editor.scrollingModel.horizontalScrollOffset

        val fm = g2.getFontMetrics(font)
        val baseline = (height - fm.height) / 2 + fm.ascent

        val rulerColor = scheme.getColor(EditorColors.LINE_NUMBERS_COLOR) ?: JBColor.GRAY
        val text = RULER

        val caretColumn = editor.caretModel.primaryCaret.logicalPosition.column
        val highlightIndex = if (caretColumn in text.indices) caretColumn else -1
        if (highlightIndex >= 0) {
            val hx = gutterWidth + originX + highlightIndex * charWidth - scrollX
            g2.color = scheme.getColor(EditorColors.SELECTION_BACKGROUND_COLOR)
                ?: JBColor(Color(0xC8, 0xD4, 0xF5), Color(0x36, 0x58, 0x80))
            g2.fillRect(hx, 0, charWidth, height - 1)
        }

        for (i in text.indices) {
            val x = gutterWidth + originX + i * charWidth - scrollX
            if (x + charWidth < 0) continue
            if (x > width) break
            g2.color = if (i == highlightIndex) scheme.defaultForeground else rulerColor
            g2.drawString(text[i].toString(), x, baseline)
        }

        g2.color = scheme.getColor(EditorColors.TEARLINE_COLOR) ?: JBColor.LIGHT_GRAY
        g2.drawLine(0, height - 1, width, height - 1)
    }

    companion object {
        const val MAX_COLUMN = 100

        val RULER: String = buildRuler(MAX_COLUMN)

        /**
         * Builds the ruler: every tenth column shows the tens digit of the column number,
         * every remaining fifth column shows `x`, and all others show `.`.
         */
        fun buildRuler(maxColumn: Int): String {
            val sb = StringBuilder(maxColumn)
            for (c in 1..maxColumn) {
                sb.append(
                    when {
                        c % 10 == 0 -> '0' + (c / 10) % 10
                        c % 5 == 0 -> 'x'
                        else -> '.'
                    }
                )
            }
            return sb.toString()
        }
    }
}
