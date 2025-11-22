package com.editor.workspace;

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
        TextEditor editor = workspace.getEditor(testFile.toString());
        assertNotNull(editor);
        assertEquals(2, editor.getLines().size());
    }

    @Test
    public void testInitFile() {
        String filePath = "newfile.txt";
        workspace.initFile(filePath, false);
        TextEditor editor = workspace.getEditor(filePath);
        assertNotNull(editor);
        assertTrue(workspace.isModified(filePath));
    }

    @Test
    public void testSaveFile() throws IOException {
        workspace.loadFile(testFile.toString());
        TextEditor editor = workspace.getEditor(testFile.toString());
        editor.append("Line 3");
        workspace.saveFile(testFile.toString());
        assertFalse(workspace.isModified(testFile.toString()));
    }

    @Test
    public void testCloseFile() throws IOException {
        workspace.loadFile(testFile.toString());
        workspace.closeFile(testFile.toString());
        assertNull(workspace.getEditor(testFile.toString()));
    }

    @Test
    public void testSetActiveFile() throws IOException {
        workspace.loadFile(testFile.toString());
        workspace.setActiveFile(testFile.toString());
        assertEquals(testFile.toString(), workspace.getActiveEditor().getFilePath());
    }
}

