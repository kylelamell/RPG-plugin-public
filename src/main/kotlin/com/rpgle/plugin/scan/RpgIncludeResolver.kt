package com.rpgle.plugin.scan

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

/**
 * Resolves /COPY and /INCLUDE targets (`LIBRARY/FILE,MEMBER`, a bare `MEMBER`, or a quoted IFS
 * path) to project files, matching member forms by file name and quoted paths relative to the
 * including file.
 */
object RpgIncludeResolver {

    private val INCLUDE_EXTENSIONS = listOf("rpgleinc", "rpgcpy", "rpgle", "sqlrpgle", "rpg")

    fun resolve(target: String, fromFile: PsiFile): List<VirtualFile> {
        val project = fromFile.project
        if (DumbService.isDumb(project)) return emptyList()

        var t = target.trim().removeSuffix(";").trim()
        val comment = t.indexOf("//")
        if (comment >= 0) t = t.substring(0, comment).trim()
        if (t.isEmpty()) return emptyList()

        if (t.startsWith("'") || t.startsWith("\"")) {
            return resolvePath(t.trim('\'', '"').trim(), fromFile)
        }

        val member = when {
            ',' in t -> t.substringAfterLast(',')
            '/' in t -> t.substringAfterLast('/')
            else -> t
        }.trim().trim('\'', '"')

        if (member.isEmpty()) return emptyList()
        return findByMember(member, project)
    }

    private fun findByMember(member: String, project: Project): List<VirtualFile> {
        val scope = GlobalSearchScope.allScope(project)
        val candidateNames = LinkedHashSet<String>()
        for (ext in INCLUDE_EXTENSIONS) {
            candidateNames.add("$member.$ext")
            candidateNames.add("${member.lowercase()}.$ext")
            candidateNames.add("${member.uppercase()}.$ext")
        }

        val result = LinkedHashSet<VirtualFile>()
        for (name in candidateNames) {
            result.addAll(FilenameIndex.getVirtualFilesByName(name, scope))
        }
        return result.toList()
    }

    private fun resolvePath(path: String, fromFile: PsiFile): List<VirtualFile> {
        val parent = fromFile.virtualFile?.parent
        val resolved = parent?.findFileByRelativePath(path)
            ?: LocalFileSystem.getInstance().findFileByPath(path)
        return listOfNotNull(resolved)
    }
}
