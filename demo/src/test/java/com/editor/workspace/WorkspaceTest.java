package com.editor.workspace;

import com.editor.editor.Editor;
import com.editor.editor.TextEditor;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Workspace测试类
 */
public class WorkspaceTest {
    private Workspace workspace;
    private Path testFile;

    @Before
    public void setUp() throws IOException {
        workspace = new Workspace();
        testFile = Files.createTempFile("test", ".txt");
        Files.write(testFile, java.util.Arrays.asList("Line 1", "Line 2"));
    }

    @Test
    public void testLoadFile() throws IOException {
        workspace.loadFile(testFile.toString());
        Editor editor = workspace.getEditor(testFile.toString());
        assertNotNull(editor);
        assertTrue(editor instanceof TextEditor);
        TextEditor textEditor = (TextEditor) editor;
        assertEquals(2, textEditor.getLines().size());
    }

    @Test
    public void testInitFile() {
        String filePath = "newfile.txt";
        workspace.initFile(filePath, false);
        Editor editor = workspace.getEditor(filePath);
        assertNotNull(editor);
        assertTrue(editor instanceof TextEditor);
        assertTrue(workspace.isModified(filePath));
    }

    @Test
    public void testSaveFile() throws IOException {
        workspace.loadFile(testFile.toString());
        Editor editor = workspace.getEditor(testFile.toString());
        assertNotNull(editor);
        assertTrue(editor instanceof TextEditor);
        TextEditor textEditor = (TextEditor) editor;
        textEditor.append("Line 3");
        workspace.saveFile(testFile.toString());
        assertFalse(workspace.isModified(testFile.toString()));
    }

    @Test
    public void testCloseFile() throws IOException {
        workspace.loadFile(testFile.toString());
        workspace.closeFile(testFile.toString());
        Editor editor = workspace.getEditor(testFile.toString());
        assertNull(editor);
    }

    @Test
    public void testSetActiveFile() throws IOException {
        workspace.loadFile(testFile.toString());
        workspace.setActiveFile(testFile.toString());
        assertEquals(testFile.toString(), workspace.getActiveEditor().getFilePath());
    }
}

