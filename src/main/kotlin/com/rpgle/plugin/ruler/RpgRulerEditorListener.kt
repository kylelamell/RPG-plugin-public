package com.rpgle.plugin.ruler

import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.event.VisibleAreaListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.Key
import com.rpgle.plugin.RpgFileType

/**
 * Installs the [RpgColumnRuler] as the header component of every main RPG file
 * editor. The header slot keeps the ruler pinned at the top while scrolling,
 * and setting it as the *permanent* header means it is restored after the
 * Find/Replace toolbar (which borrows the same slot) closes.
 */
class RpgRulerEditorListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        if (editor !is EditorEx) return
        if (editor.editorKind != EditorKind.MAIN_EDITOR) return
        if (editor.virtualFile?.fileType !== RpgFileType) return

        val ruler = RpgColumnRuler(editor)
        editor.setPermanentHeaderComponent(ruler)
        editor.setHeaderComponent(ruler)

        // Repaint on scroll/resize/zoom so the ruler tracks horizontal scrolling
        // and font changes; keep the reference so it can be detached on release.
        val listener = VisibleAreaListener { ruler.repaint() }
        editor.scrollingModel.addVisibleAreaListener(listener)
        editor.putUserData(LISTENER_KEY, listener)

        // Repaint on caret movement so the column-position highlight follows the
        // cursor; keep the reference so it can be detached on release.
        val caretListener = object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) = ruler.repaint()
        }
        editor.caretModel.addCaretListener(caretListener)
        editor.putUserData(CARET_LISTENER_KEY, caretListener)
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        val editor = event.editor
        editor.getUserData(LISTENER_KEY)?.let {
            editor.scrollingModel.removeVisibleAreaListener(it)
            editor.putUserData(LISTENER_KEY, null)
        }
        editor.getUserData(CARET_LISTENER_KEY)?.let {
            editor.caretModel.removeCaretListener(it)
            editor.putUserData(CARET_LISTENER_KEY, null)
        }
    }

    companion object {
        private val LISTENER_KEY =
            Key.create<VisibleAreaListener>("rpg.columnRuler.visibleAreaListener")
        private val CARET_LISTENER_KEY =
            Key.create<CaretListener>("rpg.columnRuler.caretListener")
    }
}
