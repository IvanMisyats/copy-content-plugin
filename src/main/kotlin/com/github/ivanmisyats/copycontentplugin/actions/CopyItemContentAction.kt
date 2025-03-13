package com.github.ivanmisyats.copycontentplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.Service
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class CopyItemContentAction : AnAction("Copy File/Folder Content to Buffer") {
    override fun actionPerformed(e: AnActionEvent) {
        // Get the selected file/folder from the context
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val builder = StringBuilder()

        if (virtualFile.isDirectory) {
            builder.append("${virtualFile.path}:\n")
            processDirectory(virtualFile, builder)
        } else {
            builder.append("${virtualFile.path}:\n")
            builder.append(getFileContent(virtualFile))
        }

        // Copy the assembled text to the system clipboard
        val selection = StringSelection(builder.toString())
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
    }

    private fun processDirectory(dir: VirtualFile, builder: StringBuilder) {
        for (child in dir.children) {
            if (child.isDirectory) {
                builder.append("${child.path}:\n")
                processDirectory(child, builder)
            } else {
                builder.append("${child.path}:\n")
                builder.append(getFileContent(child))
                builder.append("\n")
            }
        }
    }

    private fun getFileContent(file: VirtualFile): String {
        return try {
            // Read file content as text (be mindful of encoding and potential IO exceptions)
            String(file.contentsToByteArray(), Charsets.UTF_8)
        } catch (ex: Exception) {
            "Error reading file: ${ex.message}"
        }
    }
}
