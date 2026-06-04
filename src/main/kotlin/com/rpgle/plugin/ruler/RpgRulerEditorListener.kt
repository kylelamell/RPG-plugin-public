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
 * Installs the [RpgColumnRuler] as the permanent header component of every main RPG file editor,
 * so it stays pinned at the top while scrolling and survives the Find/Replace toolbar closing.
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

        val listener = VisibleAreaListener { ruler.repaint() }
        editor.scrollingModel.addVisibleAreaListener(listener)
        editor.putUserData(LISTENER_KEY, listener)

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
