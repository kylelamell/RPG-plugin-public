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
 * A one-row column ruler painted above an RPG editor. It is installed as the
 * editor's header component (see [RpgRulerEditorListener]), so it stays pinned
 * to the top while the document scrolls vertically.
 *
 * Geometry is read live from the editor on every paint — the column width, the
 * gutter width and the horizontal scroll offset — so the ruler stays aligned
 * with the characters below it, including after a font-size change or a
 * horizontal scroll.
 */
class RpgColumnRuler(private val editor: EditorEx) : JComponent() {

    init {
        isOpaque = true
    }

    override fun getPreferredSize(): Dimension =
    // One text row tall, plus a 1px bottom separator. Width is stretched to
        // the editor by the header layout, so it does not matter here.
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

        // Column 0 origin and per-column width, straight from the editor's own
        // layout so the ruler lines up exactly with the text.
        val originX = editor.logicalPositionToXY(LogicalPosition(0, 0)).x
        val charWidth = editor.logicalPositionToXY(LogicalPosition(0, 1)).x - originX
        if (charWidth <= 0) return

        val gutterWidth = editor.gutterComponentEx.width
        val scrollX = editor.scrollingModel.horizontalScrollOffset

        val fm = g2.getFontMetrics(font)
        val baseline = (height - fm.height) / 2 + fm.ascent

        val rulerColor = scheme.getColor(EditorColors.LINE_NUMBERS_COLOR) ?: JBColor.GRAY
        val text = RULER

        // Highlight the ruler cell aligned with the caret's column, but only
        // when the caret falls inside the marked range (columns 1..MAX_COLUMN).
        // The caret column is 0-based, which lines up directly with the ruler's
        // 0-based character index; a column outside [0, text.length) — before
        // the first or past the last — leaves the ruler unhighlighted.
        val caretColumn = editor.caretModel.primaryCaret.logicalPosition.column
        val highlightIndex = if (caretColumn in text.indices) caretColumn else -1
        if (highlightIndex >= 0) {
            val hx = gutterWidth + originX + highlightIndex * charWidth - scrollX
            g2.color = scheme.getColor(EditorColors.SELECTION_BACKGROUND_COLOR)
                ?: JBColor(Color(0xC8, 0xD4, 0xF5), Color(0x36, 0x58, 0x80))
            g2.fillRect(hx, 0, charWidth, height - 1)  // keep the bottom separator clear
        }

        for (i in text.indices) {
            val x = gutterWidth + originX + i * charWidth - scrollX
            if (x + charWidth < 0) continue   // scrolled off to the left
            if (x > width) break              // past the right edge
            // The highlighted glyph uses the main text color so it stays legible
            // on top of the highlight; every other glyph stays line-number gray.
            g2.color = if (i == highlightIndex) scheme.defaultForeground else rulerColor
            g2.drawString(text[i].toString(), x, baseline)
        }

        g2.color = scheme.getColor(EditorColors.TEARLINE_COLOR) ?: JBColor.LIGHT_GRAY
        g2.drawLine(0, height - 1, width, height - 1)
    }

    companion object {
        /** Columns are marked up to (and including) this one; nothing past it. */
        const val MAX_COLUMN = 100

        /** The ruler text, e.g. `....x....1....x....2....x....3...` up to column 100. */
        val RULER: String = buildRuler(MAX_COLUMN)

        /**
         * Builds the ruler: every tenth column shows the tens digit of that
         * column number (10→`1`, 20→`2`, … 90→`9`, 100→`0`), every remaining
         * fifth column shows `x`, and all others show `.`.
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
