package com.github.ivanmisyats.copycontentplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class CopyItemContentAction : AnAction("Copy Content to the Clipboard") {
    override fun actionPerformed(e: AnActionEvent) {
        // Get all selected files/folders
        val virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return

        // Remove redundant selections (e.g. if a folder and one of its files are both selected, only the file is processed)
        val finalSelection = filterRedundant(virtualFiles.toList())
        val builder = StringBuilder()

        // Process each selected item.
        for (file in finalSelection) {
            if (file.isDirectory) {
                // For a directory, recursively process its children.
                processDirectory(file, builder)
            } else {
                processFile(file, builder)
            }
        }

        // Copy the assembled text to the system clipboard.
        val selection = StringSelection(builder.toString())
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
    }

    /**
     * Outputs a block for a text file in the following format:
     *
     * <file full path>:
     * ```
     * <file content>
     * ```
     *
     * Binary files are skipped.
     */
    private fun processFile(file: VirtualFile, builder: StringBuilder) {
        if (!isTextFile(file)) return
        builder.append("${file.path}:\n")
        val fileExtension = file.extension?.lowercase() ?: ""
        val languageIdentifier = mapFileExtensionToLanguage(fileExtension)
        builder.append("```$languageIdentifier\n")
        builder.append(getFileContent(file))
        builder.append("\n```\n\n")
    }

    /**
     * Recursively processes a directory by iterating its children.
     */
    private fun processDirectory(dir: VirtualFile, builder: StringBuilder) {
        for (child in dir.children) {
            if (child.isDirectory) {
                processDirectory(child, builder)
            } else {
                processFile(child, builder)
            }
        }
    }

    /**
     * Reads and returns the file content as a UTF-8 string.
     */
    private fun getFileContent(file: VirtualFile): String {
        return try {
            String(file.contentsToByteArray(), Charsets.UTF_8)
        } catch (ex: Exception) {
            "Error reading file: ${ex.message}"
        }
    }

    /**
     * Determines whether a file is a text file.
     * This implementation uses the file type; if the file type is marked as binary, it is ignored.
     */
    private fun isTextFile(file: VirtualFile): Boolean {
        return !file.fileType.isBinary
    }

    /**
     * Filters out any file that is an ancestor of another file in the selection.
     */
    private fun filterRedundant(files: List<VirtualFile>): List<VirtualFile> {
        return files.filter { file ->
            !files.any { other -> other !== file && isAncestor(file, other) }
        }
    }

    /**
     * Checks whether [ancestor] is an ancestor of [descendant] based on their paths.
     */
    private fun isAncestor(ancestor: VirtualFile, descendant: VirtualFile): Boolean {
        val ancestorPath = ancestor.path
        val descendantPath = descendant.path
        return descendantPath.startsWith(ancestorPath) &&
                descendantPath != ancestorPath &&
                descendantPath.substring(ancestorPath.length).startsWith("/")
    }

    /**
     * Maps file extensions to language identifiers for code block formatting.
     */
    private fun mapFileExtensionToLanguage(extension: String): String {
        val languageMap = mapOf(
            // Common programming languages
            "java" to "java",
            "kt" to "kotlin",
            "kts" to "kotlin",
            "groovy" to "groovy",
            "py" to "python",
            "js" to "javascript",
            "ts" to "typescript",
            "html" to "html",
            "xml" to "xml",
            "json" to "json",
            "yaml" to "yaml",
            "yml" to "yaml",
            "md" to "markdown",
            "css" to "css",
            "scss" to "scss",
            "sql" to "sql",
            "cs" to "csharp",
            "php" to "php",
            "rb" to "ruby",
            "go" to "go",
            "rs" to "rust",
            "sh" to "bash",
            "bat" to "batch",
            "ps1" to "powershell",
            "c" to "c",
            "cpp" to "cpp",
            "h" to "c",
            "hpp" to "cpp",
            "swift" to "swift",
            "dart" to "dart",
            "properties" to "properties",
            "gradle" to "gradle",
            "txt" to "text"
        )
        return if (languageMap.containsKey(extension)) "${languageMap[extension]}" else ""
    }
}