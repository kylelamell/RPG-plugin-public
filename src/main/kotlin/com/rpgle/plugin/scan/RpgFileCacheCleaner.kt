package com.rpgle.plugin.scan

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.rpgle.plugin.RpgFileType

/**
 * Drops a file's cached symbol scan when its editor tab is closed, so a closed RPG file retains no
 * analysis data and is re-scanned lazily if reopened. Switching to another tab is not a trigger.
 */
class RpgFileCacheCleaner : FileEditorManagerListener {

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        if (file.fileType != RpgFileType) return
        val project = source.project
        if (project.isDisposed) return
        val psi = PsiManager.getInstance(project).findFile(file) ?: return
        RpgSymbolScanner.dropCache(psi)
        RpgLocalSymbols.dropCache(psi)
    }
}