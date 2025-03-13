package com.github.ivanmisyats.copycontentplugin;

import com.github.ivanmisyats.copycontentplugin.actions.CopyItemContentAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

class MyPluginTest : BasePlatformTestCase() {

    fun `test copy file content`() {
        // 1. Create a test file in the fixture
        val testContent = "Some sample content"
        val psiFile = myFixture.configureByText("TestFile.txt", testContent)

        // 2. Create an AnActionEvent that references this file
        val event = createActionEventForFile(psiFile.virtualFile)

        // 3. Invoke your action (replace with your actual class name)
        val action = CopyItemContentAction()
        action.actionPerformed(event)

        // 4. Check that the clipboard has the expected text
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val copiedText = clipboard.getData(DataFlavor.stringFlavor) as String
        assertTrue("Clipboard should contain file content", copiedText.contains(testContent))
    }

    private fun createActionEventForFile(file: VirtualFile): AnActionEvent {
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.VIRTUAL_FILE, file)
            .build()
        // Create an instance of your action and provide a non-null "place" string.
        val action = CopyItemContentAction()
        return AnActionEvent.createFromAnAction(
            action,
            null,
            "CopyItemContentAction", // Use a string identifier for the place
            dataContext
        )
    }
}