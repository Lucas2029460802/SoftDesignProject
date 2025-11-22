package com.editor.command;

import com.editor.logging.Logger;
import com.editor.workspace.Workspace;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CommandParser测试类
 */
public class CommandParserTest {
    private Workspace workspace;
    private Logger logger;
    private CommandParser parser;
    private Path testFile;

    @Before
    public void setUp() throws IOException {
        workspace = new Workspace();
        logger = new Logger();
        parser = new CommandParser(workspace, logger);
        testFile = Files.createTempFile("test", ".txt");
        Files.write(testFile, java.util.Arrays.asList("Line 1"));
    }

    @Test
    public void testLoadCommand() {
        String result = parser.execute("load " + testFile.toString());
        assertTrue(result.contains("已加载"));
    }

    @Test
    public void testAppendCommand() throws IOException {
        workspace.loadFile(testFile.toString());
        String result = parser.execute("append \"New Line\"");
        assertTrue(result.contains("已追加"));
    }

    @Test
    public void testShowCommand() throws IOException {
        workspace.loadFile(testFile.toString());
        String result = parser.execute("show");
        assertTrue(result.contains("1:"));
    }

    @Test
    public void testEditorListCommand() throws IOException {
        workspace.loadFile(testFile.toString());
        String result = parser.execute("editor-list");
        assertTrue(result.contains(testFile.getFileName().toString()));
    }

    @Test
    public void testUndoRedoCommand() throws IOException {
        workspace.loadFile(testFile.toString());
        parser.execute("append \"Test\"");
        String undoResult = parser.execute("undo");
        assertTrue(undoResult.contains("撤销"));
    }
}

