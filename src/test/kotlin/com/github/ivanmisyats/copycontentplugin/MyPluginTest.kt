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

    fun `test file extension to language mapping`() {
        // Create test files with different extensions
        val javaFile = myFixture.configureByText("Test.java", "public class Test {}")
        val kotlinFile = myFixture.configureByText("Test.kt", "class Test")
        val unknownFile = myFixture.configureByText("Test.xyz", "some content")

        // Create an action event with multiple files
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(javaFile.virtualFile, kotlinFile.virtualFile, unknownFile.virtualFile))
            .build()

        val action = CopyItemContentAction()
        val event = AnActionEvent.createFromAnAction(
            action,
            null,
            "CopyItemContentAction",
            dataContext
        )

        // Execute the action
        action.actionPerformed(event)

        // Check clipboard content
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val copiedText = clipboard.getData(DataFlavor.stringFlavor) as String

        // Verify correct language identifiers were used
        assertTrue("Java file should be marked with java language", copiedText.contains("```java"))
        assertTrue("Kotlin file should be marked with kotlin language", copiedText.contains("```kotlin"))
        assertTrue("Unknown extension should have no language identifier", copiedText.contains("```\nsome content"))
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