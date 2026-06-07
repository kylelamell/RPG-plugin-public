package com.rpgle.plugin.scan

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.rpgle.plugin.RpgFileType

/**
 * Drops a file's cached symbol scan when its editor tab is closed, so an RPG file
 * that is no longer open retains no analysis data (it is re-scanned lazily if the
 * user reopens it).
 *
 * Closing the tab — not merely switching away from it — is the trigger. The
 * platform reports tab navigation through `selectionChanged`, which is left
 * unimplemented, so moving to another tab keeps the file you came from cached.
 */
class RpgFileCacheCleaner : FileEditorManagerListener {

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        if (file.fileType != RpgFileType) return
        val project = source.project
        if (project.isDisposed) return
        // The PsiFile was created while the editor was open, so this returns the
        // existing instance whose user data holds the caches we want to evict.
        val psi = PsiManager.getInstance(project).findFile(file) ?: return
        RpgSymbolScanner.dropCache(psi)
        RpgLocalSymbols.dropCache(psi)
        RpgSqlPresence.dropCache(psi)
    }
}
